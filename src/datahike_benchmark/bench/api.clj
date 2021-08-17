(ns datahike-benchmark.bench.api
  (:require
   [datahike-benchmark.bench.interface :as b]
   [datahike-benchmark.bench.connection]
   [datahike-benchmark.bench.transaction]
   [datahike-benchmark.bench.random-query]
   ;[datahike-benchmark.bench.set-query]
   ))

(def ^{:arglists '([function resource method options])
       :doc      "Measures resource of function or code chunk with given method
                        Implemented resource-method pairings:
                        #{[:time :simple]
                          [:time :criterium]
                          [:space :perf]
                          [:space :jvm]}"} bench
  b/bench)

(defn functions []
  (keys (methods b/bench)))
