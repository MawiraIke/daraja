(ns ^:figwheel-hooks daraja.core
  (:require
    [reagent.core :as reagent :refer [atom]]
    [cljs.core.async :refer (<! >! put! chan)]
    [taoensso.encore :refer-macros (have have?)]
    [taoensso.timbre :refer-macros (tracef debugf infof warnf errorf)]
    [taoensso.sente :as sente :refer (cb-success?)]
    ))

(defn multiply [a b] (* a b))

(let [packer :edn
      {:keys [chsk ch-recv send-fn state]}
      (sente/make-channel-socket-client! "/chsk"
                                         {:type   :auto
                                          :packer packer})]
  (def chsk chsk)
  (def ch-chsk ch-recv)
  (def chsk-send! send-fn)
  (def chsk-state state))

(print "Reload")

;; define your app data so that it doesn't get over-written on reload
(def app-state (reagent/atom {:text         "Hello world!"
                              :access-token nil
                              :encode       nil}))

(defn get-input-value [input-id]
  (when-let [el (.getElementById js/document input-id)]
    (.-value el)))

(defn hello-world []
  [:div.row {:after {:content ""
                     :display "table"
                     :clear   "both"}}

   ;; Column 1

   [:div.column {:style {:float "left" :width "33%"}}
    ;; encode
    [:p "Encode"]
    [:input#encode {:type "text" :placeholder "Text"}]
    (when-let [ss (:encoded-string @app-state)]
      [:span (str "\t" " Encoded string, ") [:strong ss]])
    [:p ""]
    [:button
     {:on-click (fn [e]
                  (chsk-send! [::encode {:string (get-input-value "encode")}]
                              20000
                              (fn [cb-reply]
                                (if (sente/cb-success? cb-reply)
                                  (let [encoded-string (:reply cb-reply)]
                                    (swap! app-state assoc :encoded-string encoded-string)
                                    (js/console.log "Completed, " encoded-string))
                                  (js/console.error "Error")))))}
     "Encode"]
    [:p "--------------------"]


    [:p "Authenticate"]
    [:input#key {:type "text" :placeholder "Key"}]
    [:p ""]
    [:input#secret {:type "text" :placeholder "Secret"}]
    [:p ""]
    (when-let [ss (:access-token @app-state)]
      [:span (str "\t" " Access token, ") [:strong ss]])
    [:p ""]
    [:button
     {:on-click (fn [e] (chsk-send! [::auth {:key    (get-input-value "key")
                                             :secret (get-input-value "secret")}]
                                    20000
                                    (fn [cb-reply] (if (sente/cb-success? cb-reply) ; Checks for :chsk/closed, :chsk/timeout, :chsk/error
                                                     (let [cb-reply (:reply cb-reply)
                                                           access-token (:access_token cb-reply)
                                                           expires (:expires_in cb-reply)]
                                                       (swap! app-state assoc :access-token (if access-token
                                                                                              access-token
                                                                                              (str "Failed, " cb-reply)))
                                                       (js/console.log "Completed, " cb-reply))
                                                     (js/console.error "Error")))))}
     "Authenticate"]
    [:p "--------------------"]

    [:p "Balance API"]
    [:input#bal-access-t {:type "text" :placeholder "Access token"}]
    [:p ""]
    [:input#bal-initiator {:type "text" :placeholder "Initiator"}]
    [:p ""]
    [:input#bal-short-code {:type "text" :placeholder "Short code"}]
    [:p ""]
    [:input#bal-queue-url {:type "text" :placeholder "Queue Url"}]
    [:p ""]
    [:input#bal-result-url {:type "text" :placeholder "Result Url"}]
    [:p ""]
    [:input#bal-security-credential {:type "text" :placeholder "Security credential"}]
    [:p ""]
    [:input#bal-remarks {:type "text" :placeholder "Remarks (Optional)"}]
    [:p ""]
    (when-let [ss (:balance @app-state)]
      [:span (str "\t" " Response, ") [:strong ss]])
    [:p ""]
    [:button
     {:on-click (fn [e] (chsk-send! [::balance {:access-token        (get-input-value "bal-access-t")
                                                :party-a             (int (get-input-value "bal-short-code"))
                                                :initiator           (get-input-value "bal-initiator")
                                                :security-credential (get-input-value "bal-security-credential")
                                                :remarks             (get-input-value "bal-remarks")
                                                ;; use custom url here, make sure you use ngrok or localtunnel if
                                                ;; you are using localhost
                                                :queue-url           (get-input-value "bal-queue-url")
                                                :result-url          (get-input-value "bal-result-url")}]
                                    20000
                                    (fn [cb-reply] (if (sente/cb-success? cb-reply) ; Checks for :chsk/closed, :chsk/timeout, :chsk/error
                                                     (let [cb-reply (:reply cb-reply)
                                                           original-conversation-id (:OriginatorConversationID cb-reply)
                                                           conversation-id (:ConversationID cb-reply)
                                                           response-code (:ResponseCode cb-reply)
                                                           response-description (:ResponseDescription cb-reply)]
                                                       (swap! app-state assoc :balance (if conversation-id
                                                                                         (str conversation-id ", " response-description)
                                                                                         (str "Failed, " cb-reply)))
                                                       (js/console.log "Completed, " cb-reply))
                                                     (js/console.error "Error")))))}
     "Check balance"]
    [:p "--------------------"]

    [:button
     {:on-click (fn [e]
                  (print "Rapid button was pushed")
                  (chsk-send! [:example/test-rapid-push]))}
     "Rapid test"]
    [:button
     {:on-click (fn [e] (chsk-send! [:daraja.core/button {:had-a-callback? "indeed"}]
                                    5000
                                    (fn [cb-reply]
                                      (swap! app-state assoc :text (str cb-reply))
                                      (print "Callback reply: %s" cb-reply))))}
     "Broadcast test"]
    [:button
     {:on-click (fn [e] (chsk-send! [:example/toggle-broadcast] 5000
                                    (fn [cb-reply]
                                      (when (cb-success? cb-reply)
                                        (let [loop-enabled? cb-reply]
                                          (if loop-enabled?
                                            (print "Async broadcast loop now enabled")
                                            (print "Async broadcast loop now disabled")))))))}
     "Disconnect"]
    [:button
     {:on-click (fn [e] (do (sente/chsk-reconnect! chsk)
                            (js/console.log "Reconnecting !!")))}
     "Reconnect"]]

   ;; Column 2

   [:div.column {:style {:float "left" :width "33%"}}

    [:p "B2B API"]
    [:input#b2b-initiator {:type "text" :placeholder "Initiator"}]
    [:p ""]
    [:input#b2b-security-credential {:type "text" :placeholder "Security credential"}]
    [:p ""]
    [:input#b2b-command-id {:type "text" :placeholder "Command id (Optional)"}]
    [:p ""]
    [:input#b2b-sender-id {:type "text" :placeholder "Sender Identifier Type (Optional)"}]
    [:p ""]
    [:input#b2b-receiver-id {:type "text" :placeholder "Receiver identifier type (Optional)"}]
    [:p ""]
    [:input#b2b-amount {:type "text" :placeholder "Amount"}]
    [:p ""]
    [:input#b2b-party-a {:type "text" :placeholder "Party A"}]
    [:p ""]
    [:input#b2b-party-b {:type "text" :placeholder "Party B"}]
    [:p ""]
    [:input#b2b-account-ref {:type "text" :placeholder "Account reference"}]
    [:p ""]
    [:input#b2b-remarks {:type "text" :placeholder "Remarks"}]
    [:p ""]
    [:input#b2b-queue-url {:type "text" :placeholder "Queue Url"}]
    [:p ""]
    [:input#b2b-result-url {:type "text" :placeholder "Result Url"}]
    [:p ""]
    [:button
     {:on-click (fn [e] (chsk-send! [::b2b {:data ""}]
                                    20000
                                    (fn [cb-reply] (if (sente/cb-success? cb-reply) ; Checks for :chsk/closed, :chsk/timeout, :chsk/error
                                                     (let [access-token (:access-token cb-reply)
                                                           expires (:expires_in cb-reply)]
                                                       (swap! app-state assoc :access-token access-token)
                                                       (js/console.log "Completed, " cb-reply))
                                                     (js/console.error "Error")))))}
     "Send B2B request"]
    [:p "--------------------"]

    [:p "B2C Payment Request API"]
    [:button
     {:on-click (fn [e] (chsk-send! [::b2c {:data ""}]
                                    20000
                                    (fn [cb-reply] (if (sente/cb-success? cb-reply) ; Checks for :chsk/closed, :chsk/timeout, :chsk/error
                                                     (let [access-token (:access-token cb-reply)
                                                           expires (:expires_in cb-reply)]
                                                       (swap! app-state assoc :access-token access-token)
                                                       (js/console.log "Completed, " cb-reply))
                                                     (js/console.error "Error")))))}
     "Send B2C request"]
    [:p "--------------------"]
    ]

   ;; Column 3
   [:div.column {:style {:float "left" :width "33%"}}
    [:p "C2B Register API"]
    [:button
     {:on-click (fn [e] (chsk-send! [::c2b {:data ""}]
                                    20000
                                    (fn [cb-reply] (if (sente/cb-success? cb-reply) ; Checks for :chsk/closed, :chsk/timeout, :chsk/error
                                                     (let [access-token (:access-token cb-reply)
                                                           expires (:expires_in cb-reply)]
                                                       (swap! app-state assoc :access-token access-token)
                                                       (js/console.log "Completed, " cb-reply))
                                                     (js/console.error "Error")))))}
     "C2B Register API"]
    [:p "--------------------"]

    [:p "C2B Sim API"]
    [:button
     {:on-click (fn [e] (chsk-send! [::c2bs {:data ""}]
                                    20000
                                    (fn [cb-reply] (if (sente/cb-success? cb-reply) ; Checks for :chsk/closed, :chsk/timeout, :chsk/error
                                                     (let [access-token (:access-token cb-reply)
                                                           expires (:expires_in cb-reply)]
                                                       (swap! app-state assoc :access-token access-token)
                                                       (js/console.log "Completed, " cb-reply))
                                                     (js/console.error "Error")))))}
     "C2B Simulate"]
    [:p "--------------------"]

    [:p "Lipa na Mpesa API"]
    [:button
     {:on-click (fn [e] (chsk-send! [::lipa {:data ""}]
                                    20000
                                    (fn [cb-reply] (if (sente/cb-success? cb-reply) ; Checks for :chsk/closed, :chsk/timeout, :chsk/error
                                                     (let [access-token (:access-token cb-reply)
                                                           expires (:expires_in cb-reply)]
                                                       (swap! app-state assoc :access-token access-token)
                                                       (js/console.log "Completed, " cb-reply))
                                                     (js/console.error "Error")))))}
     "Lipa na M-Pesa"]
    [:p "--------------------"]

    [:p "Transaction Status"]
    [:button
     {:on-click (fn [e] (chsk-send! [::status {:data ""}]
                                    20000
                                    (fn [cb-reply] (if (sente/cb-success? cb-reply) ; Checks for :chsk/closed, :chsk/timeout, :chsk/error
                                                     (let [access-token (:access-token cb-reply)
                                                           expires (:expires_in cb-reply)]
                                                       (swap! app-state assoc :access-token access-token)
                                                       (js/console.log "Completed, " cb-reply))
                                                     (js/console.error "Error")))))}
     "Check Transaction status"]
    [:p "--------------------"]]

   ])



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
    (reagent/render-component [hello-world] container)))

(mount)

(defn ^:after-load on-reload []
  (mount)
  ;; (swap! app-state update-in [:__figwheel_counter] inc)
  )

(start-router!)