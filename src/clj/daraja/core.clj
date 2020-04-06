(ns daraja.core
  (:require
    [ring.middleware.defaults]
    [ring.middleware.gzip :refer [wrap-gzip]]
    [ring.middleware.cljsjs :refer [wrap-cljsjs]]
    [ring.util.response :as response]
    [ring.middleware.anti-forgery :refer (*anti-forgery-token*)]
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
    [clojure.java.io :as io]
    ;; daraja api
    [clojure-mpesa-wrapper.core :as mpesa]
    [daraja.Keys :as kys])
  (:import (java.io File Closeable)
           (java.awt Desktop HeadlessException Desktop$Action)
           (java.net URI)
           (java.util UUID)))


(defn encode [a-string]
  (mpesa/encode a-string))
(defn auth [key-string secret-string]
  (mpesa/auth key-string secret-string))
(defn balance [{:as v}]
  (mpesa/balance v))
(defn b2b [{:as v}]
  (mpesa/b2b v))
(defn b2c [{:as v}]
  (mpesa/b2c v))
(defn c2b [{:as v}]
  (mpesa/c2b-reg v))
(defn c2b-sim [{:as v}]
  (mpesa/c2b-sim v))
(def default-port 10666)

(log/set-level! :info)

(let [packer :edn
      chsk-server (sente/make-channel-socket-server! (get-sch-adapter) {:packer        packer
                                                                        :csrf-token-fn nil})
      {:keys [ch-recv send-fn connected-uids ajax-post-fn ajax-get-or-ws-handshake-fn]} chsk-server]
  (def ring-ajax-post ajax-post-fn)
  (def ring-ajax-get-or-ws-handshake ajax-get-or-ws-handshake-fn)
  (def ch-chsk ch-recv)
  (def chsk-send! send-fn)
  (def connected-uids connected-uids))
(add-watch connected-uids :connected-uids
           (fn [_ _ old new]
             (when (not= old new)
               (infof "Connected uids change: %s" new))))

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

(defn send-message [broadcast message]
  (doseq [uid (:any @connected-uids)]
    (chsk-send! uid
                [broadcast
                 (assoc message :to-whom uid)])))

(defn return-nil-for-strings [a-string]
  (if (or (nil? a-string) (= a-string ""))
    nil
    a-string))

(defonce current-root-dir (atom ""))
(defroutes my-routes
           (GET "/" req (response/content-type
                          {:status  200
                           :session (if (session-uid req)
                                      (:session req)
                                      (assoc (:session req) :uid (unique-id)))
                           :body    (io/input-stream (io/resource "public/index.html"))}
                          "text/html"))
           (GET "/success" req (response/content-type
                                 {:status  200
                                  :session (if (session-uid req)
                                             (:session req)
                                             (assoc (:session req) :uid (unique-id)))
                                  :body    (io/input-stream (io/resource "public/index.html"))}
                                 "text/html"))
           (GET "/fail" req (response/content-type
                              {:status  200
                               :session (if (session-uid req)
                                          (:session req)
                                          (assoc (:session req) :uid (unique-id)))
                               :body    (io/input-stream (io/resource "public/index.html"))}
                              "text/html"))
           (GET "/token" req (json/generate-string {:csrf-token *anti-forgery-token*}))
           (GET "/chsk" req
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

;; test rapid server to user pushes
(defn test-fast-server>user-pushes
  "Quickly pushes 100 events to all connected users. Note that this'll be
  fast+reliable even over Ajax!"
  []
  (doseq [uid (:any @connected-uids)]
    (doseq [i (range 100)]
      (chsk-send! uid [:fast-push/is-fast (str "hello " i "!!")]))))

;; test server to user broadcasts
(defonce broadcast-enabled?_ (atom true))
(defn start-example-broadcaster!
  "As an example of server>user async pushes, setup a loop to broadcast an
  event to all connected users every 10 seconds"
  []
  (let [broadcast!
        (fn [i]
          (let [uids (:any @connected-uids)]
            (debugf "Broadcasting server>user: %s uids" (count uids))
            (send-message :some/broadcast
                          {:what-is-this "An async broadcast pushed from server"
                           :how-often    "Every 10 seconds"
                           :i            i})))]

    (go-loop [i 0]
      (<! (async/timeout 10000))
      (when @broadcast-enabled?_ (broadcast! i))
      (recur (inc i)))))

;; event handlers
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
(defmethod -event-msg-handler :example/test-rapid-push
  [ev-msg] (test-fast-server>user-pushes))
(defmethod -event-msg-handler :example/toggle-broadcast
  [{:as ev-msg :keys [?reply-fn]}]
  (let [loop-enabled? (swap! broadcast-enabled?_ not)]
    (?reply-fn loop-enabled?)))

;; encode
(defmethod -event-msg-handler ::encode
  [{:as ev-msg :keys [event id ?data ring-req ?reply-fn send-fn]}]
  (let [session (:session ring-req)
        uid (:uid session)]
    (when ?reply-fn
      (?reply-fn {:reply (encode (:string (second event)))}))))

;; authenticate
(defmethod -event-msg-handler ::auth
  [{:as ev-msg :keys [event id ?data ring-req ?reply-fn send-fn]}]
  (let [session (:session ring-req)
        uid (:uid session)]
    (when ?reply-fn
      (?reply-fn {:reply (auth (:key (second event)) (:secret (second event)))}))))

;; balance
(defmethod -event-msg-handler ::balance
  [{:as ev-msg :keys [event id ?data ring-req ?reply-fn send-fn]}]
  (let [session (:session ring-req)
        uid (:uid session)]
    (when ?reply-fn
      (?reply-fn {:reply (balance {:access-token        (:access-token (second event))
                                   :initiator           (:initiator (second event))
                                   :short-code          (:party-a (second event))
                                   :security-credential (:security-credential (second event))
                                   :remarks             (return-nil-for-strings (:remarks (second event)))
                                   :queue-url           (:queue-url (second event))
                                   :result-url          (:result-url (second event))})}))))

;; b2b
(defmethod -event-msg-handler ::b2b
  [{:as ev-msg :keys [event id ?data ring-req ?reply-fn send-fn]}]
  (let [session (:session ring-req)
        uid (:uid session)]
    (when ?reply-fn
      (?reply-fn {:reply (b2b {:access-token             (:access-token (second event))
                               :initiator                (:initiator (second event))
                               :command-id               (return-nil-for-strings (:command-id (second event)))
                               :amount                   (:amount (second event))
                               :sender-identifier-type   (:sender-identifier-type (second event))
                               :receiver-identifier-type (:receiver-identifier-type (second event))
                               :party-a                  (:party-a (second event))
                               :party-b                  (:party-b (second event))
                               :account-reference        (:account-reference (second event))
                               :security-credential      (:security-credential (second event))
                               :remarks                  (return-nil-for-strings (:remarks (second event)))
                               :queue-url                (:queue-url (second event))
                               :result-url               (:result-url (second event))})}))))


;; b2c
(defmethod -event-msg-handler ::b2c
  [{:as ev-msg :keys [event id ?data ring-req ?reply-fn send-fn]}]
  (let [session (:session ring-req)
        uid (:uid session)]
    (when ?reply-fn
      (?reply-fn {:reply (b2c {:access-token        (:access-token (second event))
                               :initiator-name      (:initiator-name (second event))
                               :command-id          (return-nil-for-strings (:command-id (second event)))
                               :amount              (:amount (second event))
                               :sender-party        (:sender-party (second event))
                               :receiver-party      (:receiver-party (second event))
                               :occasion            (return-nil-for-strings (:occasion (second event)))
                               :security-credential (:security-credential (second event))
                               :remarks             (return-nil-for-strings (:remarks (second event)))
                               :queue-url           (:queue-url (second event))
                               :result-url          (:result-url (second event))})}))))


;; c2b
(defmethod -event-msg-handler ::c2b
  [{:as ev-msg :keys [event id ?data ring-req ?reply-fn send-fn]}]
  (let [session (:session ring-req)
        uid (:uid session)]
    (when ?reply-fn
      (?reply-fn {:reply (c2b {:access-token     (:access-token (second event))
                               :short-code       (:short-code (second event))
                               :response-type    (:response-type (second event))
                               :confirmation-url (:confirmation-url (second event))
                               :validation-url   (:validation-url (second event))})}))))


;; c2b-sim
(defmethod -event-msg-handler ::c2b-sim
  [{:as ev-msg :keys [event id ?data ring-req ?reply-fn send-fn]}]
  (let [session (:session ring-req)
        uid (:uid session)]
    (when ?reply-fn
      (?reply-fn {:reply (c2b-sim {:access-token    (:access-token (second event))
                                   :short-code      (:short-code (second event))
                                   :command-id      (:command-id (second event))
                                   :amount          (:amount (second event))
                                   :msisdn          (:msisdn (second event))
                                   :bill-ref-number (return-nil-for-strings (:bill-ref-number (second event)))})}))))

;; router functions
(defonce router_ (atom nil))
(defn stop-router! [] (when-let [stop-fn @router_] (stop-fn)))
(defn start-router! []
  (stop-router!)
  (reset! router_
          (sente/start-server-chsk-router! ch-chsk event-msg-handler)))

;; web server functions
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
   (start-router!)
   (start-web-server! default-port))
  ([& [port]]
   (start-router!)
   (start-web-server! (or port default-port))))

(defn -main
  "For `lein run`, etc."
  []
  (start-server!))
