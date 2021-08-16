(ns datahike-benchmark.db.datascript
  (:require [datahike-benchmark.db.interface :as db]
            [datascript.core :as d]))

(defmethod db/connect :datascript [_ {:keys [ds-config]}] (d/create-conn ds-config))

(defmethod db/transact :datascript [_ conn tx] (d/transact conn tx))

(defmethod db/release :datascript [_ _conn] nil)

(defmethod db/db :datascript [_ conn] (d/db conn))

(defmethod db/q :datascript [_ query db] (d/q query db))

(defmethod db/init :datascript [_ _] nil)

(defn schema->ds-schema [schema]
  (reduce (fn [schema {:keys [db/ident db/cardinality]}]
            (if (= cardinality :db.cardinality/many)
              (assoc schema ident {:db/cardinality :db.cardinality/many})
              schema))
          {}
          schema))

(defmethod db/prepare-and-connect :datascript [_ _config schema tx]
  (let [schema (schema->ds-schema schema)
        conn (d/create-conn schema)]
    (d/transact! conn tx)
    conn))

(defmethod db/delete :datascript  [_ _config]
  nil)
