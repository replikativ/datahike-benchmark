(ns datahike-benchmark.core
  (:require [clojure.tools.cli :as cli]
            [datahike-benchmark.bench.api :as b]
            [datahike-benchmark.plots.api :as p]
            [datahike-benchmark.config :as c]
            [datahike-benchmark.util :as u]
            [com.hypirion.clj-xchart :as ch]
            [clojure.pprint :refer [pprint]]
            [clojure.string :refer [split trim]]
            [clojure.set :refer [difference]]
            [clojure.java.io :as io])
  (:gen-class))

;; TODO: transducer verwenden!

(def implemented-libs (set (map (comp name :lib) c/db-configurations)))
(def implemented-dbs (set (map (comp name :db) c/db-configurations)))
(def implemented-functions #{"connection" "transaction" "random-query"
                             ;;"set-query"
                             })

;; Documentation unclear about versions
;; :multi not recognized and :update-fn not working even in version 1.0.194 of tools.cli
;; uses v 0.3.5 of tools.cli in clojure 1.10.1
(def cli-options
  [["-e" "--crash-on-error" "Continue after error occurs" :default false]
   ["-D" "--not-save-data" "Do not save raw benchmark output data" :default false]
   ["-P" "--not-save-plots" "Do not create plots" :default false]
   ["-a" "--space-only" "Measure only heap allocations" :default false]
   ["-t" "--time-only" "Measure only execution time" :default false]
   ["-c" "--use-criterium" "Use criterium library for time measurements" :default false]
   ["-j" "--use-java" "Use Java Runtime memory functions for space measurements" :default false]
   ["-u" "--save-to-db URI" "Save results to datahike database with given URI instead of file" :default nil]

   ["-n" "--data-dir DIR" "Data directory" :default c/default-data-dir
    :validate [#(or (not (.exists (io/file %)))
                    (.isDirectory (io/file %)))
               "Path must be a directory if it already exists. Given: "]]
   ["-p" "--plot-dir DIR" "Plot directory" :default c/default-plot-dir
    :validate [#(or (not (.exists (io/file %)))
                    (.isDirectory (io/file %)))
               "Path must be a directory if it already exists"]]
   ["-m" "--error-dir DIR" "Error directory" :default c/default-error-dir
    :validate [#(or (not (.exists (io/file %)))
                    (.isDirectory (io/file %)))
               "Path must be a directory if it already exists"]]

   ["-s" "--seed SEED" "Initial seed for data creation"
    :default (rand-int c/max-int)
    :parse-fn #(Integer/parseInt %)]
   ["-g" "--time-step STEP"
    "Step size for measurements in ms. Used for measuring space with Java."
    :default 5
    :parse-fn #(Integer/parseInt %)]
   ["-d" "--space-step STEP"
    "Step size for measurements in kB. Used for measuring space with Profiler."
    :default 5
    :parse-fn #(Integer/parseInt %)]

   ["-b" "--only-database DBNAME" "Run benchmarks only for this database (library with backend)"
    :validate [#(contains? implemented-dbs %) (str "Must be one of: " implemented-dbs)]
    :default #{}
    :assoc-fn (fn [m k v] (assoc m k (conj (get m k) v)))
    ]
   ["-B" "--except-database DBNAME" "Do not run benchmarks for this database (library with backend)"
    :validate [#(contains? implemented-dbs %) (str "Must be one of: " implemented-dbs)]
    :default #{}
    :assoc-fn (fn [m k v] (assoc m k (conj (get m k) v)))]
   ["-l" "--only-lib LIB" "Run benchmarks only for this library"
    :validate [#(contains? implemented-libs %) (str "Must be one of: " implemented-libs)]
    :default #{}
    :assoc-fn (fn [m k v] (assoc m k (conj (get m k) v)))]
   ["-L" "--except-lib LIB" "Do not run benchmarks only for this library"
    :validate [#(contains? implemented-libs %) (str "Must be one of: " implemented-libs)]
    :default #{}
    :assoc-fn (fn [m k v] (assoc m k (conj (get m k) v)))]
   ["-f" "--only-function FUNCTION" "Function or database part to measure"
    :validate [#(contains? implemented-functions %) (str "Must be one of: " implemented-functions)]
    :default #{}
    :assoc-fn (fn [m k v] (assoc m k (conj (get m k) v)))]
   ["-F" "--except-function FUNCTION" "Function or database part not to measure"
    :validate [#(contains? implemented-functions %) (str "Must be one of: " implemented-functions)]
    :default #{}
    :assoc-fn (fn [m k v] (assoc m k (conj (get m k) v)))]

   ["-i" "--iterations ITERATIONS"
    "Number of iterations as string of space-separated integers of 1. connection 2. transaction and 3. query measurements (ignored for criterium)"
    :default {:connection 50 :transaction 10 :query 10}     ; transaction 50 iterations -> over 19 hours
    :parse-fn #(zipmap [:connection :transaction :query]
                       (map (fn [x] (Integer/parseInt x)) (split (trim %) #" ")))
    :validate [#(every? nat-int? (vals %)) "Must consist of non-negative integers"]]

   ["-x" "--db-datom-count RANGE" "Range of numbers of datoms in database for which benchmarks should be run. Used in 'connection' and 'transaction'. Range must be given as triple of integers 'start stop step' which are given as input for range function (range start stop step)"
    :default :function-specific
    :parse-fn #(apply range (map (fn [x] (Integer/parseInt x)) (split (trim %) #" ")))
    :validate [#(every? nat-int? %) "Must consist of non-negative integers"]]
   ["-y" "--tx-datom-count RANGE" "Range of numbers of datoms in database for which benchmarks should be run. Used in 'transaction'. Range must be given as triple of integers 'start stop step' which are given as input for range function (range start stop step)"
    :default :function-specific
    :parse-fn #(apply range (map (fn [x] (Integer/parseInt x)) (split (trim %) #" ")))
    :validate [#(every? nat-int? %) "Must consist of non-negative integers"]]
   ["-z" "--entity-count RANGE" "Range of numbers of entities in database for which benchmarks should be run. Used in 'random-query' and 'set-query'. Range must be given as triple of integers 'start stop step' which are given as input for range function (range start stop step)"
    :default :function-specific
    :parse-fn #(apply range (map (fn [x] (Integer/parseInt x)) (split (trim %) #" ")))
    :validate [#(every? nat-int? %) "Must consist of non-negative integers"]]
   ["-w" "--ref-attr-count RANGE" "Range of numbers of entities in database for which benchmarks should be run. Used in 'random-query'. Range must be given as triple of integers 'start stop step' which are given as input for range function (range start stop step)"
    :default :function-specific
    :parse-fn #(apply range (map (fn [x] (Integer/parseInt x)) (split (trim %) #" ")))
    :validate [#(every? nat-int? %) "Must consist of non-negative integers"]]

   ["-h" "--help"]])

(defn save [measurements subject resource options]
  (when (not (:not-save-data options))
    (print (str " Save " (name subject) " data..."))
    (if (nil? (:save-to-db options))
      (do
        (when-not (.exists (io/file (:data-dir options)))
          (.mkdir (io/file (:data-dir options))))
        (u/write-as-csv measurements (c/data-filename (:data-dir options) subject resource)))
      (do (when-not (.exists (io/file (:data-dir options)))
            (.mkdir (io/file (:data-dir options))))
          (u/write-to-db measurements subject resource (:save-to-db options))))
    (print " saved\n"))

  (when (not (:not-save-plots options))
    (print (str " Save " (name subject) " plots..."))
    (when-not (.exists (io/file (:plot-dir options)))
      (.mkdir (io/file (:plot-dir options))))
    (let [plots (p/create-plots subject measurements resource)]
      (doall (for [[plot file-suffix] plots]
               (do
                 ;;    (ch/view plot)
                 (ch/spit plot (c/plot-filename (:plot-dir options) subject file-suffix))))))
    (print " saved\n")))


(defn parse-multi [only except all]
  (let [included (if (not-empty only) only all)]
    (set (map keyword (difference included except)))))

(defn -main [& args]
  (let [{:keys [options errors summary]} (cli/parse-opts args cli-options)]
    (if (some? errors)
      (do (println "Errors:" errors)
          (println (str "Usage: lein run [options] \n\n  Options:\n" summary)))
      (if (:help options)
        (println (str "Usage: lein run [options] \n\n  Options:\n" summary))
        (let [libs-used (parse-multi (:only-lib options) (:except-lib options) implemented-libs)
              dbs-used (parse-multi (:only-database options) (:except-database options) implemented-dbs)
              databases (->> c/db-configurations
                             (filter #(contains? libs-used (:lib %)))
                             (filter #(contains? dbs-used (:db %))))
              ext-options (assoc options
                            :databases databases
                            :time-method (if (:use-criterium options) :criterium :simple)
                            :space-method (if (:use-java options) :java :profiler))
              functions (parse-multi (:only-function options) (:except-function options) implemented-functions)]

          (println "Options used: ")
          (pprint options)
          (println "Databases used: ")
          (pprint (apply sorted-set (map :db databases)))
          (doall (for [function functions]
                   (do
                     (when (not (:space-only options))
                       (-> (b/bench function :time (:time-method ext-options) ext-options)
                           (save function :time options)))
                     (when (not (:time-only options))
                       (-> (b/bench function :space (:space-method ext-options) ext-options)
                           (save function :space options))))))))))
  (println "Benchmarking has finished successfully"))
