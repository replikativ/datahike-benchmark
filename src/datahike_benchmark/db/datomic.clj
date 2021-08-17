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

(defmethod db/prepare-and-connect :datomic [_ {:keys [uri]} schema tx]
  (d/delete-database uri)
  (d/create-database uri)
  (let [conn (d/connect uri)]
    @(d/transact conn schema)
    @(d/transact conn tx)
    conn))

(defmethod db/delete :datomic [_ {:keys [uri]}]
  (d/delete-database uri))


(defmethod db/connect :datomic-client [_ {:keys [client dat-config]}] (c/connect client dat-config))

(defmethod db/transact :datomic-client [_ conn tx] (c/transact conn {:tx-data tx}))

(defmethod db/release :datomic-client [_ _conn] nil)

(defmethod db/db :datomic-client [_ conn] (c/db conn))

(defmethod db/q :datomic-client [_ query db] (println query) (c/q query db))

(defmethod db/init :datomic-client [_ {:keys [client dat-config]}]
  (c/delete-database client dat-config)
  (c/create-database client dat-config))

(defmethod db/prepare-and-connect :datomic-client [_ {:keys [client dat-config]} schema tx]
  (c/delete-database client dat-config)
  (c/create-database client dat-config)
  (let [conn (c/connect client dat-config)]
    (c/transact conn {:tx-data schema})
    (c/transact conn {:tx-data tx})
    conn))

(defmethod db/delete :datomic-client [_ {:keys [client dat-config]}]
  (c/delete-database client dat-config))

(defmethod db/configs :datomic-client [_]
  [{:lib          :datomic
    :display-name "Datomic Mem"
    :db           :dat-mem
    :uri          "datomic:mem://performance"} ;; not working on connect
   {:lib          :datomic             ;; connection issues, datomic trying to use different port than declared
    :display-name "Datomic Free"
    :db           :dat-free
    :uri          "datomic:free://localhost:4334/performance?password=clojure"}
   {:lib :datomic-client
    :display-name "Datomic Dev"
    :db           :dat-dev
    :client (c/client {:server-type :dev-local
                       :storage-dir "/tmp/performance-datomic"
                       :system "dev"})
    :dat-config {:db-name "performance-datomic"}}
   {:lib :datomic-client
    :display-name "Datomic Dev Mem"
    :db           :dat-dev-mem
    :client (c/client {:server-type :dev-local
                       :storage-dir :mem
                       :system "ci"})
    :dat-config {:db-name "performance-datomic"}}])
