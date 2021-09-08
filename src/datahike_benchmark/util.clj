(ns datahike-benchmark.util
  (:require [clojure.data.csv :as csv]
            [clojure.java.io :as io]
            [datahike.api :as d]
            [datahike.config :as c]))

(defn order-row-values [header vals]
  (mapv #(get vals %) header))

(defn write-as-csv [data filename]
  (let [header (vec (distinct (apply concat (map keys data))))
        content (concat [(map name header)]
                        (map (partial order-row-values header) data))]
    (with-open [writer (io/writer filename)]
      (csv/write-csv writer content))))

(def schema
  [{:db/ident       :backend
    :db/cardinality :db.cardinality/one
    :db/valueType   :db.type/string}
   {:db/ident       :function
    :db/cardinality :db.cardinality/one
    :db/valueType   :db.type/string}
   {:db/ident       :resource
    :db/cardinality :db.cardinality/one
    :db/valueType   :db.type/string}
   {:db/ident       :schema-flexibility
    :db/cardinality :db.cardinality/one
    :db/valueType   :db.type/keyword}
   {:db/ident       :keep-history?
    :db/cardinality :db.cardinality/one
    :db/valueType   :db.type/boolean}
   {:db/ident       :datoms
    :db/cardinality :db.cardinality/one
    :db/valueType   :db.type/long}
   {:db/ident       :db-size
    :db/cardinality :db.cardinality/one
    :db/valueType   :db.type/long}
   {:db/ident       :entities
    :db/cardinality :db.cardinality/one
    :db/valueType   :db.type/long}
   {:db/ident       :n-attr
    :db/cardinality :db.cardinality/one
    :db/valueType   :db.type/long}
   {:db/ident       :n-ref-attr
    :db/cardinality :db.cardinality/one
    :db/valueType   :db.type/long}
   {:db/ident       :n-clauses
    :db/cardinality :db.cardinality/one
    :db/valueType   :db.type/long}
   {:db/ident       :n-joins
    :db/cardinality :db.cardinality/one
    :db/valueType   :db.type/long}
   {:db/ident       :n-direct
    :db/cardinality :db.cardinality/one
    :db/valueType   :db.type/long}
   {:db/ident       :dtype
    :db/cardinality :db.cardinality/one
    :db/valueType   :db.type/string}
   {:db/ident       :category
    :db/cardinality :db.cardinality/one
    :db/valueType   :db.type/string}
   {:db/ident       :specific
    :db/cardinality :db.cardinality/one
    :db/valueType   :db.type/string}
   {:db/ident       :mean
    :db/cardinality :db.cardinality/one
    :db/valueType   :db.type/float}
   {:db/ident       :median
    :db/cardinality :db.cardinality/one
    :db/valueType   :db.type/float}
   {:db/ident       :sd
    :db/cardinality :db.cardinality/one
    :db/valueType   :db.type/float}])

(defn write-to-db [data function resource]
  (println "Write to database:" function resource)
  (let [config (c/load-config)]
    (println " Using configuration" config)
    (if (not (d/database-exists? config))
      (do (d/create-database config)
          (d/transact (d/connect config) schema)
          (println "Database created: " config))
      (println "Output database exists: " config))
    (let [tx (mapv #(assoc %
                           :function (name function)
                           :resource (name resource))
                   data)
          conn (d/connect config)]
      (d/transact conn tx)
      (d/release conn)
      (d/connect config))))
