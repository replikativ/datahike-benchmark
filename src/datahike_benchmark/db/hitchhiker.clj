(ns datahike-benchmark.db.hitchhiker
  (:require [datahike-benchmark.db.interface :as db]
            [hitchhiker.tree.utils.async :as async]
            [hitchhiker.tree.messaging :as msg]
            [hitchhiker.tree :as tree]))


;; Hitchhiker functions

(def ^:const br 300)                                        ;; same as in datahike
(def ^:const br-sqrt (long (Math/sqrt br)))                 ;; same as in datahike

(def memory (atom {}))

(defn create-tree [type]
  (swap! memory assoc type (async/<?? (tree/b-tree (tree/->Config br-sqrt br (- br br-sqrt))))))

(defn delete-tree [type]
  (swap! memory dissoc type))

(defn insert-many [tree values]
  (async/<??
    (async/reduce<
      (fn [tree val] (msg/insert tree val nil))
      tree
      values)))

(defn entities->datoms [entities]
  (apply concat (map #(map (fn [[k v]] (vector 0 k v :db/add)) %) entities)))

(defn entities->values [entities]
  (apply concat (map #(map second %) entities)))

(defn entities->nodes [conn entities]
  (if (= (:type conn) :values)
    (entities->values entities)
    (entities->datoms entities)))


;; Multimethods

(defmethod db/connect :hitchhiker [_ {:keys [tree-type]}]
  {:tree   (get @memory type)
   :type tree-type})

(defmethod db/release :hitchhiker [_ _] nil)

(defmethod db/transact :hitchhiker [_ conn tx]
  (let [new-tree (insert-many (:tree conn) (entities->nodes conn tx))]
    (assoc conn :tree new-tree)))

(defmethod db/init :hitchhiker [_ {:keys [tree-type]}]
  (delete-tree tree-type)
  (create-tree tree-type))



