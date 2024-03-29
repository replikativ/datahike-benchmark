(ns datahike-benchmark.measure.function-specific
  (:require [datahike-benchmark.db.api :as db]))

;;
;; Subject specific functions called by datahike-benchmark.measure/measure
;;
;; - f-to-measure takes the result of setup-fn or one-time-setup-fn as argument
;; - setup-fn is called in each iteration before the function to measure
;; - tear-down-fn is called after each iteration if possible and takes the result of
;;   setup-fn as first argument and the result of f-to-measure as second argument
;; Depending on the measuring function, setup etc. on each iteration is not possible, only
;; a single setup for all iterations. Then, setup-fn and tear-down-fn are replaced by
;; - one-time-setup-fn (called only once before all iterations)
;; - one-time-tear-down-fn (called only once after all iterations and takes the result of
;;   setup-fn as single argument)
;;
;; Two call principles of datahike-benchmark.measure/measure:
;;
;;        (def samples
;;          (dotimes [i iterations]
;;            (let [args (setup-fn)
;;                  res (f-to-measure args)]
;;              (tear-down-fn args res))
;;
;;  or if new setup not possible for each iteration:
;;
;;        (def samples
;;          (let [args (setup-fn)]
;;            (dotimes [i iterations]
;;              (f-to-measure args))
;;            (tear-down-fn args)))
;;

(defmulti get-setup-fn (fn [function _ _] function))
(defmulti get-one-time-setup-fn (fn [function _ _] function))
(defmulti get-fn-to-measure (fn [function _ _] function))
(defmulti get-tear-down-fn (fn [function _ _] function))
(defmulti get-one-time-tear-down-fn (fn [function _ _] function))


;; Connection


(defmethod get-setup-fn :connection [_ _ _]
  (fn [] nil))

(defmethod get-fn-to-measure :connection [_ lib config]
  (fn [_] (db/connect lib config)))

(defmethod get-tear-down-fn :connection [_ lib _]
  (fn [_ conn]
    (db/release lib conn)))


;; Connection with release


(defmethod get-one-time-setup-fn :connection-release [_ _ _]
  (fn [] nil))

(defmethod get-fn-to-measure :connection-release [_ lib config]
  (fn [_] (db/release lib (db/connect lib config))))

(defmethod get-one-time-tear-down-fn :connection-release [_ _ _]
  (fn [_] nil))


;; Transaction


(defn prepare-transaction-measurements [{:keys [lib] :as config} schema db-datoms tx-datoms]
  (let [conn (db/prepare-db-and-connect lib config schema db-datoms)]
    [conn tx-datoms]))

(defmethod get-setup-fn :transaction [_ _ {:keys [config schema db-datom-gen tx-datom-gen]}]
  (fn [] (prepare-transaction-measurements config schema (db-datom-gen) (tx-datom-gen))))   ; needed here so db does not fill up more with each iteration

(defmethod get-one-time-setup-fn :transaction [_ _ {:keys [config schema db-datom-gen tx-datom-gen]}]
  (fn [] (prepare-transaction-measurements config schema (db-datom-gen) (tx-datom-gen))))

(defmethod get-fn-to-measure :transaction [_ lib _]
  (fn [[conn tx-data]] (db/transact lib conn tx-data)))

(defmethod get-tear-down-fn :transaction [_ lib _]
  (fn [[conn _] _] (db/release lib conn)))

(defmethod get-one-time-tear-down-fn :transaction [_ lib _]
  (fn [[conn _]] (db/release lib conn)))


;; Query


(defmethod get-setup-fn :query [_ _ {:keys [query-gen]}]
  (fn [] (query-gen)))

(defmethod get-one-time-setup-fn :query [_ _ {:keys [query-gen]}]
  (fn [] (query-gen)))

(defmethod get-fn-to-measure :query [_ lib {:keys [db]}]
  (fn [query] (db/q lib query db)))

(defmethod get-tear-down-fn :query [_ _ _]
  (fn [_ _] nil))

(defmethod get-one-time-tear-down-fn :query [_ _ _]
  (fn [_] nil))
