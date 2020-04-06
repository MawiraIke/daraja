(ns daraja.Keys)

(def queue-timeout-url "http://0cef73d4.ngrok.io/fail")
(def result-url "http://0cef73d4.ngrok.io/success")
(def access-token "")

(def balance-credentials {:access-token access-token
                          :party-a/short-code                                    600741
                          :initiator                                             "Safaricomapi"
                          :security-credential #_(daraja/encode "Safaricom741#") "Safaricom741#"
                          :identifier-type                                       "4"
                          :remarks                                               "Testing Balance"
                          ;; use custom url here, make sure you use ngrok or localtunnel if
                          ;; you are using localhost
                          :queue-timeout-url                                     queue-timeout-url
                          :result-url                                            result-url})

(def b2b-credentials {:access-token access-token
                      :initiator "Safaricomapi"
                      :amount 01
                      :party-a 600741
                      :party-b 600000
                      :account-reference ""
                      ;; use custom url here, make sure you use ngrok or localtunnel if
                      ;; you are using localhost
                      :queue-url "http://0cef73d4.ngrok.io/fail"
                      :result-url "http://0cef73d4.ngrok.io/success"
                      :security-credential ""})


(def b2c-credentials {:access-token access-token
                       :initiator-name "Safaricomapi"
                       :amount 1
                       :sender-party 600741
                       :receiver-party 254708374149
                       ;; use custom url here, make sure you use ngrok or localtunnel if
                       ;; you are using localhost
                       :queue-url "https://90991c74.ngrok.io/fail"
                       :result-url "https://90991c74.ngrok.io/success"
                       :occasion "No Info"
                       :security-credential ""
                       })