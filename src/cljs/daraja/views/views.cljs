(ns daraja.views.views)

(defn encode-ui [app-state]
  [:div
   [:p "Encode"]
   [:input#encode {:type "text" :placeholder "Text"}]
   (when-let [ss (:encoded-string app-state)]
     [:span (str "\t" " Encoded string, ") [:strong ss]])
   [:p ""]])

(defn authenticate-ui [app-state]
  [:div
   [:p "Authenticate"]
   [:input#key {:type "text" :placeholder "Key"}]
   [:p ""]
   [:input#secret {:type "text" :placeholder "Secret"}]
   [:p ""]
   (when-let [ss (:access-token app-state)]
     [:span (str "\t" " Access token, ") [:strong ss]])
   [:p ""]])

(defn balance-ui [app-state]
  [:div
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
   (when-let [ss (:balance app-state)]
     [:span (str "\t" " Response, ") [:strong ss]])
   [:p ""]
   ])

(defn b2b [app-state]
  [:div
   [:p "B2B API"]
   [:input#b2b-access-t {:type "text" :placeholder "Access token"}]
   [:p ""]
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
   (when-let [ss (:b2b app-state)]
     [:span (str "\t" " Response, ") [:strong ss]])
   [:p ""]])

(defn b2c [app-state]
  [:div
   [:p "B2C Payment Request API"]
   [:input#b2c-access-t {:type "text" :placeholder "Access token"}]
   [:p ""]
   [:input#b2c-initiator {:type "text" :placeholder "Initiator"}]
   [:p ""]
   [:input#b2c-security-credential {:type "text" :placeholder "Security credential"}]
   [:p ""]
   [:input#b2c-command-id {:type "text" :placeholder "Command id (Optional)"}]
   [:p ""]
   [:input#b2c-sender-id {:type "text" :placeholder "Sender party"}]
   [:p ""]
   [:input#b2c-receiver-id {:type "text" :placeholder "Receiver party"}]
   [:p ""]
   [:input#b2c-amount {:type "text" :placeholder "Amount"}]
   [:p ""]
   [:input#b2c-remarks {:type "text" :placeholder "Remarks"}]
   [:p ""]
   [:input#b2c-queue-url {:type "text" :placeholder "Queue Url"}]
   [:p ""]
   [:input#b2c-result-url {:type "text" :placeholder "Result Url"}]
   [:p ""]
   [:input#b2c-occasion {:type "text" :placeholder "Occasion (Optional)"}]
   [:p ""]
   (when-let [ss (:b2c app-state)]
     [:span (str "\t" " Response, ") [:strong ss]])
   [:p ""]
   ])