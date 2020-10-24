(ns datahike-benchmark.function-specific-test
  (:require [clojure.test :refer [is deftest]]
            [datahike-benchmark.bench.set-query :as s]
            [datahike-benchmark.db.api :as db]
            [datahike.api :as d]
            [datahike.datom :as dhd]
            [datahike-benchmark.measure.function-specific :as f]))

(def test-config {:display-name "In-Memory (HHT)"
                  :lib          :datahike
                  :db           :dh-mem-hht
                  :dh-config    {:store              {:backend :mem, :dbname "test"}
                                 :index              :datahike.index/hitchhiker-tree
                                 :schema-flexibility :read
                                 :keep-history?      false}})

(deftest test-transaction
  (let [get-av         (fn [datom] [(.-a datom) (.-v datom)])
        test-tx-data   [{:name "test-tx-datom"}]
        schema         [{:ident :name, :valueType :db.type/string, :cardinality :db.cardinality/one}]

        f-args         {:config       test-config
                        :schema       schema
                        :db-datom-gen (fn [] [{:name "test-db-datom"}])
                        :tx-datom-gen (fn [] test-tx-data)}

        setup-function (f/get-setup-fn :transaction :datahike f-args)
        fn-to-measure  (f/get-fn-to-measure :transaction :datahike f-args)
        tear-down-fn   (f/get-tear-down-fn :transaction :datahike f-args)

        [conn tx-data] (setup-function)
        _              (is (= test-tx-data tx-data))
        _              (is (= (set (mapv get-av (d/datoms @conn :eavt)))
                              #{[:name "test-db-datom"]}))

        report         (fn-to-measure [conn tx-data])
        _              (is (= (set (mapv get-av (d/datoms @conn :eavt)))
                              #{[:name "test-db-datom"] [:name "test-tx-datom"]}))
        _              (is (not (nil? report)))]

    (tear-down-fn [conn tx-data] report)))

(deftest test-connection
  (db/prepare-db :datahike test-config [] [])
  (let [f-args         test-config

        setup-function (f/get-setup-fn :connection :datahike f-args)
        fn-to-measure  (f/get-fn-to-measure :connection :datahike f-args)
        tear-down-fn   (f/get-tear-down-fn :connection :datahike f-args)

        unused-args    (setup-function)

        conn           (fn-to-measure unused-args)
        _              (is (not (nil? conn)))]

    (tear-down-fn unused-args conn)))

(deftest test-connection-release
  (db/prepare-db :datahike test-config [] [])
  (let [f-args         test-config

        setup-function (f/get-one-time-setup-fn :connection-release :datahike f-args)
        fn-to-measure  (f/get-fn-to-measure :connection-release :datahike f-args)
        tear-down-fn   (f/get-one-time-tear-down-fn :connection-release :datahike f-args)

        unused-args    (setup-function)

        _              (fn-to-measure unused-args)]

    (tear-down-fn unused-args)))

(deftest test-query
  (let [schema         (s/make-set-query-schema)
        n-entities     10
        entities       (mapv #(s/make-set-query-entity %) (range n-entities))
        conn           (db/prepare-db-and-connect :datahike test-config schema entities)
        db             (db/db :datahike conn)
        test-query     '{:find [(count ?e)] :in [$] :where [[?e :KSEQ 2]]}
        query-gen      #(identity test-query)

        f-args         {:db        db
                        :query-gen query-gen}

        setup-function (f/get-setup-fn :query :datahike f-args)
        fn-to-measure  (f/get-fn-to-measure :query :datahike f-args)
        tear-down-fn   (f/get-tear-down-fn :query :datahike f-args)

        query          (setup-function)
        _              (is (= query test-query))

        unused-args    (fn-to-measure query)]

    (tear-down-fn query unused-args)))

