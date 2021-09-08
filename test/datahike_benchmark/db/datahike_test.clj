(ns datahike-benchmark.db.datahike-test
  (:require [clojure.test :refer [is deftest testing]]
            [datahike-benchmark.db.api :as db]
            [datahike-benchmark.db.datahike :refer [datahike-base-configs]]
            [datahike.api :as d]))

(defn dh-config [db]
  (first (filter #(= db (:db %)) datahike-base-configs)))

(deftest test-datahike
  (testing "In-memory backend (:mem)"
    (testing "Hitchhiker-tree index"
      (let [config (dh-config :dh-mem-hht)
            _ (db/init :datahike config)
            _ (is (d/database-exists? (:dh-config config)))
            conn (db/connect :datahike config)
            _ (is (not (nil? conn)))]
        (db/release :datahike conn)
        (db/delete :datahike config)
        (is (not (d/database-exists? (:dh-config config))))))
    (testing "Persistent set index"
      (let [config (dh-config :dh-mem-set)
            _ (db/init :datahike config)
            _ (is (d/database-exists? (:dh-config config)))
            conn (db/connect :datahike config)
            _ (is (not (nil? conn)))]
        (db/release :datahike conn)
        (db/delete :datahike config)
        (is (not (d/database-exists? (:dh-config config)))))))

  (testing "File backend (:file)"
    (let [config (dh-config :dh-file)
          _ (db/init :datahike config)
          _ (is (d/database-exists? (:dh-config config)))
          conn (db/connect :datahike config)
          _ (is (not (nil? conn)))]
      (db/release :datahike conn)
      (db/delete :datahike config)
      (is (not (d/database-exists? (:dh-config config))))))

  #_(testing "LevelDB backend (:level)"
      (let [config (dh-config :dh-level)
            _ (db/init :datahike config)
            _ (is (d/database-exists? (:dh-config config)))
            conn (db/connect :datahike config)
            _ (is (not (nil? conn)))]
        (db/release :datahike conn)
        (db/delete :datahike config)
        (is (not (d/database-exists? (:dh-config config))))))

  (testing "JDBC backend (:jdbc)"
    #_(testing "Postgres database"
        (let [config (dh-config :dh-psql)
              _ (db/init :datahike config)
              _ (is (d/database-exists? (:dh-config config)))
              conn (db/connect :datahike config)
              _ (is (not (nil? conn)))]
          (db/release :datahike conn)
          (db/delete :datahike config)
          (is (not (d/database-exists? (:dh-config config))))))
    #_(testing "Mysql database"
        (let [config (dh-config :dh-mysql)
              _ (db/init :datahike config)
              _ (is (d/database-exists? (:dh-config config)))
              conn (db/connect :datahike config)
              _ (is (not (nil? conn)))]
          (db/release :datahike conn)
          (db/delete :datahike config)
          (is (not (d/database-exists? (:dh-config config))))))
    (testing "H2 database"
      (let [config (dh-config :dh-h2)
            _ (db/init :datahike config)
            _ (is (d/database-exists? (:dh-config config)))
            conn (db/connect :datahike config)
            _ (is (not (nil? conn)))]
        (db/release :datahike conn)
        (db/delete :datahike config)
        (is (not (d/database-exists? (:dh-config config))))))))
