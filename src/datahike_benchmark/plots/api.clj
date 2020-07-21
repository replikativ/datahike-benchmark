(ns datahike-benchmark.plots.api
  (:require
    [datahike-benchmark.plots.interface :as p]
    [datahike-benchmark.plots.connection]
    [datahike-benchmark.plots.transaction]
    [datahike-benchmark.plots.random-query]))



(defn create-plots [subject data resource] (p/create-plots subject data resource))