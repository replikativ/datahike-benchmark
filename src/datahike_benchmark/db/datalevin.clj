(ns datahike-benchmark.db.datalevin
  (:require [datahike-benchmark.db.interface :as db]
            [datalevin.core :as d]
            [datalevin.util :as u]))

(defmethod db/connect :datalevin [_ {:keys [path]}] (d/get-conn path))

(defmethod db/transact :datalevin [_ conn tx] (d/transact conn tx))

(defmethod db/release :datalevin [_ conn] (d/close conn))

(defmethod db/db :datalevin [_ conn] (d/db conn))

(defmethod db/q :datalevin [_ query db] (d/q query db))

(defmethod db/init :datalevin [_ _] nil)

(defn schema->ds-schema [schema]
  (reduce (fn [schema {:keys [db/ident db/cardinality]}]
            (if (= cardinality :db.cardinality/many)
              (assoc schema ident {:db/cardinality :db.cardinality/many})
              schema))
          {}
          schema))

(defmethod db/prepare-and-connect :datalevin [_ {:keys [path]} schema tx]
  (let [schema (schema->ds-schema schema)
        conn (d/get-conn path schema)]
    (d/transact! conn tx)
    conn))

(defmethod db/delete :datalevin  [_ {:keys [path]}]
   (u/delete-files path))
