(ns daraja.Keys)

(def queue-timeout-url "http://0cef73d4.ngrok.io/fail")
(def result-url "http://0cef73d4.ngrok.io/success")
(def access-token "")
(def security-credential "")

(def balance-credentials {:access-token        access-token
                          :party-a/short-code  600741
                          :initiator           "Safaricomapi"
                          :security-credential security-credential
                          :identifier-type     "4"
                          :remarks             "Testing Balance"
                          ;; use custom url here, make sure you use ngrok or localtunnel if
                          ;; you are using localhost
                          :queue-timeout-url   queue-timeout-url
                          :result-url          result-url})

(def b2b-credentials {:access-token        access-token
                      :initiator           "Safaricomapi"
                      :amount              01
                      :party-a             600741
                      :party-b             600000
                      :account-reference   ""
                      ;; use custom url here, make sure you use ngrok or localtunnel if
                      ;; you are using localhost
                      :queue-url           "http://0cef73d4.ngrok.io/fail"
                      :result-url          "http://0cef73d4.ngrok.io/success"
                      :security-credential security-credential})


(def b2c-credentials {:access-token        access-token
                      :initiator-name      "Safaricomapi"
                      :amount              1
                      :sender-party        600741
                      :receiver-party      254708374149
                      ;; use custom url here, make sure you use ngrok or localtunnel if
                      ;; you are using localhost
                      :queue-url           "https://90991c74.ngrok.io/fail"
                      :result-url          "https://90991c74.ngrok.io/success"
                      :occasion            "No Info"
                      :security-credential security-credential})

(def c2b-reg {:access-token     access-token
              :short-code       600741
              :response-type    "Canceled"
              ;; use custom url here, make sure you use ngrok or localtunnel if
              ;; you are using localhost
              :confirmation-url "https://bfb436a4.ngrok.io/conf"
              :validation-url   "https://bfb436a4.ngrok.io/valid"})

(def c2b-sim {:access-token    access-token
              :short-code      600741
              :command-id      "CustomerPayBillOnline"
              :amount          1
              :msisdn          254708374149
              :bill-ref-number "174379"})

(def transaction-status {:access-token        access-token
                         :security-credential security-credential
                         :initiator           "Safaricomapi"
                         :command-id          "TransactionStatusQuery"
                         :transaction-id      "LX34LFD354"
                         :party-a             "600741"
                         :identifier-type     "1"
                         ;; use custom url here, make sure you use ngrok or localtunnel if
                         ;; you are using localhost
                         :result-url          "https://8e5b5745.ngrok.io/res"
                         :queue-timeout-url   "https://8e5b5745.ngrok.io/timeout"
                         :remarks             "Transaction Reversal"
                         :occasion            "Transaction Reversal"})

(def lipa-na-mpesa {:access-token            access-token
                    :short-code              174379
                    :transaction-type        "CustomerPayBillOnline"
                    :amount                  1
                    :phone-number            254708374149
                    :callback-url            "https://8e5b5745.ngrok.io/res"
                    :account-reference       "account"
                    :transaction-description "Lipa na Mpesa Online"
                    :passkey                 "bfb279f9aa9bdbcf158e97dd71a467cd2e0c893059b10f78e6b72ada1ed2c919"})
