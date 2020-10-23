(ns datahike-benchmark.plots.util)


;; Convenience functions

(defn add-config-type [data-map]
  (cond
    (and (= :read (:schema-flexibility data-map)) (:keep-history? data-map))
    (assoc data-map :config-type 3)

    (and (= :read (:schema-flexibility data-map)) (not (:keep-history? data-map)))
    (assoc data-map :config-type 2)

    (and (= :write (:schema-flexibility data-map)) (:keep-history? data-map) )
    (assoc data-map :config-type 1)

    :else (assoc data-map :config-type 0)))


(defn add-label-and-config-type [data-map]
  (let [data-map-with-type (add-config-type data-map)]
    (case (:config-type data-map-with-type)
      3 (assoc data-map-with-type :label (str (:backend data-map) " with history and schema-on-read"))
      2 (assoc data-map-with-type :label (str (:backend data-map) " with schema-on-read"))
      1 (assoc data-map-with-type :label (str (:backend data-map) " with history"))
      (assoc data-map-with-type :label (:backend data-map)))))
