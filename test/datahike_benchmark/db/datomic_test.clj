(ns datahike-benchmark.db.datomic-test
  (:require [clojure.test :refer [is deftest testing]]
            [datahike-benchmark.db.api :as db]))


(deftest test-datomic
  (testing "Datomic in-memory"
    (let [config (db/config :dat-mem)]
      (db/init :datomic config)
      (let [conn (db/connect :datomic config)]
        (is (not (nil? conn)))
        (db/release :datomic conn)
        (db/delete :datomic config))))
  (testing "Datomic free"
    (let [config (db/config :dat-free)]
      (db/init :datomic config)
      (let [conn (db/connect :datomic config)]
        (is (not (nil? conn)))
        (db/release :datomic conn)
        (db/delete :datomic config))))
  (testing "Datomic Dev"
    (let [config (db/config :dat-dev)]
      (db/init :datomic-client config)
      (let [conn (db/connect :datomic-client config)]
        (is (not (nil? conn)))
        (db/release :datomic-client conn)
        (db/delete :datomic-client config))))
  (testing "Datomic Dev Mem"
    (let [config (db/config :dat-dev-mem)]
      (db/init :datomic-client config)
      (let [conn (db/connect :datomic-client config)]
        (is (not (nil? conn)))
        (db/release :datomic-client conn)
        (db/delete :datomic-client config)))))
