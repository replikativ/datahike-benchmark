(ns datahike-benchmark.core-test
  (:require [clojure.test :refer [is deftest testing]]
            [datahike-benchmark.config :as c]
            [datahike-benchmark.db.api :as db]
            [datahike.api :as d]))

(defn get-config-for [db]
  (first (filter #(= db (:db %)) c/db-configurations)))

(defn get-dhconfig-for [db]
  (first (filter #(= db (:db %)) c/datahike-base-configs)))

(deftest test-backends
  (testing "In-memory backend (:mem)"
    (testing "Hitchhiker-tree index"
      (let [config (get-dhconfig-for :dh-mem-hht)
            _ (db/init :datahike config)
            _ (is (d/database-exists? (:dh-config config)))
            conn (db/connect :datahike config)
            _ (is (not (nil? conn)))]
        (db/release :datahike conn)
        (db/delete :datahike config)
        (is (not (d/database-exists? (:dh-config config))))))
    (testing "Persistent set index"
      (let [config (get-dhconfig-for :dh-mem-set)
            _ (db/init :datahike config)
            _ (is (d/database-exists? (:dh-config config)))
            conn (db/connect :datahike config)
            _ (is (not (nil? conn)))]
        (db/release :datahike conn)
        (db/delete :datahike config)
        (is (not (d/database-exists? (:dh-config config)))))))

  (testing "File backend (:file)"
    (let [config (get-dhconfig-for :dh-file)
          _ (db/init :datahike config)
          _ (is (d/database-exists? (:dh-config config)))
          conn (db/connect :datahike config)
          _ (is (not (nil? conn)))]
      (db/release :datahike conn)
      (db/delete :datahike config)
      (is (not (d/database-exists? (:dh-config config))))))

  #_(testing "LevelDB backend (:level)"
      (let [config (get-dhconfig-for :dh-level)
            _ (db/init :datahike config)
            _ (is (d/database-exists? (:dh-config config)))
            conn (db/connect :datahike config)
            _ (is (not (nil? conn)))]
        (db/release :datahike conn)
        (db/delete :datahike config)
        (is (not (d/database-exists? (:dh-config config))))))

  (testing "JDBC backend (:jdbc)"
    (testing "Postgres database"
      (let [config (get-dhconfig-for :dh-psql)
            _ (db/init :datahike config)
            _ (is (d/database-exists? (:dh-config config)))
            conn (db/connect :datahike config)
            _ (is (not (nil? conn)))]
        (db/release :datahike conn)
        (db/delete :datahike config)
        (is (not (d/database-exists? (:dh-config config))))))
    (testing "Mysql database"
      (let [config (get-dhconfig-for :dh-mysql)
            _ (db/init :datahike config)
            _ (is (d/database-exists? (:dh-config config)))
            conn (db/connect :datahike config)
            _ (is (not (nil? conn)))]
        (db/release :datahike conn)
        (db/delete :datahike config)
        (is (not (d/database-exists? (:dh-config config))))))
    (testing "H2 database"
      (let [config (get-dhconfig-for :dh-h2)
            _ (db/init :datahike config)
            _ (is (d/database-exists? (:dh-config config)))
            conn (db/connect :datahike config)
            _ (is (not (nil? conn)))]
        (db/release :datahike conn)
        (db/delete :datahike config)
        (is (not (d/database-exists? (:dh-config config))))))))

(deftest test-foreign-libs
  (testing "Datomic library"
    (testing "Datomic in-memory"
      (let [config (get-config-for :dat-mem)]
        (db/init :datomic config)
        (let [conn (db/connect :datomic config)]
          (is (not (nil? conn)))
          (db/release :datomic conn)
          (db/delete :datomic config))))
    (testing "Datomic free"
      (let [config (get-config-for :dat-free)]
        (db/init :datomic config)
        (let [conn (db/connect :datomic config)]
          (is (not (nil? conn)))
          (db/release :datomic conn)
          (db/delete :datomic config))))
    (testing "Datomic Dev"
      (let [config (get-config-for :dat-dev)]
        (db/init :datomic-client config)
        (let [conn (db/connect :datomic-client config)]
          (is (not (nil? conn)))
          (db/release :datomic-client conn)
          (db/delete :datomic-client config)))))

  (testing "Hitchhiker-tree"
    (testing "Raw values as entries"
      (let [config (get-config-for :hht-val)
            conn (db/connect :hitchhiker config)]
        (is (not (nil? conn)))
        (db/release :hitchhiker conn)
        (db/delete :hitchhiker config)))
    (testing "Datoms as entries"
      (let [config (get-config-for :hht-dat)
            conn (db/connect :hitchhiker config)]
        (is (not (nil? conn)))
        (db/release :hitchhiker conn)
        (db/delete :hitchhiker config))))

  (testing "Datascript"
    (let [config (get-config-for :datascript)
          conn (db/connect :datascript config)]
      (is (not (nil? conn)))
      (db/release :datascript conn)))

  (testing "Datalevin"
    (let [config (get-config-for :datalevin)
          conn (db/connect :datalevin config)]
      (is (not (nil? conn)))
      (db/release :datalevin conn))))
