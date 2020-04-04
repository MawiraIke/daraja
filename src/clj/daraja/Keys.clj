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
                      :queue-url "http://0cef73d4.ngrok.io/fail"
                      :result-url "http://0cef73d4.ngrok.io/success"
                      :security-credential "p3+NcvWoPqLMJVyz3Rw1JahFE/gtgCwOybzkBduW/Uxk4lQtBqlaN+/iKommW1vCnaW1QYuvso+Hm/wg2IMzFou0XspVTqN4fHB1OT3IturmE/6NlKur9NQWFTH3npyjjz64aZgJEvs4A3RtarYbvJRdDdoCtxYGeupngyJXi8UCiWta+7mkpysv2c3hwKUIZiMwszC4flFrNkjz+Y1do5ywRp0Vsj+DjfdCavsMFcrjbt7U4vcCuRwNRh9FxfmZgJN1/AYeU8Tu1NtD0Png2tSA4362pH3aMppFdqr33oNlTd8S+ttICCDfl4gpcSJ2Lx5FrYE4fHFYq9z233dqDA=="})