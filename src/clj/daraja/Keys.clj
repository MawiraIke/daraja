(ns daraja.Keys)

(def balance-credentials {:party-a/short-code                                    600741
                          :initiator                                             "Safaricomapi"
                          :security-credential #_(daraja/encode "Safaricom741#") "Safaricom741#"
                          :identifier-type                                       "4"
                          :remarks                                               "Testing Balance"
                          ;; use custom url here, make sure you use ngrok or localtunnel if
                          ;; you are using localhost
                          :queue-timeout-url                                     "http://0cef73d4.ngrok.io/fail"
                          :result-url                                            "http://0cef73d4.ngrok.io/success"})