(ns datahike-benchmark.plots.util)


;; Convenience functions

(defn add-config-type [data-map]
  (cond
    (and (:schema-on-read data-map) (:temporal-index data-map))
    (assoc data-map :config-type 3)

    (and (:schema-on-read data-map) (not (:temporal-index data-map)))
    (assoc data-map :config-type 2)

    (and (not (:schema-on-read data-map)) (:temporal-index data-map) )
    (assoc data-map :config-type 1)

    :else (assoc data-map :config-type 0)))


(defn add-label-and-config-type [data-map]
  (let [data-map-with-type (add-config-type data-map)]
    (case (:config-type data-map-with-type)
      1 (assoc data-map-with-type :label (str (:backend data-map) " with history"))
      2 (assoc data-map-with-type :label (str (:backend data-map) " with schema-on-read"))
      3 (assoc data-map-with-type :label (str (:backend data-map) " with history and schema-on-read"))
      (assoc data-map-with-type :label (:backend data-map)))))
