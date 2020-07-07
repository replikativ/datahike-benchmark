(ns datahike-benchmark.db.datahike
  (:require [datahike-benchmark.db.interface :as db]
            [datahike.api :as d]
    ;;  [datahike-redis.core]
            [datahike-postgres.core]
            [datahike-leveldb.core]))


(defmethod db/connect :datahike [_ config] (d/connect config))

(defmethod db/transact :datahike [_ conn tx] (d/transact conn tx))

(defmethod db/release :datahike [_ conn] (d/release conn))

(defmethod db/db :datahike [_ conn] (d/db conn))

(defmethod db/q :datahike [_ query db] (d/q query db))

(defmethod db/init :datahike [_ config args]
  (d/delete-database config)
  (apply d/create-database config args))
