(defproject om-ac-ex "0.1.0-SNAPSHOT"
  :description "FIXME: write this!"
  :url "http://example.com/FIXME"

  :dependencies [[org.clojure/clojure "1.5.1"]
                 [org.clojure/clojurescript "0.0-2138"]
                 [org.clojure/core.async "0.1.267.0-0d7780-alpha"]
                 [om "0.3.6"]
                 [org.clojure/core.match "0.2.1"]
                 [com.facebook/react "0.8.0.1"]]

  :plugins [[lein-cljsbuild "1.0.2"]]

  :source-paths ["src"]

  :cljsbuild { 
    :builds [{:id "om-ac-ex"
              :source-paths ["src"]
              :compiler {
                :output-to "om_ac_ex.js"
                :output-dir "out"
                :optimizations :none
                :source-map true}}]})
