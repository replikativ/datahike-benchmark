(ns datahike-benchmark.measure-test
  (:require [clojure.test :refer [is deftest testing]]
            [datahike-benchmark.db.api :as db]
            [datahike-benchmark.config :as c]
            [datahike-benchmark.bench.util :as u]
            [datahike-benchmark.measure.api :as m]))

(deftest test-measurement-methods
  (let [config       (first (db/configs :datahike))
        n-db-datoms  20
        n-tx-datoms  20
        db-datom-gen (u/tx-generator :name :db.type/string n-db-datoms 0)
        tx-datom-gen (u/tx-generator :name :db.type/string n-tx-datoms 0)
        t-iterations 1
        s-iterations 5
        schema       [(u/make-attr :name :db.type/string)]
        fn-args      {:config       config
                      :schema       schema
                      :db-datom-gen db-datom-gen
                      :tx-datom-gen tx-datom-gen}]

    (testing "Time measurement with time macro"
      (let [{:keys [mean]} (time (m/measure :time :simple {} t-iterations :transaction (:lib config) fn-args))]
        (println "Mean time measured:" mean (c/unit :time))
        (is (> mean 0))))

    (testing "Time measurement with criterium library"
      (let [{mean :mean} (time (m/measure :time :criterium {} t-iterations :transaction (:lib config) fn-args))]
        (println "Mean time measured:" mean (c/unit :time))
        (is (> mean 0))))

    (testing "Space measurement with JVM functions"
      (let [{:keys [mean]} (time (m/measure :space :jvm {:time-step 1} s-iterations :transaction (:lib config) fn-args))]
        (println "Mean space measured:" mean (c/unit :space))
        (is (> mean 0))))

    (testing "Space measurement with async-profiler"
      (let [{:keys [mean]} (time (m/measure :space :perf {:space-step 1} 10 :transaction (:lib config) fn-args))]
        (println "Mean space measured:" mean (c/unit :space))
        (is (> mean 0))))))
