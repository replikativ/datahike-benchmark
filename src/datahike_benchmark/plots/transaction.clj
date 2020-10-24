(ns datahike-benchmark.plots.transaction
  (:require [com.hypirion.clj-xchart :as ch]
            [datahike-benchmark.config :as c]
            [datahike-benchmark.plots.util :as u]
            [datahike-benchmark.plots.interface :as p]))

(defn create-transaction-tx-size-plots [data resource statistics file-suffix]
  (let [backends (vec (set (map :backend data)))
        backend-colors (zipmap backends c/x-colors)]

    (for [db-size (vec (set (map :db-size data)))]
      (let [new-file-suffix (str file-suffix
                                 "_" (name resource)
                                 "_" (name statistics)
                                 "_db-size-" db-size)
            plot-mapping (->> data
                              (filter #(= (:db-size %) db-size))
                              (map #(u/add-label-and-config-type %))
                              (remove #(= (:config-type %) 3))
                              (group-by :label)
                              (map (fn [[label vals]]
                                     (let [{backend :backend config-type :config-type} (first vals)]
                                       [label
                                        {:x          (map :datoms vals)
                                         :y          (map statistics vals)
                                         :error-bars (map :sd vals)
                                         :style {:marker-type  (get c/x-shapes config-type)
                                                 :marker-color (get backend-colors backend)
                                                 :line-style   (get c/x-strokes config-type)
                                                 :line-color   (get backend-colors backend)}}])))
                              (apply concat)
                              (apply sorted-map)
                              reverse)

            plot (ch/xy-chart plot-mapping
                              {:title            (str "Transaction " (name resource)  " (" (name statistics) ") vs. datoms per transaction \n(with "
                                                      db-size " " (if (= 1 db-size) "datom" "datoms") " in database)")
                               :x-axis           {:title "Number of datoms per transaction"}
                               :y-axis           {:title (str "Transaction " (name resource) " (" (name statistics) " in " (c/unit resource) ")")}
                               :height           800
                               :width            1600
                               :error-bars-color :match-series})]
        [plot new-file-suffix]))))

(defn create-transaction-db-size-plots [data resource statistics file-suffix]
  (let [backends (vec (set (map :backend data)))
        backend-colors (zipmap backends c/x-colors)]
    (for [tx-size (vec (set (map :datoms data)))]
      (let [new-file-suffix (str file-suffix
                                 "_" (name resource)
                                 "_" (name statistics)
                                 "_tx-size-" tx-size)
            plot-mapping (->> data
                              (filter #(= (:datoms %) tx-size))             ;; for new data
                              (map #(u/add-label-and-config-type %))
                              (remove #(= (:config-type %) 3))
                              (group-by :label)
                              (map (fn [[label vals]]
                                     (let [{backend :backend config-type :config-type} (first vals)]
                                       [label
                                        {:x          (map :db-size vals)
                                         :y          (map statistics vals)
                                         :error-bars (map :sd vals)
                                         :style {:marker-type  (get c/x-shapes config-type)
                                                 :marker-color (get backend-colors backend)
                                                 :line-style   (get c/x-strokes config-type)
                                                 :line-color   (get backend-colors backend)}}])))
                              (apply concat)
                              (apply sorted-map)
                              reverse)
            plot (ch/xy-chart plot-mapping
                              {:title            (str "Transaction " (name resource) " (" (name statistics) ") vs. datoms in database \n(with "
                                                      tx-size " " (if (= 1 tx-size) "datom" "datoms") " per transaction)")
                               :x-axis           {:title "Number of datoms in database"}
                               :y-axis           {:title (str "Transaction " (name resource) " (" (name statistics) ") in " (c/unit resource) ")")}
                               :height           800
                               :width            1600
                               :error-bars-color :match-series})]
        [plot new-file-suffix]))))

(defmethod p/create-plots :transaction [_ data resource]
  (concat (create-transaction-tx-size-plots data resource :mean "compare-db-sizes")
          (create-transaction-tx-size-plots data resource :median "compare-db-sizes")
          (create-transaction-db-size-plots data resource :mean "compare-tx-sizes")
          (create-transaction-db-size-plots data resource :median "compare-tx-sizes")))
