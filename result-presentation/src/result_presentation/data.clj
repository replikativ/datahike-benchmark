(ns result-presentation.data
  (:require [clojure.string :as s]
            [datahike.api :as d]
            [clojure.java.io :as io]))


(defn read-val [str]                                        ;; read has problems with spaces in strings
  (let [interpreted (read-string str)]
    (if (or (number? interpreted)
            (boolean? interpreted))
      interpreted
      str)))

(defn read-csv [csv]
  (let [raw-data (->> csv
                      slurp
                      (s/split-lines)
                      (map #(map read-val (s/split % #","))))]
    (map #(zipmap (map keyword (first raw-data)) %)
         (rest raw-data))))

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
      1 (assoc data-map-with-type :config "history" :label (str (:backend data-map) " with history"))
      2 (assoc data-map-with-type :config "schema-on-read" :label (str (:backend data-map) " with schema-on-read"))
      3 (assoc data-map-with-type :config "history, schema-on-read" :label (str (:backend data-map) " with history and schema-on-read"))
      (assoc data-map-with-type :config "simple" :label (:backend data-map)))))


(defn get-data-from-file [filename]
  (->> (read-csv filename)
       (mapv add-label-and-config-type)))

(defn get-data [function resource]
  (let [query '[:find  ?e ?a ?v
                :in $ ?function ?resource
                :where [?e ?a ?v]
                [?e :resource ?resource]
                      [?e :function ?function]]
        conn (d/connect "datahike:file:///tmp/output-db")
        ]
    (->> (d/q query @conn function resource)
         (group-by first)
         (vals)
         (map #(into {} (map (fn [[_ a v]] [a v]) %)))
         (mapv add-label-and-config-type))))