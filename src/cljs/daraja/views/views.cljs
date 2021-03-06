(ns daraja.views.views)

(defn encode-ui [app-state]
  [:div
   [:p "Encode"]
   [:input#encode {:type "text" :placeholder "Text"}]
   [:div
    (when-let [ss (:encoded-string app-state)]
      [:span (str "\t" " Encoded string, ") [:strong ss]])]
   [:p ""]])

(defn authenticate-ui [app-state]
  [:div
   [:p "Authenticate"]
   [:input#key {:type "text" :placeholder "Key"}]
   [:p ""]
   [:input#secret {:type "text" :placeholder "Secret"}]
   [:p ""]
   [:div
    (when-let [ss (:access-token app-state)]
      [:span (str "\t" " Access token, ") [:strong ss]])]
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
   [:div
    (when-let [ss (:balance app-state)]
      [:span (str "\t" " Response, ") [:strong ss]])]
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
   [:div
    (when-let [ss (:b2b app-state)]
      [:span (str "\t" " Response, ") [:strong ss]])]
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
   [:div
    (when-let [ss (:b2c app-state)]
      [:span (str "\t" " Response, ") [:strong ss]])]
   [:p ""]
   ])

(defn c2b-reg [app-state]
  [:div
   [:p "C2B Register API"]
   [:input#c2b-access-t {:type "text" :placeholder "Access token"}]
   [:p ""]
   [:input#c2b-short-code {:type "text" :placeholder "Short code"}]
   [:p ""]
   [:input#c2b-response-type {:type "text" :placeholder "Response type"}]
   [:p ""]
   [:input#c2b-confirmation-url {:type "text" :placeholder "Confirmation Url"}]
   [:p ""]
   [:input#c2b-validation-url {:type "text" :placeholder "Validation Url"}]
   [:p ""]
   [:div
    (when-let [ss (:c2b app-state)]
      [:span (str "\t" " Response, ") [:strong ss]])]
   [:p ""]])

(defn c2b-sim [app-state]
  [:div
   [:p "C2B Simulate API"]
   [:input#c2b-sim-access-t {:type "text" :placeholder "Access token"}]
   [:p ""]
   [:input#c2b-sim-short-code {:type "text" :placeholder "Short code"}]
   [:p ""]
   [:input#c2b-sim-command-id {:type "text" :placeholder "Command Id"}]
   [:p ""]
   [:input#c2b-sim-amount {:type "text" :placeholder "Amount"}]
   [:p ""]
   [:input#c2b-sim-msisdn {:type "text" :placeholder "MSISDN"}]
   [:p ""]
   [:input#c2b-sim-bill-ref-number {:type "text" :placeholder "Bill Ref Number"}]
   [:p ""]
   [:div
    (when-let [ss (:c2b-sim app-state)]
      [:span (str "\t" " Response, ") [:strong ss]])]
   [:p ""]
   ])

(defn lipa [app-state]
  [:div
   [:p "Lipa na Mpesa API"]
   [:input#lipa-a {:type "text" :placeholder "Access token"}]
   [:p ""]
   [:input#lipa-b {:type "text" :placeholder "Short code"}]
   [:p ""]
   [:input#lipa-c {:type "text" :placeholder "Transaction Type"}]
   [:p ""]
   [:input#lipa-d {:type "text" :placeholder "Amount"}]
   [:p ""]
   [:input#lipa-e {:type "text" :placeholder "Phone Number"}]
   [:p ""]
   [:input#lipa-f {:type "text" :placeholder "Callback Url"}]
   [:p ""]
   [:input#lipa-g {:type "text" :placeholder "Account reference (Optional)"}]
   [:p ""]
   [:input#lipa-h {:type "text" :placeholder "Transaction Description (Optional)"}]
   [:p ""]
   [:input#lipa-i {:type "text" :placeholder "Pass key"}]
   [:p ""]
   [:div
    (when-let [ss (:lipa app-state)]
      [:span (str "\t" " Response, ") [:strong ss]])]
   [:p ""]
   ])

(defn trans [app-state]
  [:div
   [:p "Transaction Status"]
   [:input#trans-access-t {:type "text" :placeholder "Access token"}]
   [:p ""]
   [:input#trans-security-credential {:type "text" :placeholder "Security credential"}]
   [:p ""]
   [:input#trans-initiator {:type "text" :placeholder "Initiator"}]
   [:p ""]
   [:input#trans-command-id {:type "text" :placeholder "Command Id (Optional)"}]
   [:p ""]
   [:input#trans-trans-id {:type "text" :placeholder "Transaction ID"}]
   [:p ""]
   [:input#trans-party-a {:type "text" :placeholder "Party A"}]
   [:p ""]
   [:input#trans-trans-iden {:type "text" :placeholder "Identifier Type (Optional)"}]
   [:p ""]
   [:input#trans-result-url {:type "text" :placeholder "Result Url"}]
   [:p ""]
   [:input#trans-queue-url {:type "text" :placeholder "Queue Url"}]
   [:p ""]
   [:input#trans-remarks {:type "text" :placeholder "Remarks (Optional)"}]
   [:p ""]
   [:input#trans-occasion {:type "text" :placeholder "Occasion (Optional)"}]
   [:p ""]
   [:div
    (when-let [ss (:trans app-state)]
      [:span (str "\t" " Response, ") [:strong ss]])]
   [:p ""]
   ])
