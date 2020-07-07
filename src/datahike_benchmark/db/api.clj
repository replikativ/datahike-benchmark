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

(def ^{:arglists '([lib config args])
       :doc "Creates a new empty database"} init db/init)


;; Others

(defn init-and-connect [lib config & args]
  (init lib config args)
  (connect lib config))

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
