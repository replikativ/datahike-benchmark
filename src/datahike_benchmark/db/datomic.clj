(ns datahike-benchmark.db.datomic
  (:require [datahike-benchmark.db.interface :as db]
            [datomic.api :as d]))


(defmethod db/connect :datomic [_ {:keys [uri]}] (d/connect uri))

(defmethod db/transact :datomic [_ conn tx] (deref (d/transact conn tx)))

(defmethod db/release :datomic [_ conn] (d/release conn))

(defmethod db/db :datomic [_ conn] (d/db conn))

(defmethod db/q :datomic [_ query db] (d/q query db))

(defmethod db/init :datomic [_ {:keys [uri]}]
  (d/delete-database uri)
  (d/create-database uri))
