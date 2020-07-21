(ns datahike-benchmark.plots.interface)

(defmulti create-plots (fn [lib & _] lib))