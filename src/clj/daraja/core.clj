(ns daraja.core
  (:require
    [ring.middleware.defaults]
    [ring.middleware.gzip :refer [wrap-gzip]]
    [ring.middleware.cljsjs :refer [wrap-cljsjs]]
    [ring.util.response :as response]
    [ring.middleware.anti-forgery :refer (*anti-forgery-token*)]
    [clojure-mpesa-wrapper.core :as mpesa]
    [compojure.core :as comp :refer (defroutes GET POST)]
    [compojure.route :as route]
    [clojure.core.async :as async :refer (<! <!! >! >!! put! chan go go-loop)]
    [taoensso.sente :as sente]
    [taoensso.encore :as encore :refer (have have?)]
    [taoensso.timbre :as log :refer (tracef debugf infof warnf errorf)]
    [aleph.http :as aleph]
    [taoensso.sente.server-adapters.aleph :refer (get-sch-adapter)]
    [taoensso.sente.packers.transit :as sente-transit]
    [cheshire.core :as json]
    [clojure.java.io :as io])
  (:import (java.io File Closeable)
           (java.awt Desktop HeadlessException Desktop$Action)
           (java.net URI)
           (java.util UUID)))

(def default-port 10666)

(log/set-level! :info)

(let [packer (sente-transit/get-transit-packer)
      chsk-server (sente/make-channel-socket-server! (get-sch-adapter) {:packer packer})
      {:keys [ch-recv send-fn connected-uids ajax-post-fn ajax-get-or-ws-handshake-fn]} chsk-server]
  (def ring-ajax-post ajax-post-fn)
  (def ring-ajax-get-or-ws-handshake ajax-get-or-ws-handshake-fn)
  (def ch-chsk ch-recv)
  (def chsk-send! send-fn)
  (def connected-uids connected-uids))

(defn send-all!
  [data]
  (doseq [uid (:any @connected-uids)]
    (chsk-send! uid data)))

(send-all! "Trial")

(add-watch connected-uids :connected-uids
           (fn [_ _ old new]
             (when (not= old new)
               (infof "Connected uids change: %s" new))))
(defn connected-uids? []
  @connected-uids)

(defn unique-id
  "Get a unique id for a session."
  []
  (str (UUID/randomUUID)))

(defn session-uid
  "Get session uuid from a request."
  [req]
  (get-in req [:session :uid]))

(defn strip-path-seps
  [path]
  (if (= (last path)
         (File/separatorChar))
    (strip-path-seps (apply str (drop-last path)))
    path))

(defn join-paths
  [path1 path2]
  (str (strip-path-seps path1)
       (File/separatorChar)
       path2))

(defonce current-root-dir (atom ""))

(defroutes my-routes
           (GET  "/" req (response/content-type
                           {:status 200
                            :session (if (session-uid req)
                                       (:session req)
                                       (assoc (:session req) :uid (unique-id)))
                            :body (io/input-stream (io/resource "public/index.html"))}
                           "text/html"))
           (GET "/token" req (json/generate-string {:csrf-token *anti-forgery-token*}))
           (GET  "/chsk" req
                 (debugf "/chsk got: %s" req)
                 (ring-ajax-get-or-ws-handshake req))
           (POST "/chsk" req (ring-ajax-post req))
           (route/resources "/" {:root "public"})
           (GET "*" req (let [reqpath (join-paths @current-root-dir (-> req :params :*))
                              reqfile (io/file reqpath)
                              altpath (str reqpath ".html")
                              dirpath (join-paths reqpath "index.html")]
                          (cond
                            ;; If the path exists, use that
                            (and (.exists reqfile) (not (.isDirectory reqfile)))
                            (response/file-response reqpath)
                            ;; If not, look for a `.html` version and if found serve that instead
                            (.exists (io/file altpath))
                            (response/content-type (response/file-response altpath) "text/html")
                            ;; If the path is a directory, check for index.html
                            (and (.exists reqfile) (.isDirectory reqfile))
                            (response/file-response dirpath)
                            ;; Otherwise, not found
                            :else (response/redirect "/"))))
           (route/not-found "<h1>There's no place like home</h1>"))

(def main-ring-handler
  (-> my-routes
      (ring.middleware.defaults/wrap-defaults ring.middleware.defaults/site-defaults)
      (wrap-cljsjs)
      (wrap-gzip)))

(defmulti -event-msg-handler :id)

(defn event-msg-handler [{:as ev-msg :keys [id ?data event]}]
  (tracef "Event: %s" event)
  (-event-msg-handler ev-msg))

(defmethod -event-msg-handler :default
  [{:as ev-msg :keys [event id ?data ring-req ?reply-fn send-fn]}]
  (let [session (:session ring-req)
        uid (:uid session)]
    (tracef "Unhandled event: %s" event)
    (when ?reply-fn
      (?reply-fn {:umatched-event-as-echoed-from-from-server event}))))

(defonce router_ (atom nil))
(defn stop-router! [] (when-let [stop-fn @router_] (stop-fn)))
(defn start-router! []
  (stop-router!)
  (reset! router_
          (sente/start-server-chsk-router! ch-chsk event-msg-handler)))

(defonce web-server_ (atom nil))
(defn web-server-started? [] @web-server_)
(defn stop-web-server! [] (when-let [stop-fn (:stop-fn @web-server_)] (stop-fn)))
(defn start-web-server! [& [port]]
  (stop-web-server!)
  (let [port (or port default-port)
        ring-handler (var main-ring-handler)
        [port stop-fn]
        (let [server (aleph/start-server ring-handler {:port port})
              p (promise)]
          (future @p)
          [(aleph.netty/port server)
           (fn [] (.close ^Closeable server) (deliver p nil))])
        uri (format "http://localhost:%s/" port)]
    (infof "Web server is running at `%s`" uri)
    (reset! web-server_ {:port port :stop-fn stop-fn})
    (try
      (if (and (Desktop/isDesktopSupported)
               (.isSupported (Desktop/getDesktop) Desktop$Action/BROWSE))
        (.browse (Desktop/getDesktop) (URI. uri))
        (.exec (Runtime/getRuntime) (str "xdg-open" uri)))
      (Thread/sleep 7500)
      (catch HeadlessException _))))
(defn get-server-port [] (:port @web-server_))

(defn stop! []
  (stop-router!)
  (stop-web-server!))

(defn start-server!
  ([]
   (start-web-server! default-port)
   (start-router!))
  ([& [port]]
   (start-web-server! (or port default-port))
   (start-router!)))

(defn -main []
  (start-server! 10666)
  )
