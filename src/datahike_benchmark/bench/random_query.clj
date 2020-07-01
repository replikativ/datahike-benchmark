(ns datahike-benchmark.bench.random-query
  (:require [datahike-benchmark.bench.interface :as b]
            [datahike-benchmark.bench.util :as u]
            [datahike-benchmark.db.api :as db]
            [datahike-benchmark.measure.api :as m]
            [datahike-benchmark.config :as c]
            [clojure.string :refer [split]]))


(defn add-join-clauses [initial-query n-joins n-attr shuffle-fn]
  (let [ref-names (map #(keyword (str "R" %)) (take n-joins (shuffle-fn (range n-attr))))
        attr-names (map #(keyword (str "A" %)) (take n-joins (shuffle-fn (range n-attr))))
        ref-symbols (map #(symbol (str "?r" %)) (take n-joins (range n-attr)))
        attr-symbols (map #(symbol (str "?rres" %)) (take n-joins (range n-attr)))
        join-clauses (map (fn [a ref] (conj '[?e] a ref)) ref-names ref-symbols)
        attr-clauses (map (fn [ref a v] (conj '[] ref a v)) ref-symbols attr-names attr-symbols)]
    (reduce (fn [query [res join-clause attr-clause]]
              (-> query
                  (update :find conj res)
                  (update :where conj join-clause)
                  (update :where conj attr-clause)))
            initial-query
            (map vector attr-symbols join-clauses attr-clauses))))


(defn add-direct-clauses [initial-query n-clauses n-attr shuffle-fn]
  (let [attr-names (map #(keyword (str "A" %)) (take n-clauses (shuffle-fn (range n-attr))))
        attr-symbols (map #(symbol (str "?ares" %)) (take n-clauses (range n-attr)))
        attr-clauses (map (fn [a v] (conj '[?e] a v)) attr-names attr-symbols)]
    (reduce (fn [query [res attr-clause]]
              (-> query
                  (update :find conj res)
                  (update :where conj attr-clause)))
            initial-query
            (map vector attr-symbols attr-clauses))))

(defn create-query
  "Assumes database with entities of m direct and m reference attributes"
  [n-direct-vals n-ref-vals m shuffle-seed]
  (let [shuffle-fn (u/shuffle-generator shuffle-seed)
        initial-query '{:find [?e] :where []}]
    (-> initial-query
        (add-direct-clauses n-direct-vals m shuffle-fn)
        (add-join-clauses n-ref-vals m shuffle-fn))))

(defn make-hom-schema
  "Creates homogeneous database schema with attributes of a single type"
  [ident-prefix type cardinality n-attributes]
  (mapv (fn [i] (u/make-attr (keyword (str ident-prefix i)) type cardinality))
        (range n-attributes)))


(defn make-entity [base-map ident-prefix n-attributes max-attribute value-list shuffle-fn]
  (into base-map
        (map (fn [i v] [(keyword (str ident-prefix i)) v])
             (take n-attributes (shuffle-fn (range max-attribute)))
             value-list)))

(defn create-value-ref-db
  "Creates db of
   - 2n+1 different attributes (m <= n)
     - n attributes of given type
     - n reference attributes
     - 1 attribute to identify non-schema entities
   - e entities with
     - 2m+1 values (m <= e)
       - m direct attributes
       - m reference attributes
       - 1 attribute identifying it as non-schema entity"
  ([uri type n e seed]                          ;; n=m -> entities have all possible attributes
   (create-value-ref-db uri type n n e seed))
  ([uri type n m e seed]
   (println "Set up database:" uri)
   (println " Type:" type)
   (println " Number of different attributes:" (inc (* 2 n)))
   (println " - thereof reference attributes:" n)
   (println " Number of attributes per entity:" (inc (* 2 m)))
   (println " - thereof reference attributes:" m)
   (println " Number of entities:" e)
   (time
     (let [shuffle-fn (u/shuffle-generator seed)
           schema (into [(u/make-attr :randomEntity :db.type/boolean)]
                        (concat (make-hom-schema "A" type :db.cardinality/one n)
                                (make-hom-schema "R" :db.type/ref :db.cardinality/one n)))
           entities (mapv (fn [_] (make-entity {:randomEntity true} "A" m n
                                               (repeatedly m (u/data-generator type seed))
                                               shuffle-fn))
                          (range e))
           conn (db/prepare-db-and-connect (:lib uri) uri schema entities)
           ids (map first (db/q (:lib uri)
                                '[:find ?e :where [?e :randomEntity]]
                                (db/db (:lib uri) conn)))
           add-to-entity (mapv (fn [id] (make-entity {:db/id id} "R" m n
                                                     (take m (shuffle-fn (filter #(not= id %) ids)))
                                                     shuffle-fn))
                               ids)]
       (db/transact (:lib uri) conn add-to-entity)
       (print "   ")
       conn))))

(defn run-query-combinations [uri measure-function resource db n-ref-attr query-seed options db-info]
  (remove empty? (doall (for [n-clauses [(* 2 (min n-ref-attr 5))] ;; i.e. max 10 clauses
                            n-joins (range (inc n-clauses))
                            :let [n-direct-clauses (- n-clauses n-joins)
                                  run-info (merge db-info
                                                  {:n-clauses n-clauses
                                                   :n-joins   n-joins
                                                   :n-direct  n-direct-clauses})]]
                        (try
                          (println "                Clauses with joins:" n-joins "out of" n-clauses "clauses")
                          (let [fn-args {:db db :query-gen #(create-query n-direct-clauses n-joins n-ref-attr query-seed)}
                                t (measure-function (:lib uri) fn-args)]
                            (println "   Mean:" (:mean t) (c/unit resource))
                            (println "   Median:" (:median t) (c/unit resource))
                            (println "   Standard deviation:" (:sd t) (c/unit resource))
                            (merge run-info (select-keys t [:mean :median :sd])))
                          (catch Exception e (u/error-handling e options run-info))
                          (catch AssertionError e (u/error-handling e options run-info)))))))


(defn run-combinations
  "Returns observations"
  [configs measure-function resource options]
  (let [[db-seed query-seed] (repeatedly 2 (u/int-generator (:seed options)))

        entity-count (if (= :function-specific (:entity-count options))
                     [100]    ;; use at least 1 Mio, but takes too long, 100 ok, 1000 to much
                     (:entity-count options))

        ref-attr-count (if (= :function-specific (:ref-attr-count options))
                         [5 50 100]    ;; until?
                         (:ref-attr-count options))

        res (doall (for [n-entities entity-count
                         n-ref-attr ref-attr-count
                         type [:db.type/long :db.type/string]
                         config configs
                         :let [n-attr (+ 1 (* 2 n-ref-attr))
                               dtype (last (split (str type) #"/"))
                               db-info {:backend (:name config)
                                        :schema-on-read (:schema-on-read config)
                                        :temporal-index (:temporal-index config)
                                        :entities n-entities
                                        :n-attr n-attr
                                        :n-ref-attr n-ref-attr
                                        :dtype dtype}]]
                     (try
                       (println " RANDOM-QUERY - Data type:" dtype)
                       (println "                Attributes per entity:" n-attr)
                       (println "                Number of entities in database:" n-entities)
                       (println "                Config:" config)
                       (println "                Seed:" (:seed options))
                       (let [conn (create-value-ref-db config type n-ref-attr n-entities db-seed)
                             db (db/db (:lib config) conn)
                             query-measurements (run-query-combinations config measure-function resource db n-ref-attr query-seed options db-info)]
                         (db/release (:lib config) conn)
                         query-measurements)
                       (catch Exception e (u/error-handling e options db-info))
                       (catch AssertionError e (u/error-handling e options db-info)))))]
    (remove empty? (apply concat res))))


(defmethod b/bench :random-query [_ resource method options]
  (println (str "Getting random-query " (name resource) "..."))
  (let [configs (remove #(= :hitchhiker (:lib %)) (:databases options))
        iter (:query (:iterations options))
        f (partial m/measure resource method options iter :query)]
    (run-combinations configs f resource options)))


#_(let [options {:not-write-to-db false,
               :time-only false,
               :space-only false,
               :space-step 5,
               :not-save-plots false,
               :use-criterium false,
               :crash-on-error true,
               :not-save-data false,
               :function "random-query",
               :seed 1092967482,
               :time-step 5,
               :use-java false,
               :iterations {:connection 50, :transaction 10, :query 10}}

      f (partial m/measure :time :core.time options 200 :query)]
  (doall (run-combinations [{:lib            :datahike
                             :name           "In-Memory (HHT)"
                             :uri            "datahike:mem://performance-hht"
                             :schema-on-read false :temporal-index false
                             :store          {:backend :mem :path "performance-hht"} :index :datahike.index/hitchhiker-tree}]
                           ;;(concat c/hitchhiker-configs c/db-configurations)
                           f :time options)))