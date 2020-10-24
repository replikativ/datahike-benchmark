(ns datahike-benchmark.plots.random-query
  (:require [com.hypirion.clj-xchart :as ch]
            [datahike-benchmark.plots.util :as u]
            [datahike-benchmark.config :as c]
            [datahike-benchmark.plots.interface :as p]))

(defn create-config-plots [data resource statistics file-suffix]
  (doall (for [n-entities (set (map :entities data))
               n-attr (set (map :n-attr data))
               dtype (set (map :dtype data))
               n-clauses (set (map :n-clauses data))]
           (let [new-file-suffix (str file-suffix
                                      "_" (name resource)
                                      "_" (name statistics)
                                      "_n-entities-" n-entities
                                      "_n-attr-" n-attr
                                      "_dtype-" dtype
                                      "_n-clauses-" n-clauses)

                 backends (vec (set (map :backend data)))
                 backend-colors (zipmap backends c/x-colors)

                 plot-mapping (->> data
                                   (filter #(and (= (:n-attr %) n-attr)
                                                 (= (:entities %) n-entities)
                                                 (= (:n-clauses %) n-clauses)
                                                 (= (:dtype %) dtype)))
                                   (map #(u/add-label-and-config-type %))
                                   (remove #(= (:config-type %) 3))
                                   (group-by :label)
                                   (map (fn [[label vals]]
                                          (let [{backend :backend config-type :config-type} (first vals)]
                                            [label
                                             {:x          (map :n-joins vals)
                                              :y          (map :mean vals)
                                              :error-bars (map :sd vals)
                                              :style {:marker-type  (get c/x-shapes config-type)
                                                      :marker-color (get backend-colors backend)
                                                      :line-style   (get c/x-strokes config-type)
                                                      :line-color   (get backend-colors backend)}}])))
                                   (apply concat)
                                   (apply sorted-map)
                                   reverse)

                 plot (ch/xy-chart plot-mapping
                                   {:title  (str "Query " (name resource) " (" (name statistics) ") vs. joins per query \n(with "
                                                 n-entities " entities with each "
                                                 n-attr " attributes of type "
                                                 dtype " and queries of "
                                                 n-clauses " clauses)")
                                    :x-axis {:title "Number of joins per query"}
                                    :y-axis {:title (str "Query " (name resource) " " (name statistics) " (" (c/unit resource) ")")}
                                    :height 800
                                    :width  1600
                                    :error-bars-color :match-series})]
             [plot new-file-suffix]))))

(defn create-type-plots [data resource statistics file-suffix]
  (doall (for [n-entities (set (map :entities data))
               n-attr (set (map :n-attr data))
               n-clauses (set (map :n-clauses data))]
           (let [new-file-suffix (str file-suffix
                                      "_" (name resource)
                                      "_" (name statistics)
                                      "_n-entities-" n-entities
                                      "_n-attr-" n-attr
                                      "_n-clauses-" n-clauses)

                 backends (vec (set (map :backend data)))
                 backend-colors (zipmap backends c/x-colors)

                 plot-mapping (->> data
                                   (filter #(and (= (:n-attr %) n-attr)
                                                 (= (:entities %) n-entities)
                                                 (= (:n-clauses %) n-clauses)))
                                   (map #(u/add-config-type %))
                                   (filter #(= (:config-type %) 0))
                                   (map #(assoc % :label (str (:backend %) " (" (:dtype %) ")")))
                                   (group-by :label)
                                   (map (fn [[label vals]]
                                          (let [{backend :backend dtype :dtype} (first vals)]
                                            [label
                                             {:x          (map :n-joins vals)
                                              :y          (map :mean vals)
                                              :error-bars (map :sd vals)
                                              :style {:marker-type  (get c/x-shapes (if (= dtype "long") 0 1))
                                                      :marker-color (get backend-colors backend)
                                                      :line-style   (get c/x-strokes (if (= dtype "long") 0 1))
                                                      :line-color   (get backend-colors backend)}}])))
                                   (apply concat)
                                   (apply sorted-map)
                                   reverse)

                 plot (ch/xy-chart plot-mapping
                                   {:title  (str "Query " (name resource) " (" (name statistics) ") vs. joins per query \n(with "
                                                 n-entities " entities with each "
                                                 n-attr " attributes and queries of "
                                                 n-clauses " clauses)")
                                    :x-axis {:title "Number of joins per query"}
                                    :y-axis {:title (str "Query " (name resource) " ("  (name statistics) " in " (c/unit resource) ")")}
                                    :height 800
                                    :width  1600
                                    :error-bars-color :match-series})]
             [plot new-file-suffix]))))

(defn create-n-attr-plots [data resource statistics file-suffix]
  (doall (for [n-entities (set (map :entities data))
               dtype (set (map :dtype data))
               n-clauses (set (map :n-clauses data))]
           (let [new-file-suffix (str file-suffix
                                      "_" (name resource)
                                      "_" (name statistics)
                                      "_n-entities-" n-entities
                                      "_dtype-" dtype
                                      "_n-clauses-" n-clauses)

                 backend-colors (zipmap (set (map :backend data)) c/x-colors)

                 plot-mapping (->> data
                                   (filter #(and (= (:dtype %) dtype)
                                                 (= (:entities %) n-entities)
                                                 (= (:n-clauses %) n-clauses)))
                                   (map #(u/add-config-type %))
                                   (filter #(= (:config-type %) 0))
                                   (map #(assoc % :label (str (:backend %) " (" (:n-attr %) " attributes)")))
                                   (group-by :label)
                                   (map (fn [[label vals]]
                                          (let [{backend :backend n-attr :n-attr} (first vals)]
                                            [label
                                             {:x          (map :n-joins vals)
                                              :y          (map statistics vals)
                                              :error-bars (map :sd vals)
                                              :style {:marker-type  (get c/x-shapes (int (/ n-attr 100)))
                                                      :marker-color (get backend-colors backend)
                                                      :line-style   (get c/x-strokes (int (/ n-attr 100)))
                                                      :line-color   (get backend-colors backend)}}])))
                                   (apply concat)
                                   (apply sorted-map)
                                   reverse)

                 plot (ch/xy-chart plot-mapping
                                   {:title            (str "Query " (name resource) " (" (name statistics) ") vs. joins per query \n(with "
                                                           n-entities " entities with attributes of type "
                                                           dtype " and queries of "
                                                           n-clauses " clauses)")
                                    :x-axis           {:title "Number of joins per query"}
                                    :y-axis           {:title (str "Query " (name resource) "(" (name statistics) ") in " (c/unit resource) ")")}
                                    :height           800
                                    :width            1600
                                    :error-bars-color :match-series})]
             [plot new-file-suffix]))))

(defmethod p/create-plots :random-query [_ data resource]
  (concat
   (create-config-plots data resource :mean "compare-config")
   (create-config-plots data resource :median "compare-config")
   (create-type-plots data resource :mean "compare-type")
   (create-type-plots data resource :median "compare-type")
   (create-n-attr-plots data resource :mean "compare-n-attr")
   (create-n-attr-plots data resource :median "compare-n-attr")))
