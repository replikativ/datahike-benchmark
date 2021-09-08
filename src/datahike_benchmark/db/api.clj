(ns datahike-benchmark.db.api
  (:require [datahike-benchmark.db.interface :as db]
            [datahike-benchmark.db.datahike]
            [datahike-benchmark.db.datalevin]
            [datahike-benchmark.db.datascript]
            [datahike-benchmark.db.hitchhiker]))


;;
;; Functions for setup and cleanup
;;

;; Functions from interface


(def ^{:arglists '([lib config])} connect
  db/connect)

(def ^{:arglists '([lib conn])} transact
  db/transact)

(def ^{:arglists '([lib conn])} release
  db/release)

(def ^{:arglists '([lib conn])} db
  db/db)

(def ^{:arglists '([lib query db])} q
  db/q)

(def ^{:arglists '([lib query db])} init
  db/init)

(def ^{:arglists '([lib config schema tx])
       :doc      "Creates a database with given schema and transactions applied"} prepare-db-and-connect
  db/prepare-and-connect)

(def ^{:arglists '([lib config schema tx])
       :doc      "Removes potentially moved files for database"} delete
  db/delete)

(def ^{:arglists '([lib])
       :doc      "Returns implemented configurations for a library"} configs
  db/configs)

;; Others

(defn libs []
  (keys (methods db/configs)))

(defn configurations []
  (->> (libs)
       (map (fn [lib] (configs lib)))
       (apply concat)))

(defn dbs []
  (set (map :db (configurations))))

(defn config [db]
  (first (filter #(= db (:db %)) (configurations))))

(defn prepare-db [lib config schema tx]
  (let [conn (prepare-db-and-connect lib config schema tx)]
    (release lib conn)))
