(ns datahike-benchmark.core-test
  (:require [clojure.test :refer [is deftest testing]]
            [datahike-benchmark.config :as c]
            [datahike-benchmark.db.api :as db]
            [datahike.api :as d]))

(defn get-first-config-for [db]
  (first (filter #(= db (:db %)) c/db-configurations)))

(defn get-first-dhconfig-for [db]
  (first (filter #(= db (:db %)) c/datahike-base-configs)))

(deftest test-backends
  (testing "In-memory backend (:mem)"
    (testing "Hitchhiker-tree index"
      (let [config (get-first-dhconfig-for :dh-mem-hht)]
        (db/init :datahike config)
        (is (d/database-exists? (:dh-config config)))
        (let [conn (db/connect :datahike config)]
          (is (not (nil? conn)))
          (db/release :datahike conn))))
    (testing "Persistent set index"
      (let [config (get-first-dhconfig-for :dh-mem-set)]
        (db/init :datahike config)
        (is (d/database-exists? config))
        (let [conn (db/connect :datahike config)]
          (is (not (nil? conn)))
          (db/release :datahike conn)))))

  (comment (testing "File backend (:file)"
             (let [config (get-first-dhconfig-for :dh-file)]
               (db/init :datahike config)
               (is (d/database-exists? (:dh-config config)))
               (let [conn (db/connect :datahike config)]
                 (is (not (nil? conn)))
                 (db/release :datahike conn)))))

  (testing "JDBC backend (:jdbc)"
    (testing "Postgres database"
      (let [config (get-first-dhconfig-for :dh-psql)]
        (db/init :datahike config)
        (is (d/database-exists? (:dh-config config)))
        (let [conn (db/connect :datahike config)]
          (is (not (nil? conn)))
          (db/release :datahike conn))))
    (testing "Mysql database"
      (let [config (get-first-dhconfig-for :dh-mysql)]
        (db/init :datahike config)
        (is (d/database-exists? (:dh-config config)))
        (let [conn (db/connect :datahike config)]
          (is (not (nil? conn)))
          (db/release :datahike conn))))
    (testing "H2 database"
      (let [config (get-first-dhconfig-for :dh-h2)]
        (db/init :datahike config)
        (is (d/database-exists? (:dh-config config)))
        (let [conn (db/connect :datahike config)]
          (is (not (nil? conn)))
          (db/release :datahike conn))))))


(deftest test-database-reachability
  (testing "Datomic library"
    (testing "Datomic in-memory"
      (let [config (get-first-config-for :dat-mem)]
        (db/init :datomic config)
        (let [conn (db/connect :datomic config)]
          (is (not (nil? conn)))
          (db/release :datomic conn))))
    (testing "Datomic free"
      (let [config (get-first-config-for :dat-free)]
        (db/init :datomic config)
        (let [conn (db/connect :datomic config)]
          (is (not (nil? conn)))
          (db/release :datomic conn)))))

  (testing "Hitchhiker-tree"
    (testing "Raw values as entries"
      (let [config (get-first-config-for :hht-val)
            conn (db/connect :hitchhiker config)]
        (is (not (nil? conn)))
        (db/release :hitchhiker conn)))
    (testing "Datoms as entries"
      (let [config (get-first-config-for :hht-dat)
            conn (db/connect :hitchhiker config)]
        (is (not (nil? conn)))
        (db/release :hitchhiker conn)))))


