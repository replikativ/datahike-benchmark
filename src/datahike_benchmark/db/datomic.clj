(ns datahike-benchmark.db.datomic
  (:require [datahike-benchmark.db.interface :as db]
            [datomic.api :as d]
            [datomic.client.api :as c]))

(defmethod db/connect :datomic [_ {:keys [uri]}] (d/connect uri))

(defmethod db/transact :datomic [_ conn tx] (deref (d/transact conn tx)))

(defmethod db/release :datomic [_ conn] (d/release conn))

(defmethod db/db :datomic [_ conn] (d/db conn))

(defmethod db/q :datomic [_ query db] (d/q query db))

(defmethod db/init :datomic [_ {:keys [uri]}]
  (d/delete-database uri)
  (d/create-database uri))


(defmethod db/connect :datomic-client [_ {:keys [client dat-config]}] (c/connect client dat-config))

(defmethod db/transact :datomic-client [_ conn tx] (deref (c/transact conn tx)))

(defmethod db/release :datomic-client [_ _] nil)

(defmethod db/db :datomic-client [_ conn] (c/db conn))

(defmethod db/q :datomic-client [_ query db] (c/q query db))

(defmethod db/init :datomic-client [_ {:keys [client dat-config]}]
  (c/delete-database client dat-config)
  (c/create-database client dat-config))
