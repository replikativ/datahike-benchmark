(ns datahike-benchmark.bench.set-query
  (:require [datahike-benchmark.bench.interface :as b]
            [datahike-benchmark.bench.util :as u]
            [datahike-benchmark.measure.api :as m]
            [datahike-benchmark.db.api :as db]
            [datahike-benchmark.config :as c]))


(def q4-conditions
  [['[?e :K2 1]]
   ['[?e :K100 ?v1]
    '[(< 80 ?v1)]]
   ['[?e :K10000 ?v2]
    '[(< 2000 ?v2)]
    '[(< ?v2 3000)]]
   ['[?e :K5 3]]
   ['(or [?e :K25 11]
         [?e :K25 19])]
   ['[?e :K4 3]]
   ['[?e :K100 ?v5]
    '[(< ?v5 41)]]
   ['[?e :K1000 ?v4]
    '[(< 850 ?v4)]
    '[(< ?v4 950)]]
   ['[?e :K10 7]]
   ['(or [?e :K25 3]
         [?e :K25 4])]])


(def set-queries
  (let [count-query '{:find [(count ?e)] :in [$] :where []}
        sum-query '{:find [(sum ?s)] :with [?e] :in [$] :where []}
        res2-query '{:find [?res1 ?res2] :with [?e] :in [$] :where []}
        res2-count-query '{:find [?res1 ?res2 (count ?e)] :in [$] :where []}] ;; implicit grouping

    (concat
      (map #(hash-map
              :category "Q1"
              :specific (str %)
              :query (update count-query :where conj
                             (conj '[?e] (keyword (str "K" %)) 2)))
           [500000 250000 100000 40000 10000 1000 100 25 10 5 4 2])

      [{:category "Q1"
        :specific "seq"
        :query    '{:find [(count ?e)] :in [$] :where [[?e :KSEQ 2]]}}]

      (map #(hash-map
              :category "Q2a"
              :specific (str %)
              :query (update count-query :where conj
                             '[?e :K2 2]
                             (conj '[?e] (keyword (str "K" %)) 3)))
           [500000 250000 100000 40000 10000 1000 100 25 10 5 4])


      (map #(hash-map
              :category "Q2b"
              :specific (str %)
              :query (update count-query :where conj
                             '[?e :K2 2]
                             (conj '() (conj '[?e] (keyword (str "K" %)) 3) 'not)))
           [500000 250000 100000 40000 10000 1000 100 25 10 5 4])


      (map #(hash-map
              :category "Q3a"
              :specific (str %)
              :query (update sum-query :where conj
                             '[?e :K1000 ?s]
                             '[?e :KSEQ ?v]
                             '[(< 400000 ?v)]
                             '[(< ?v 500000)]               ;; [(< 400000 ?v 500000)] not possible in datomic
                             (conj '[?e] (keyword (str "K" %)) 3)))
           [500000 250000 100000 40000 10000 1000 100 25 10 5 4])

      (map #(hash-map
              :category "Q3b"
              :specific (str %)
              :query (update sum-query :where conj
                             '[?e :K1000 ?s]
                             '[?e :KSEQ ?v]
                             '(or
                                (and [(< 400000 ?v)]
                                     [(< ?v 410000)])
                                (and [(< 420000 ?v)]
                                     [(< ?v 430000)])
                                (and [(< 440000 ?v)]
                                     [(< ?v 450000)])
                                (and [(< 460000 ?v)]
                                     [(< ?v 470000)])
                                (and [(< 480000 ?v)]
                                     [(< ?v 500000)]))
                             (conj '[?e] (keyword (str "K" %)) 3)))
           [500000 250000 100000 40000 10000 1000 100 25 10 5 4])


      (map #(hash-map
              :category "Q4"
              :specific (str % "-" (+ % 2))
              :query (update res2-query :where (partial apply conj)
                             '[?e :KSEQ ?res1]
                             '[?e :K500000 ?res2]
                             (into '[] (apply concat (subvec q4-conditions % (+ % 3))))))
           (range 7))
      (map #(hash-map
              :category "Q4"
              :specific (str % "-" (+ % 4))
              :query (update res2-query :where (partial apply conj)
                             '[?e :KSEQ ?res1]
                             '[?e :K500000 ?res2]
                             (into '[] (apply concat (subvec q4-conditions % (+ % 5))))))
           (range 5))


      (map (fn [[kn1 kn2]] {:category "Q5"
                            :specific (str "(" kn1 " " kn2 ")")
                            :query    (update res2-count-query :where conj
                                              (conj '[?e] (keyword (str "K" kn1)) '?res1)
                                              (conj '[?e] (keyword (str "K" kn2)) '?res2))})
           [[2 100] [4 25] [10 25]])


      (map #(hash-map
              :category "Q6a"
              :specific (str %)
              :query (update count-query :where conj
                             (conj '[?e] (keyword (str "K" %)) 49)
                             '[?e :K250000 ?v1]
                             '[?e2 :K500000 ?v2]
                             '[(= ?v1 ?v2)]))
           [100000 40000 10000 1000 100])

      (map #(hash-map
              :category "Q6b"
              :specific (str %)
              :query (update res2-query :where conj
                             (conj '[?e] (keyword (str "K" %)) 99)
                             '[?e :KSEQ ?res1]
                             '[?e :K250000 ?v1]
                             '[?e2 :K25 19]
                             '[?e2 :KSEQ ?res2]
                             '[?e2 :K500000 ?v2]
                             '[(= ?v1 ?v2)]))
           [40000 10000 1000 100]))))


(defn rand-str [length]
  (let [chars (map char (range 33 127))]
    (apply str (take length (repeatedly #(rand-nth chars))))))


(defn make-set-query-schema []
  (let [int-cols (mapv #(u/make-attr (keyword (str "K" %)) :db.type/long)
                       [500000 250000 100000 40000 10000 1000 100 25 10 5 4 2])
        str-cols (mapv #(u/make-attr (keyword (str "S" %)) :db.type/string)
                       [1 2 3 4 5 6 7 8])]
    (into [(u/make-attr :KSEQ :db.type/long)]
          (concat int-cols str-cols))))


(defn make-set-query-entity [index]
  (let [int-cols (mapv #(vector (keyword (str "K" %)) (long (rand-int %)))
                       [500000 250000 100000 40000 10000 1000 100 25 10 5 4 2])
        first-str-col [:S1 (rand-str 8)]
        str-cols (mapv #(vector (keyword (str "S" %)) (rand-str 20))
                       [2 3 4 5 6 7 8])]
    (into {:KSEQ index}
          (concat int-cols [first-str-col] str-cols))))


(defn run-query-combinations [lib measure-function resource db options db-context]
  (remove empty? (doall (for [{:keys [category specific query]} set-queries
                              :let [context (merge db-context
                                                   {:category category
                                                    :specific specific})]]
                          (try
                            (println "             Query:" category "(" specific ")")

                            (let [fn-args {:db        db
                                           :query-gen #(identity query)}
                                  {:keys [mean median sd]} (measure-function lib fn-args)
                                  unit (c/unit resource)]

                              (println "  Mean:" mean unit)
                              (println "  Median:" median unit)
                              (println "  Standard deviation:" sd unit)

                              (merge context
                                     {:mean
                                          :median
                                      :sd sd}))

                            (catch Exception e (u/error-handling e options context))
                            (catch AssertionError e (u/error-handling e options context)))))))


(defn run-combinations
  "Returns observations"
  [configs measure-function resource options]
  (let [{:keys [entity-count]} options
        set-query-schema (make-set-query-schema)

        entity-counts (if (= :function-specific entity-count)
                        [1000]                              ; use at least 1 Mio, 1000 plausible
                        entity-count)

        res (doall (for [n-entities entity-counts
                         {:keys [lib backend schema-on-read temporal-index] :as config} configs
                         :let [db-context {:backend        backend
                                           :schema-on-read schema-on-read
                                           :temporal-index temporal-index
                                           :entities       n-entities}]]
                     (try
                       (println " SET_QUERY - Number of entities in database:" n-entities)
                       (println "             Config:" config)

                       (let [schema (if schema-on-read [] set-query-schema)
                             entities (mapv #(make-set-query-entity %)
                                            (range n-entities))
                             conn (db/prepare-db-and-connect lib config schema entities)
                             db (db/db lib conn)
                             measurements (run-query-combinations lib measure-function resource db options db-context)]

                         (db/release lib conn)

                         measurements)

                       (catch Exception e (u/error-handling e options db-context))
                       (catch AssertionError e (u/error-handling e options db-context)))))]
    (remove empty? (apply concat res))))


(defmethod b/bench :set-query [_ resource method options]
  (println (str "Getting set-query " (name resource) "..."))
  (let [configs (remove #(= :hitchhiker (:lib %)) (:databases options))
        iter (:query (:iterations options))
        f (partial m/measure resource method options iter :query)]
    (run-combinations configs f resource options)))
