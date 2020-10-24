(ns datahike-benchmark.util
  (:require [clojure.data.csv :as csv]
            [clojure.java.io :as io]
            [datahike.api :as d]))

(defn order-row-values [header vals]
  (mapv #(get vals %) header))

(defn write-as-csv [data filename]
  (let [header (vec (distinct (apply concat (map keys data))))
        content (concat [(map name header)]
                        (map (partial order-row-values header) data))]
    (with-open [writer (io/writer filename)]
      (csv/write-csv writer content))))

(defn write-to-db [data function resource uri]
  (println "Write to db: " uri function resource)
  (if (not (d/database-exists? uri))
    (do (d/create-database uri :schema-on-read true)
        (println "Database created: " uri))
    (println "Output database exists: " uri))
  (let [tx (mapv #(assoc % :function (name function)
                         :resource (name resource))
                 data)
        conn (d/connect uri)]
    (d/transact conn tx)))
