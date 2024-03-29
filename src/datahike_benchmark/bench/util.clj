(ns datahike-benchmark.bench.util
  (:require [datahike-benchmark.config :as c]
            [clojure.pprint :refer [pprint]]
            [clojure.java.io :as io]
            [clojure.string :as s])
  (:import (java.util Random Collection ArrayList Collections)
           (clojure.lang RT)))

;; Error handling

(defn print-short-error-report [error]
  (let [{:keys [trace cause]} (Throwable->map error)
        [first-items rest-items] (split-at 5 trace)
        rest-important-items (filter #(s/starts-with? (first %) "datahike_benchmark")
                                     rest-items)]
    (println "  Shortened stacktrace for:  " cause)
    (println "   Start:")
    (dorun (for [item first-items]
             (println "    " item)))
    (when (seq? rest-important-items)
      (println "   Then:")
      (dorun (for [item rest-important-items]
               (println "    " item))))))

(defn save-error-report [error {:keys [error-dir] :as options} context]
  (when-not (.exists (io/file error-dir))
    (.mkdir (io/file error-dir)))
  (let [filename (c/error-filename error-dir)]

    (spit filename
          (with-out-str
            (pprint
             {:error        (Throwable->map error)
              :loop-context context
              :cli-options  options})))
    (println (str "Full error report saved in " filename))
    filename))

(defn error-handling
  ([error options] (error-handling error options nil))
  ([error options context]
   (save-error-report error options context)
   (if (:crash-on-error options)
     (throw error)
     (print-short-error-report error))))


;; Schema creation


(defn make-attr
  ([name type] (make-attr name type :db.cardinality/one))
  ([name type cardinality]
   {:db/ident       name
    :db/valueType   type
    :db/cardinality cardinality}))


;; Random data generation


(defn data-generator
  ([type seed]
   (let [r (Random. seed)
         generator (case type
                     :db.type/long #(long (.nextInt r))
                     :db.type/string #(format "%15d" (.nextInt r))
                     (throw (Exception. (str "Generator implemented for type: " type))))]
     generator)))

(defn int-generator [seed]
  (data-generator :db.type/long seed))

(defn shuffle-generator [seed]
  (let [r (Random. seed)]
    #(let [al (ArrayList. ^Collection %)]
       (Collections/shuffle al r)
       (RT/vector (.toArray al)))))

(defn datom-generator [attribute-ident type seed]
  (let [generator (data-generator type seed)]
    #(hash-map attribute-ident (generator))))

(defn tx-generator [attribute-ident type n seed]
  (if (pos? n)
    (let [generate-datom (datom-generator attribute-ident type seed)]
      #(vec (repeatedly n generate-datom)))
    #(vector)))

(defn create-n-str-transactions [attribute-ident n seed]
  (let [generate-tx (tx-generator attribute-ident :db.type/string n seed)]
    (generate-tx)))


;; Point series generation


(defn linspace [start end point-count]
  (vec (range start
              (inc end)
              (/ (- end start) (dec point-count)))))

(defn int-linspace [start end point-count]
  (mapv int (linspace start end point-count)))
