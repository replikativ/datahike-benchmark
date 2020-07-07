(ns datahike-benchmark.config
  (:import (java.util Date)
           (java.text SimpleDateFormat)))


;; Maximum value for integer databases
(def max-int Integer/MAX_VALUE)


;; Output

(def plot-dir "./plots")
(def data-dir "./data")
(def error-dir "/tmp/datahike-benchmark-errors")

(def date-format "yyyy-MM-dd_HH-mm-ss")

(defn filename [directory subject file-suffix ext]
  (str (if (= directory "") "" (str directory "/"))
       (.format (SimpleDateFormat. date-format) (Date.))
       "_" subject
       (if (= "" file-suffix) "" (str "_" file-suffix))
       "."
       ext))

(defn data-filename [subject resource]
  (filename data-dir (name subject) (name resource) "csv"))

(defn plot-filename [subject file-suffix]
  (filename plot-dir (name subject) file-suffix "png"))

(defn error-filename []
  (filename error-dir "error" "" "edn"))


;; Benchmark uris

(def datahike-uris
  (for [sor [false true]
        ti [false true]
        base-config [                                          ;; missing tests and install instructions (also mutex error?): {:name "Redis (HHT)" :uri "datahike:redis:localhost:6379/"  :db :dh-redis-hht :index :datahike.index/hitchhiker-tree :store {:backend :redis :host "localhost" :port 6379  :path "performance"}}
                  {:backend "In-Memory (HHT)" :uri "datahike:mem://" :store {:backend :mem :path "performance-hht"} :db :dh-mem-hht :index :datahike.index/hitchhiker-tree}
                  {:backend "In-Memory (Set)" :uri "datahike:mem://" :store {:backend :mem :path "performance-ps"} :db :dh-mem-set :index :datahike.index/persistent-set}
                  ;; fails too often: {:name "LevelDB (HHT)" :uri "datahike:level://" :store {:backend :level :path "/tmp/lvl-performance"} :db :dh-level :index :datahike.index/hitchhiker-tree}
                  {:backend "PostgreSQL (HHT)" :uri "datahike:pg://datahike:clojure@localhost:5440/" :db :dh-psql :index :datahike.index/hitchhiker-tree
                   :store   {:backend :pg :host "localhost" :port 5440 :username "datahike" :password "clojure" :path "performance"}}
                  {:backend "File-based (HHT)" :uri "datahike:file://" :store {:backend :file :path "/tmp/file-performance"} :db :dh-file :index :datahike.index/hitchhiker-tree}]]
    (let [new-path (str (get-in base-config [:store :path]) "-s" (if sor 1 0) "-t" (if ti 1 0))
          new-store (assoc (:store base-config) :path new-path)]
      (merge base-config
        {:lib            :datahike
         :schema-on-read sor
         :temporal-index ti
         :uri            (str (:uri base-config) new-path)
         :store          new-store}))))

(def datomic-uris [{:lib :datomic :backend "Datomic Mem" :db :dat-mem :uri "datomic:mem://performance" :schema-on-read false :temporal-index false} ;; not working on connect
                   {:lib :datomic :backend "Datomic Free" :db :dat-free :uri "datomic:free://localhost:4334/performance?password=clojure" :schema-on-read false :temporal-index false}])

(def hitchhiker-configs [{:lib :hitchhiker :backend " Hitchhiker Tree (Datoms)" :db :hht-dat :uri "datoms" :schema-on-read false :temporal-index false}
                         {:lib :hitchhiker :backend " Hitchhiker Tree (Values)" :db :hht-val :uri "values" :schema-on-read false :temporal-index false}])

(def db-configurations (into [] (concat datahike-uris datomic-uris hitchhiker-configs)))

(def libs (set (map :lib db-configurations)))


;; Resources that can be measured

(def resources #{:time :space})

(defn unit [resource]
  (case resource
    :space "kB"
    :time "ms"
    :default ""))


;; xchart values

(def x-colors [:red :blue :cyan :green :magenta :orange :pink :yellow :black :light-gray :dark-gray :gray])
(def x-shapes [:square :circle :diamond :triangle-up :triangle-down])
(def x-strokes [:solid :dash-dash :dash-dot :dot-dot])

