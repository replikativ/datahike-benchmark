(defproject io.replikativ/datahike-benchmark "0.2.1-SNAPSHOT"
  :description "Measuring of datahike performance"
  :license {:name "Eclipse"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :url "https://github.com/replikativ/datahike-benchmark"

  :main datahike-benchmark.core
  :aot [datahike-benchmark.core]

  :jvm-opts ["-Xmx2g"                                       ;; max 2GB Heap size, default usually 1GB
             "-server"                                      ;; faster operating speed, vs. '-client': smaller memory footprint and faster startup time
             "-Djdk.attach.allowAttachSelf=true"            ;; allow clj-async profiling
             "-XX:+UnlockDiagnosticVMOptions"               ;; make profiling more accurate
             "-XX:+DebugNonSafepoints"                      ;; "-"
             ]

  :dependencies [[org.clojure/clojure "1.10.1"]
                 [org.clojure/data.csv "1.0.0"]

                 [io.replikativ/datahike "0.3.3-SNAPSHOT"   ;:exclusions [io.replikativ/konserve]
                  ]
                 ;[io.replikativ/datahike-jdbc "0.1.1-SNAPSHOT"]
                 ;; [io.replikativ/datahike-leveldb "0.1.0"]
                 [com.datomic/datomic-free "0.9.5697"]

                 [criterium "0.4.5"]
                 [com.clojure-goes-fast/clj-async-profiler "0.4.1"]
                 [com.hypirion/clj-xchart "0.2.0"]]

  :repl-options {:init-ns datahike-benchmark.core})
