(defproject daraja "0.1.0-SNAPSHOT"
  :description "FIXME: write this!"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url  "http://www.eclipse.org/legal/epl-v10.html"}

  :min-lein-version "2.7.1"

  :dependencies [[org.clojure/clojure "1.9.0"]
                 [org.clojure/clojurescript "1.10.520"]
                 [reagent "0.8.1"]
                 [clojure-mpesa-wrapper "1.0.4-SNAPSHOT"]
                 [com.taoensso/sente "1.15.0"]
                 [aleph "0.4.6"]
                 [compojure "1.6.1"]
                 [cheshire "5.9.0"]
                 [ring "1.7.1"]
                 [ring/ring-defaults "0.3.2"]
                 [bk/ring-gzip "0.3.0"]
                 [ring-cljsjs "0.2.0"]]


  :source-paths ["src/clj" "src/cljs"]

  :aliases {"fig"       ["trampoline" "run" "-m" "figwheel.main"]
            "fig:build" ["trampoline" "run" "-m" "figwheel.main" "-b" "dev" "-r"]
            "fig:min"   ["run" "-m" "figwheel.main" "-O" "advanced" "-bo" "dev"]
            "fig:test"  ["run" "-m" "figwheel.main" "-co" "test.cljs.edn" "-m" "daraja.test-runner"]}

  :profiles {:dev {:dependencies [[com.bhauman/figwheel-main "0.2.3"]
                                  [com.bhauman/rebel-readline-cljs "0.1.4"]]
                   }}
  :main daraja.core
  )

