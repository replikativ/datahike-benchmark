(ns datahike-benchmark.bench.connection
  (:require [datahike-benchmark.bench.interface :as b]
            [datahike-benchmark.bench.util :as u]
            [datahike-benchmark.measure.api :as m]
            [datahike-benchmark.db.api :as db]
            [datahike-benchmark.config :as c]))

(defn run-combinations
  "Returns observations"
  [configs measure-function resource options]
  (let [{:keys [seed db-datom-count]} options
        schema       [(u/make-attr :name :db.type/string)]

        datom-counts (if (= :function-specific db-datom-count)
                       (u/int-linspace 0 5000 21)           ;; for 8192 memory exception
                       db-datom-count)
        _ (println "dc" datom-counts)

        res          (doall (for [db-datoms datom-counts
                                  {:keys [lib display-name] :as config} configs
                                  :let [tx                 (u/create-n-str-transactions :name db-datoms seed)
                                        schema-flexibility (get-in config [:dh-config :schema-flexibility] c/default-schema-flexibility)
                                        keep-history?      (get-in config [:dh-config :keep-history?] c/default-keep-history?)
                                        context            {:backend            display-name
                                                            :schema-flexibility schema-flexibility
                                                            :keep-history?      keep-history?
                                                            :datoms             db-datoms}]]
                              (try
                                (println " CONNECT - Number of datoms in db:" db-datoms)
                                (println "           Config:" config)

                                (db/prepare-db lib config schema tx)

                                (let [{:keys [mean median sd]} (measure-function lib config)
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

(defmethod b/bench :connection [_ resource method options]
  (println (str "Getting connection " (name resource) "..."))
  (let [configs (remove #(= :dat-mem (:db %)) (:databases options))
        iter    (:connection (:iterations options))
        f       (if (or (= method :perf)
                        (= method :criterium))
                  (partial m/measure resource method options iter :connection-release) ;; allows iterations without setting up new stuff before measurement
                  (partial m/measure resource method options iter :connection))]
    (run-combinations configs f resource options)))
