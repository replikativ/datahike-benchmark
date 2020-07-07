(ns datahike-benchmark.bench.interface)

(defmulti bench (fn [function _ _ _] function))
