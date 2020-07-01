(ns datahike-benchmark.bench.connection
  (:require [datahike-benchmark.bench.interface :as b]
            [datahike-benchmark.bench.util :as u]
            [datahike-benchmark.measure.api :as m]
            [datahike-benchmark.db.api :as db]
            [datahike-benchmark.config :as c]))


(defn run-combinations
  "Returns observations"
  [configs measure-function resource options]
  (let [schema [(u/make-attr :name :db.type/string)]

        datom-count (if (= :function-specific (:db-datom-count options))
                      (u/int-linspace 0 5000 21) ;; for 8192 memory exception
                      (:db-datom-count options))

        res (doall (for [db-datom-count datom-count
                         config configs
                         :let [tx (u/create-n-str-transactions :name db-datom-count (:seed options))
                               run-info {:backend        (:name config)
                                         :schema-on-read (:schema-on-read config)
                                         :temporal-index (:temporal-index config)
                                         :datoms         db-datom-count}]]
                     (try
                       (println " CONNECT - Number of datoms in db:" db-datom-count)
                       (println "           Config:" config)
                       (println "           Seed:" (:seed options))
                       (db/prepare-db (:lib config) config schema tx)
                       (let [t (measure-function (:lib config) config)]
                         (println "  Mean:" (:mean t) (c/unit resource))
                         (println "  Median:" (:median t) (c/unit resource))
                         (println "  Standard deviation:" (:sd t) (c/unit resource))
                         (merge run-info (select-keys t [:mean :median :sd])))
                       (catch Exception e (u/error-handling e options run-info))
                       (catch AssertionError e (u/error-handling e options run-info)))))]
    (remove empty? res)))


(defmethod b/bench :connection [_ resource method options]
  (println (str "Getting connection " (name resource) "..."))
  (let [configs (remove #(= :dat-mem (:db %)) (:databases options))
        iter (:connection (:iterations options))
        f (if (or (= method :profiler)
                  (= method :criterium))
            (partial m/measure resource method options iter :connection-release) ;; allows iterations without setting up new stuff before measurement
            (partial m/measure resource method options iter :connection))]
    (run-combinations configs f resource options)))
