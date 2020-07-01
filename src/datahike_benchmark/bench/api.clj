(ns datahike-benchmark.bench.api
  (:require
    [datahike-benchmark.bench.interface :as b]
    [datahike-benchmark.bench.connection]
    [datahike-benchmark.bench.transaction]
    [datahike-benchmark.bench.random-query]
    [datahike-benchmark.bench.set-query]))


(defn bench
  "Measures resource of function or code chunk with given method
  Implemented resource-method pairings:
    #{[:time :core.time]
      [:time :criterium]
      [:space :profiler]
      [:space :java]}"
  [function resource method options]
  (b/bench function resource method options))
