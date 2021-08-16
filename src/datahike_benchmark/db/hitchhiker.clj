(ns datahike-benchmark.db.hitchhiker
  (:require [datahike-benchmark.db.interface :as db]
            [hitchhiker.tree.utils.async :as async]
            [hitchhiker.tree.messaging :as msg]
            [hitchhiker.tree :as tree]))


;; Hitchhiker functions


(def ^:const br 300)                                        ;; same as in datahike
(def ^:const br-sqrt (long (Math/sqrt br)))                 ;; same as in datahike

(def memory (atom {}))

(defn create-tree [data-type]
  (swap! memory assoc data-type (async/<?? (tree/b-tree (tree/->Config br-sqrt br (- br br-sqrt))))))

(defn delete-tree [data-type]
  (swap! memory dissoc data-type))

(defn insert-many [tree values op-count]
  (async/<??
   (async/reduce<
    (fn [tree val] (msg/insert tree val nil op-count))
    tree
    values)))

(defn entities->datoms [entities] ;; [e a v t added?] format
  (apply concat (map #(map (fn [[k v]] (vector 0 k v 0 true)) %) entities)))

(defn entities->values [entities]
  (apply concat (map #(map second %) entities)))

(defn entities->nodes [data-type entities]
  (if (= data-type :values)
    (entities->values entities)
    (entities->datoms entities)))


;; Multimethods


(defmethod db/connect :hitchhiker [_ {:keys [data-type]}]
  {:tree (get @memory data-type)
   :op-count 0
   :type data-type})

(defmethod db/release :hitchhiker [_ _] nil)

(defmethod db/transact :hitchhiker [_ {:keys [tree type op-count]} tx]
  (let [vals (entities->nodes type tx)
        new-tree (insert-many tree vals op-count)]
    {:tree (get (swap! memory assoc type new-tree) type)
     :op-count (+ op-count (count vals))
     :type type}))

(defmethod db/init :hitchhiker [_ _] nil)

(defmethod db/prepare-and-connect :hitchhiker [lib {:as config :keys [data-type]} _schema tx]
  (delete-tree data-type)
  (create-tree data-type)
  (let [conn (db/connect lib config)]
    (db/transact lib conn tx)))

(defmethod db/delete :hitchhiker  [_ {:keys [tree-type]}]
  (delete-tree tree-type))


