(ns datahike-benchmark.db.api
  (:require [datahike-benchmark.db.interface :as db]
            [datahike-benchmark.db.datahike]
            [datahike-benchmark.db.datomic]
            [datahike-benchmark.db.hitchhiker]))


;;
;; Functions for setup and cleanup
;;

;; Functions from interface

(defn connect [lib uri] (db/connect lib uri))
(defn transact [lib conn tx] (db/transact lib conn tx))
(defn release [lib conn] (db/release lib conn))
(defn db [lib conn] (db/db lib conn))
(defn q [lib query db] (db/q lib query db))
(defn init [lib uri args] (db/init lib uri args))


;; Others

(defn init-and-connect [lib uri & args]
  (init lib uri args)
  (connect lib uri))

(defn use-uri [config]
  (or (not (= :datahike (:lib config)))
      (= :pg (get-in config [:store :backend]))))

(defn prepare-db-and-connect [lib config schema tx]
  (let [conn (init-and-connect lib (if (use-uri config)
                                     (:uri config)
                                     (:store config))
                               :schema-on-read (:schema-on-read config)
                               :temporal-index (:temporal-index config)
                               :index (:index config))]
    (when (not (:schema-on-read config))
      (transact lib conn schema))
    (transact lib conn tx)
    conn))

(defn prepare-db [lib config schema tx]
  (let [conn (prepare-db-and-connect lib config schema tx)]
    (release lib conn)))
