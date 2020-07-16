(ns datahike-benchmark.bench.transaction
  (:require [datahike-benchmark.bench.interface :as b]
            [datahike-benchmark.bench.util :as u]
            [datahike-benchmark.measure.api :as m]
            [datahike-benchmark.config :as c]))


(defn run-combinations
  "Returns observations"
  [configs measure-function resource options]
  (println "Measuring transaction function...")
  (let [{:keys [seed db-datom-count tx-datom-counts]} options
        schema [(u/make-attr :name :db.type/string)]
        [tx-seed db-seed] (repeatedly 2 (u/int-generator seed))

        db-datom-counts (if (= :function-specific db-datom-count)
                          (u/int-linspace 0 1000 11)        ;; for 8192 memory exception
                          db-datom-count)

        tx-datom-counts (if (= :function-specific tx-datom-counts)
                          (assoc (u/int-linspace 0 1000 11) 0 1)
                          tx-datom-counts)

        res (doall (for [n-db-datoms db-datom-counts
                         n-tx-datoms tx-datom-counts
                         {:keys [lib backend schema-on-read temporal-index] :as config} configs
                         :let [context {:datoms         n-tx-datoms
                                        :db-size        n-db-datoms
                                        :backend        backend
                                        :schema-on-read schema-on-read
                                        :temporal-index temporal-index}]]
                     (try
                       (println " TRANSACT: Number of datoms in db:" n-db-datoms)
                       (println "           Number of datoms per transaction:" n-tx-datoms)
                       (println "           Config:" config)
                       (println "           Seed:" seed)

                       (let [db-datom-gen (u/tx-generator :name :db.type/string n-db-datoms db-seed)
                             tx-datom-gen (u/tx-generator :name :db.type/string n-tx-datoms tx-seed)
                             fn-args {:config       config
                                      :schema       schema
                                      :db-datom-gen db-datom-gen
                                      :tx-datom-gen tx-datom-gen}

                             {:keys [median mean sd]} (measure-function lib fn-args)
                             unit (c/unit resource)]

                         (println "  Mean:" mean unit)
                         (println "  Median:" median unit)
                         (println "  Standard deviation:" sd unit)

                         (merge context
                                {:mean   mean
                                 :median median
                                 :sd     sd}))

                       (catch Exception e (u/error-handling e options context))
                       (catch AssertionError e (u/error-handling e options context)))))]
    (remove empty? res)))


(defmethod b/bench :transaction [_ resource method options]
  (println (str "Getting transaction " (name resource)) "...")
  (let [iter (:transaction (:iterations options))
        f (partial m/measure resource method options iter :transaction)]
    (run-combinations (:databases options) f resource options)))
