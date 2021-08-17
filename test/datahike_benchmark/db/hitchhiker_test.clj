(ns datahike-benchmark.db.hitchhiker-test
  (:require [clojure.test :refer [is deftest testing]]
            [datahike-benchmark.db.api :as db]))

(deftest test-hitchhiker
  (testing "Raw values as entries"
    (let [config (db/config :hht-val)
          conn (db/connect :hitchhiker config)]
      (is (not (nil? conn)))
      (db/release :hitchhiker conn)
      (db/delete :hitchhiker config)))
  (testing "Datoms as entries"
    (let [config (db/config :hht-dat)
          conn (db/connect :hitchhiker config)]
      (is (not (nil? conn)))
      (db/release :hitchhiker conn)
      (db/delete :hitchhiker config))))
