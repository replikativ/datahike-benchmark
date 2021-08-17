(ns datahike-benchmark.plots.api
  (:require
   [datahike-benchmark.plots.interface :as p]
   [datahike-benchmark.plots.connection]
   [datahike-benchmark.plots.transaction]
   [datahike-benchmark.plots.random-query]))

(def ^{:arglists '([subject data resource])} create-plots
  p/create-plots)