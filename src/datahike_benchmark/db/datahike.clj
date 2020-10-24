(ns datahike-benchmark.db.datahike
  (:require [datahike-benchmark.db.interface :as db]
            [datahike.api :as d]
      ;;      [datahike-jdbc.core]
            [datahike-leveldb.core]))

(defmethod db/connect :datahike [_ {:keys [dh-config]}] (d/connect dh-config))

(defmethod db/transact :datahike [_ conn tx] (d/transact conn tx))

(defmethod db/release :datahike [_ conn] (d/release conn))

(defmethod db/db :datahike [_ conn] (d/db conn))

(defmethod db/q :datahike [_ query db] (d/q query db))

(defmethod db/init :datahike [_ {:keys [dh-config]}]
  (d/delete-database dh-config)
  (d/create-database dh-config))
