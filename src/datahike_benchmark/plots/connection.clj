(ns datahike-benchmark.plots.connection
  (:require [com.hypirion.clj-xchart :as ch]
            [datahike-benchmark.plots.interface :as p]
            [datahike-benchmark.plots.util :as u]
            [datahike-benchmark.config :as c]))

(defn create-summary-plot [data resource statistics file-suffix]
  (let [backends (vec (set (map :backend data)))
        backend-colors (zipmap backends c/x-colors)

        new-file-suffix (str file-suffix
                             "_" (name resource)
                             "_" (name statistics))

        plot-mapping (->> data
                          (map #(u/add-label-and-config-type %))
                          (remove #(= (:config-type %) 3))
                          (group-by :label)
                          (map (fn [[label vals]]
                                 (let [{:keys [backend config-type]} (first vals)]
                                   [label
                                    {:x          (map :datoms vals)
                                     :y          (map statistics vals)
                                     :error-bars (map :sd vals)
                                     :style      {:marker-type  (get c/x-shapes config-type)
                                                  :marker-color (get backend-colors backend)
                                                  :line-style   (get c/x-strokes config-type)
                                                  :line-color   (get backend-colors backend)}}])))
                          (apply concat)
                          (apply sorted-map)
                          reverse)

        plot (ch/xy-chart plot-mapping
                          {:title            (str "Connection " (name resource) " (" (name statistics) ") vs. datoms in database")
                           :x-axis           {:title "Number of datoms in database"}
                           :y-axis           {:title (str "Connection " (name resource) " (" (name statistics) ") in " (c/unit resource) ")")}
                           :height           800
                           :width            1600
                           :error-bars-color :match-series})]
    [plot new-file-suffix]))

(defmethod p/create-plots :connection [_ data resource]
  [(create-summary-plot data resource :mean "SUMMARY")
   (create-summary-plot data resource :median "SUMMARY")])
