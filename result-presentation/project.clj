(defproject result-presentation "0.1.0-SNAPSHOT"
            :description "Creation of a document showing results of the benchmarking"
            :main result-presentation.core
            :aot [result-presentation.core]
            :license {:name "Eclipse"
                      :url "http://www.eclipse.org/legal/epl-v10.html"}
            :url "https://github.com/replikativ/datahike-presentation"
            :dependencies [[org.clojure/clojure "1.10.1"]
                           [io.replikativ/datahike "0.2.2-SNAPSHOT"]
                           [metasoarous/oz "1.6.0-alpha6"]]
            :repl-options {:init-ns result-presentation.core})
