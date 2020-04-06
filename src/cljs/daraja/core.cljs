(ns ^:figwheel-hooks daraja.core
  (:require
    [reagent.core :as reagent :refer [atom]]
    [cljs.core.async :refer (<! >! put! chan)]
    [taoensso.encore :refer-macros (have have?)]
    [taoensso.timbre :refer-macros (tracef debugf infof warnf errorf)]
    [taoensso.sente :as sente :refer (cb-success?)]
    [daraja.views.views :as views]
    ))

(let [packer :edn
      {:keys [chsk ch-recv send-fn state]}
      (sente/make-channel-socket-client! "/chsk"
                                         {:type   :auto
                                          :packer packer})]
  (def chsk chsk)
  (def ch-chsk ch-recv)
  (def chsk-send! send-fn)
  (def chsk-state state))

(def app-state (reagent/atom {:selected "Balance"}))

;; timeout before the functions throw errors if no response is received
;; this is a lot of time. In case you have stable and fast change it to
;; 5000 or 10000.
(def timeout 20000)

;; gets the input value for an html element by id
(defn get-input-value [input-id]
  (when-let [el (.getElementById js/document input-id)]
    (.-value el)))

(def column-styles {:style {:float "left" :width "25%"}})

(defn bal2bc-button-fn [cb-reply ky]
  (if (sente/cb-success? cb-reply)                          ; Checks for :chsk/closed, :chsk/timeout, :chsk/error
    (let [cb-reply (:reply cb-reply)
          original-conversation-id (:OriginatorConversationID cb-reply)
          conversation-id (:ConversationID cb-reply)
          response-code (:ResponseCode cb-reply)
          response-description (:ResponseDescription cb-reply)]
      (swap! app-state assoc ky (if conversation-id
                                  (str conversation-id ", " response-description)
                                  (str "Failed, " cb-reply)))
      (js/console.log "Completed, " cb-reply))
    (js/console.error "Error")))

(defn c2-button-fn [cb-reply ky]
  (if (sente/cb-success? cb-reply)                          ; Checks for :chsk/closed, :chsk/timeout, :chsk/error
    (let [cb-reply (:reply cb-reply)
          original-conversation-id (:OriginatorConversationID cb-reply)
          conversation-id (:ConversationID cb-reply)
          response-description (:ResponseDescription cb-reply)]
      (swap! app-state assoc ky (if conversation-id
                                  (str conversation-id ", " response-description)
                                  (str "Failed, " cb-reply)))
      (js/console.log "Completed, " cb-reply))
    (js/console.error "Error")))

(defn hello-world []
  [:div.container
   [:div.form-group
    [:label "pick an API option"]
    [:select.form-control {:field :list :id :many.options :on-change #(swap! app-state assoc :selected (.. % -target -value))}
     [:option {:key :bal} "Balance"]
     [:option {:key :b2b} "B2B"]
     [:option {:key :b2c} "B2C"]
     [:option {:key :c2b} "C2B register"]
     [:option {:key :c2b-sim} "C2B simulate"]
     [:option {:key :lipa} "Lipa na mpesa"]
     [:option {:key :trans} "Transaction status"]
     ]]
   [:div.row

    ;; Column 1

    [:div.col


     ;; encode
     [:div
      (views/encode-ui @app-state)
      [:button.btn.btn-dark.btn-sm
       {:on-click (fn [e]
                    (chsk-send! [::encode {:string (get-input-value "encode")}]
                                timeout
                                (fn [cb-reply]
                                  (if (sente/cb-success? cb-reply)
                                    (let [encoded-string (:reply cb-reply)]
                                      (swap! app-state assoc :encoded-string encoded-string)
                                      (js/console.log "Completed, " encoded-string))
                                    (js/console.error "Error")))))}
       "Encode"]]
     [:p "--------------------"]

     ;; authenticate
     [:div
      (views/authenticate-ui @app-state)
      [:button.btn.btn-dark.btn-sm
       {:on-click (fn [e] (chsk-send! [::auth {:key    (get-input-value "key")
                                               :secret (get-input-value "secret")}]
                                      timeout
                                      (fn [cb-reply] (if (sente/cb-success? cb-reply) ;; Checks for :chsk/closed,
                                                       ;;                                 :chsk/timeout, :chsk/error
                                                       (let [cb-reply (:reply cb-reply)
                                                             access-token (:access_token cb-reply)
                                                             expires (:expires_in cb-reply)]
                                                         (swap! app-state assoc :access-token
                                                                (if access-token
                                                                  access-token
                                                                  (str "Failed, " cb-reply)))
                                                         (js/console.log "Completed, " cb-reply))
                                                       (js/console.error "Error")))))}
       "Authenticate"]]

     [:p ""]
     [:button.btn.btn-dark.btn-sm
      {:on-click (fn [e]
                   (print "Rapid button was pushed")
                   (chsk-send! [:example/test-rapid-push]))}
      "Rapid test"]
     [:button.btn.btn-dark.btn-sm
      {:on-click (fn [e] (chsk-send! [:daraja.core/button {:had-a-callback? "indeed"}]
                                     5000
                                     (fn [cb-reply]
                                       (swap! app-state assoc :text (str cb-reply))
                                       (print "Callback reply: %s" cb-reply))))}
      "Broadcast test"]
     [:button.btn.btn-dark.btn-sm
      {:on-click (fn [e] (chsk-send! [:example/toggle-broadcast] 5000
                                     (fn [cb-reply]
                                       (when (cb-success? cb-reply)
                                         (let [loop-enabled? cb-reply]
                                           (if loop-enabled?
                                             (print "Async broadcast loop now enabled")
                                             (print "Async broadcast loop now disabled")))))))}
      "Disconnect"]
     [:button.btn.btn-dark.btn-sm
      {:on-click (fn [e] (do (sente/chsk-reconnect! chsk)
                             (js/console.log "Reconnecting !!")))}
      "Reconnect"]]


    [:div.col
     (cond
       (= (:selected @app-state) "Balance")
       [:div
        (views/balance-ui @app-state)
        [:button.btn.btn-dark.btn-sm
         {:on-click (fn [e] (chsk-send! [::balance {:access-token        (get-input-value "bal-access-t")
                                                    :party-a             (int (get-input-value "bal-short-code"))
                                                    :initiator           (get-input-value "bal-initiator")
                                                    :security-credential (get-input-value "bal-security-credential")
                                                    :remarks             (get-input-value "bal-remarks")
                                                    ;; use custom url here, make sure you use ngrok or localtunnel if
                                                    ;; you are using localhost
                                                    :queue-url           (get-input-value "bal-queue-url")
                                                    :result-url          (get-input-value "bal-result-url")}]
                                        timeout
                                        #(bal2bc-button-fn % :balance)))}
         "Check balance"]]

       (= (:selected @app-state) "B2B")
       [:div
        (views/b2b @app-state)
        [:button.btn.btn-dark.btn-sm
         {:on-click (fn [e] (chsk-send! [::b2b {:access-token             (get-input-value "b2b-access-t")
                                                :initiator                (get-input-value "b2b-initiator")
                                                :command-id               (get-input-value "b2b-command-id")
                                                :amount                   (int (get-input-value "b2b-amount"))
                                                :sender-identifier-type   (int (get-input-value "b2b-sender-id"))
                                                :receiver-identifier-type (int (get-input-value "b2b-receiver-id"))
                                                :party-a                  (int (get-input-value "b2b-party-a"))
                                                :party-b                  (int (get-input-value "b2b-party-b"))
                                                :account-reference        (get-input-value "b2b-account-ref")
                                                :remarks                  (get-input-value "b2b-remarks")
                                                :queue-url                (get-input-value "b2b-queue-url")
                                                :result-url               (get-input-value "b2b-result-url")
                                                :security-credential      (get-input-value "b2b-security-credential")}]
                                        timeout
                                        #(bal2bc-button-fn % :b2b)))}
         "Send B2B request"]]

       (= (:selected @app-state) "B2C")
       [:div
        (views/b2c @app-state)
        [:button.btn.btn-dark.btn-sm
         {:on-click (fn [e] (chsk-send! [::b2c {:access-token        (get-input-value "b2c-access-t")
                                                :initiator-name      (get-input-value "b2c-initiator")
                                                :amount              (int (get-input-value "b2c-amount"))
                                                :sender-party        (int (get-input-value "b2c-sender-id"))
                                                :receiver-party      (cljs.reader/read-string
                                                                       (get-input-value "b2c-receiver-id"))
                                                :queue-url           (get-input-value "b2c-queue-url")
                                                :result-url          (get-input-value "b2c-result-url")
                                                :security-credential (get-input-value "b2c-security-credential")
                                                :occasion            (get-input-value "b2c-occasion")
                                                :remarks             (get-input-value "b2c-remarks")
                                                :command-id          (get-input-value "b2c-command-id")}]
                                        timeout
                                        #(bal2bc-button-fn % :b2c)))}
         "Send B2C request"]]

       (= (:selected @app-state) "C2B register")
       [:div
        (views/c2b-reg @app-state)
        [:button.btn.btn-dark.btn-sm
         {:on-click (fn [e] (chsk-send! [::c2b {:access-token     (get-input-value "c2b-access-t")
                                                :short-code       (int (get-input-value "c2b-short-code"))
                                                :response-type    (get-input-value "c2b-response-type")
                                                :confirmation-url (get-input-value "c2b-confirmation-url")
                                                :validation-url   (get-input-value "c2b-validation-url")
                                                }]
                                        timeout
                                        #(c2-button-fn % :c2b)))}
         "C2B Register API"]]

       (= (:selected @app-state) "C2B simulate")
       [:div
        (views/c2b-sim @app-state)
        [:button.btn.btn-dark.btn-sm
         {:on-click (fn [e] (chsk-send! [::c2b-sim {:access-token    (get-input-value "c2b-sim-access-t")
                                                    :short-code      (int (get-input-value "c2b-sim-short-code"))
                                                    :command-id      (get-input-value "c2b-sim-command-id")
                                                    :amount          (int (get-input-value "c2b-sim-amount"))
                                                    :msisdn          (cljs.reader/read-string
                                                                       (get-input-value "c2b-sim-msisdn"))
                                                    :bill-ref-number (get-input-value "c2b-sim-bill-ref-number")}]
                                        timeout
                                        #(c2-button-fn % :c2b-sim)))}
         "C2B Simulate"]]

       (= (:selected @app-state) "Lipa na mpesa")
       [:div
        [:p "Lipa na Mpesa API"]
        [:button.btn.btn-dark.btn-sm
         {:on-click (fn [e] (chsk-send! [::lipa {:data ""}]
                                        timeout
                                        (fn [cb-reply] (if (sente/cb-success? cb-reply) ; Checks for :chsk/closed, :chsk/timeout, :chsk/error
                                                         (let [access-token (:access-token cb-reply)
                                                               expires (:expires_in cb-reply)]
                                                           (swap! app-state assoc :access-token access-token)
                                                           (js/console.log "Completed, " cb-reply))
                                                         (js/console.error "Error")))))}
         "Lipa na M-Pesa"]]

       (= (:selected @app-state) "Transaction status")
       [:div
        [:p "Transaction Status"]
        [:button.btn.btn-dark.btn-sm
         {:on-click (fn [e] (chsk-send! [::status {:data ""}]
                                        timeout
                                        (fn [cb-reply] (if (sente/cb-success? cb-reply) ; Checks for :chsk/closed, :chsk/timeout, :chsk/error
                                                         (let [access-token (:access-token cb-reply)
                                                               expires (:expires_in cb-reply)]
                                                           (swap! app-state assoc :access-token access-token)
                                                           (js/console.log "Completed, " cb-reply))
                                                         (js/console.error "Error")))))}
         "Check Transaction status"]])]]])



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

(def router_ (atom nil))
(defn stop-router! []
  (when-let [stop-f @router_] (stop-f)))
(defn start-router! []
  (stop-router!)
  (reset! router_ (sente/start-client-chsk-router! ch-chsk event-msg-handler)))


(defn mount []
  (let [container (.getElementById js/document "app")]
    (reagent/render-component [hello-world] container)))

(mount)

(defn ^:after-load on-reload []
  (mount))

(start-router!)