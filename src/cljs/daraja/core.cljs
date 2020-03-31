(ns ^:figwheel-hooks daraja.core
  (:require
    [reagent.core :as reagent :refer [atom]]
    [cljs.core.async :refer (<! >! put! chan)]
    [taoensso.encore :refer-macros (have have?)]
    [taoensso.timbre :refer-macros (tracef debugf infof warnf errorf)]
    [taoensso.sente :as sente :refer (cb-success?)]
    [taoensso.sente.packers.transit :as sente-transit]
    ))

(println "This text is printed from src/daraja/core.cljs. Go ahead and edit it and see reloading in action.")

(defn multiply [a b] (* a b))

(let [packer (sente-transit/get-transit-packer)
      {:keys [chsk ch-recv send-fn state]}
      (sente/make-channel-socket-client! "/chsk"
                                         {:type   :auto
                                          :packer packer})]
  (def chsk chsk)
  (def ch-chsk ch-recv)
  (def chsk-send! send-fn)
  (def chsk-state state))


;; define your app data so that it doesn't get over-written on reload
(defonce app-state (atom {:text "Hello world!"}))


(defn hello-world []
  [:div
   [:p (:text @app-state)]
   [:p "Edit this in src/daraja/core.cljs and watch it change!"]
   [:button
    {:on-click (fn [e] (do (chsk-send! [:example/test-rapid-push])
                         (js/console.log "Sending request !!")))}
    "Try this"]
   [:button
    {:on-click (fn [e] (do (sente/chsk-disconnect! chsk)
                         (js/console.log "Disconnecting !!")))}
    "Disconnect"]
   [:button
    {:on-click (fn [e] (do (sente/chsk-reconnect! chsk)
                         (js/console.log "Reconnecting !!")))}
    "Reconnect"]])



;; Sente event handlers

(defmulti -event-msg-handler :id)

(defn event-msg-handler [{:as ev-msg :keys [id ?data event]}]
  (debugf "Event: %s" event) []
  (-event-msg-handler ev-msg))
(defmethod -event-msg-handler :default
  [{:as ev-msg :keys [event]}]
  (debugf "Unhandled event: %s" event))
(defmethod -event-msg-handler :chsk/state
  [{:as ev-msg :keys [?data]}]
  (let [[old-state-map new-state-map] (have vector? ?data)]
    (if (:first-open? new-state-map)
      (debugf "Channel socket successfully established!: %s" ?data)
      (debugf "Channel socket state change: %s" ?data))))
(defmethod -event-msg-handler :chsk/handshake
  [{:as ev-msg :keys [?data]}]
  (let [[?uid ?csrf-token ?handshake-data] ?data]
    (debugf "Handshake: %s" ?data)))
(defmethod -event-msg-handler :chsk/recv
  [{:as ev-msg :keys [?data]}]
  (let [[id msg] ?data]
    (cond (= id :daraja.core/:auth)
          (swap! app-state assoc :new-msg msg)

          :default
          (debugf "Push event from server: %s" ?data))))

;; TODO Add (defmethod -event-msg-handler <event-id> [ev-msg] <body>)s here...

;; Sente event router (our `event-msg-handler` loop)
(def router_ (atom nil))
(defn stop-router! []
  (when-let [stop-f @router_] (stop-f)))
(defn start-router! []
  (stop-router!)
  (reset! router_ (sente/start-client-chsk-router! ch-chsk event-msg-handler)))


(defn mount
  []
  (let [container (.getElementById js/document "app")]
    (reagent/render-component  [hello-world] container)))

(mount)

(defn ^:after-load on-reload []
  (mount)
  ;; (swap! app-state update-in [:__figwheel_counter] inc)
  )

(start-router!)