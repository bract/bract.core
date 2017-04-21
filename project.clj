(defproject bract/bract.core "0.1.0-SNAPSHOT"
  :description "Multi-purpose, modular Clojure application framework"
  :url "https://github.com/bract/bract.core"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :global-vars {*warn-on-reflection* true
                *assert* true
                *unchecked-math* :warn-on-boxed}
  :min-lein-version "2.7.1"
  :pedantic? :abort
  :dependencies [[keypin "0.5.0"]
                 [org.clojure/tools.cli "0.3.5"]]
  :java-source-paths ["src-java"]
  :profiles {:provided {:dependencies [[org.clojure/clojure "1.7.0"]]}
             :c17 {:dependencies [[org.clojure/clojure "1.7.0"]]}
             :c18 {:dependencies [[org.clojure/clojure "1.8.0"]]}
             :c19 {:dependencies [[org.clojure/clojure "1.9.0-alpha15"]]}
             :dln {:jvm-opts ["-Dclojure.compiler.direct-linking=true"]}})
