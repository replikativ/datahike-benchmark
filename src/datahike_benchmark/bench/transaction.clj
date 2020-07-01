(ns datahike-benchmark.bench.transaction
  (:require [datahike-benchmark.bench.interface :as b]
            [datahike-benchmark.bench.util :as u]
            [datahike-benchmark.measure.api :as m]
            [datahike-benchmark.config :as c]))


(defn run-combinations
  "Returns observations"
  [configs measure-function resource options]
  (println "Measuring transaction function...")
  (let [schema [(u/make-attr :name :db.type/string)]
        [tx-seed db-seed] (repeatedly 2 (u/int-generator (:seed options)))

        db-datom-counts (if (= :function-specific (:db-datom-count options))
                          (u/int-linspace 0 1000 11) ;; for 8192 memory exception
                          (:db-datom-count options))

        tx-datom-counts (if (= :function-specific (:tx-datom-count options))
                          (assoc (u/int-linspace 0 1000 11) 0 1)
                          (:tx-datom-count options))

        res (doall (for [db-datom-count db-datom-counts
                         tx-datom-count tx-datom-counts
                         config configs
                         :let [run-info {:backend        (:name config)
                                         :schema-on-read (:schema-on-read config)
                                         :temporal-index (:temporal-index config)
                                         :datoms tx-datom-count
                                         :db-size db-datom-count}]]
                     (try
                       (println " TRANSACT: Number of datoms in db:" db-datom-count)
                       (println "           Number of datoms per transaction:" tx-datom-count)
                       (println "           Config:" config)
                       (println "           Seed:" (:seed options))
                       (let [db-datom-gen (u/tx-generator :name :db.type/string db-datom-count db-seed)
                             tx-datom-gen (u/tx-generator :name :db.type/string tx-datom-count tx-seed)
                             fn-args {:config config :schema schema :db-datom-gen db-datom-gen :tx-datom-gen tx-datom-gen}
                             t (measure-function (:lib config) fn-args)]
                         (println "  Mean:" (:mean t) (c/unit resource))
                         (println "  Median:" (:median t) (c/unit resource))
                         (println "  Standard deviation:" (:sd t) (c/unit resource))
                         (merge run-info (select-keys t [:mean :median :sd])))
                       (catch Exception e (u/error-handling e options run-info))
                       (catch AssertionError e (u/error-handling e options run-info)))))]
    (remove empty? res)))


(defmethod b/bench :transaction [_ resource method options]
  (println (str "Getting transaction " (name resource)) "...")
  (let [iter (:transaction (:iterations options))
        f (partial m/measure resource method options iter :transaction)]
    (run-combinations (:databases options)                  ;;(concat c/hitchhiker-configs c/db-configurations)
                      f resource options)))

#_(let [options {:not-write-to-db false,
               :time-only false,
               :space-only false,
               :space-step 5,
               :not-save-plots false,
               :use-criterium false,
               :crash-on-error true,
               :not-save-data false,
               :function "transaction",
               :seed 1092967482,
               :time-step 5,
               :use-java false,
               :iterations {:connection 50, :transaction 10, :query 10}}

      f (partial m/measure :time :core.time options 1 :transaction)]
  (doall (run-combinations [{:lib            :datahike
                             :name           "In-Memory (HHT)"
                             :uri            "datahike:mem://performance-hht"
                             :schema-on-read false :temporal-index false
                             :store          {:backend :mem :path "performance-hht"} :index :datahike.index/hitchhiker-tree}]
                           ;;(concat c/hitchhiker-configs c/db-configurations)
                           f :time options)))