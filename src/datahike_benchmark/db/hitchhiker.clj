(ns datahike-benchmark.db.hitchhiker
  (:require [datahike-benchmark.db.interface :as db]
            [hitchhiker.tree.utils.async :as async]
            [hitchhiker.tree.messaging :as msg]
            [hitchhiker.tree :as tree]))


;; Hitchhiker functions

(def ^:const br 300)                                        ;; same as in datahike
(def ^:const br-sqrt (long (Math/sqrt br)))                 ;; same as in datahike

(def memory (atom {}))

(defn create [config]
  (swap! memory assoc config (async/<?? (tree/b-tree (tree/->Config br-sqrt br (- br br-sqrt))))))

(defn delete [config]
  (swap! memory dissoc config))

(defn insert-many [tree values]
  (async/<??
    (async/reduce<
      (fn [tree val] (msg/insert tree val nil))
      tree
      values)))

(defn entities->datoms [entities]
  (apply concat (map #(map (fn [[k v]] (vector 0 k v :db/add)) %) entities)))

(defn entities->values [entities]
  (apply concat (map #(map second %) entities)) )

(defn entities->nodes [conn entities]
  (if (= (:config conn) "values")
    (entities->values entities)
    (entities->datoms entities)))


;; Multimethods

(defmethod db/connect :hitchhiker [_ config]
  {:tree (get @memory config)
   :config config})

(defmethod db/release :hitchhiker [_ _] nil)

(defmethod db/transact :hitchhiker [_ conn tx]
  (let [new-tree (insert-many (:tree conn) (entities->nodes conn tx))]
    (assoc conn :tree new-tree)))

(defmethod db/init :hitchhiker [_ config _]
  (delete config)
  (create config))



