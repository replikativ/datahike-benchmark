(ns datahike-benchmark.db.api
  (:require [datahike-benchmark.db.interface :as db]
            [datahike-benchmark.db.datahike]
            [datahike-benchmark.db.datomic]
            [datahike-benchmark.db.hitchhiker]))


;;
;; Functions for setup and cleanup
;;

;; Functions from interface


(def ^{:arglists '([lib config])} connect db/connect)

(def ^{:arglists '([lib conn])} transact db/transact)

(def ^{:arglists '([lib conn])} release db/release)

(def ^{:arglists '([lib conn])} db db/db)

(def ^{:arglists '([lib query db])} q db/q)

(def ^{:arglists '([lib config])
       :doc      "Creates a new empty database"} init db/init)


;; Others


(defn init-and-connect [lib config]
  (init lib config)
  (connect lib config))

(defn prepare-db-and-connect [lib config schema tx]
  (let [conn (init-and-connect lib config)]
    (when (or (not= lib :datahike)
              (= :write (get-in config [:dh-config :schema-flexibility])))
      (transact lib conn schema))
    (transact lib conn tx)
    conn))

(defn prepare-db [lib config schema tx]
  (let [conn (prepare-db-and-connect lib config schema tx)]
    (release lib conn)))
