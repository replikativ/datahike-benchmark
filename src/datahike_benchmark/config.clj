(ns datahike-benchmark.config
  (:import (java.util Date)
           (java.text SimpleDateFormat)))


;; Maximum value for integer databases
(def max-int Integer/MAX_VALUE)


;; Output

(def default-plot-dir "./plots")
(def default-data-dir "./data")
(def default-error-dir "/tmp/datahike-benchmark-errors")

(def date-format "yyyy-MM-dd_HH-mm-ss")

(defn filename [directory subject file-suffix ext]
  (str (if (= directory "") "" (str directory "/"))
       (.format (SimpleDateFormat. date-format) (Date.))
       "_" subject
       (if (= "" file-suffix) "" (str "_" file-suffix))
       "."
       ext))

(defn data-filename [data-dir subject resource]
  (filename data-dir (name subject) (name resource) "csv"))

(defn plot-filename [plot-dir subject file-suffix]
  (filename plot-dir (name subject) file-suffix "png"))

(defn error-filename [error-dir]
  (filename error-dir "error" "" "edn"))


;; Benchmark uris

(def default-schema-flexibility :read)
(def default-keep-history? true)

(def datahike-base-configs
  (let [common-info   {:lib :datahike}
        specific-info [{:display-name "In-Memory (HHT)"
                        :db           :dh-mem-hht
                        :dh-config    {:store {:backend :mem
                                               :dbname  "performance-hht"}
                                       :index :datahike.index/hitchhiker-tree}}
                       {:display-name "In-Memory (Set)"
                        :db           :dh-mem-set
                        :dh-config    {:store {:backend :mem
                                               :dbname  "performance-ps"}
                                       :index :datahike.index/persistent-set}}
                       {:display-name "File-based (HHT)"
                        :db           :dh-file
                        :dh-config    {:index :datahike.index/hitchhiker-tree
                                       :store {:backend :file
                                               :dbname  "/tmp/performance-file"}}}
                       ;;{:backend "LevelDB (HHT)" :uri "datahike:level://" :store {:backend :level :path "/tmp/lvl-performance"} :db :dh-level :index :datahike.index/hitchhiker-tree}
                       {:display-name "JDBC Postgres (HHT)"
                        :db           :dh-psql
                        :dh-config    {:index :datahike.index/hitchhiker-tree
                                       :store {:backend  :jdbc
                                               :dbtype   "postgresql"
                                               :host     "localhost"
                                               :port     5440
                                               :user     "datahike"
                                               :password "clojure"
                                               :dbname   "performance-psql"}}}
                       {:display-name "JDBC MySql (HHT)"
                        :db           :dh-mysql
                        :dh-config    {:index :datahike.index/hitchhiker-tree
                                       :store {:backend  :jdbc
                                               :dbtype   "mysql"
                                               :host     "localhost"
                                               :port     3306
                                               :user     "datahike"
                                               :password "clojure"
                                               :dbname   "performance-msql"}}}
                       {:display-name "JDBC H2 (HHT)"
                        :db           :dh-h2
                        :dh-config    {:index :datahike.index/hitchhiker-tree
                                       :store {:backend :jdbc
                                               :dbtype  "h2:mem"
                                               :dbname  "performance-h2"}}}]]
    (vec (for [info specific-info]
           (merge common-info info)))))

(def datahike-configs
  (for [ti          [false true]
        sor         [:read :write]
        base-config datahike-base-configs]
    (let [suffix (str "-s" (if (= sor :read) "r" "w")
                      "-t" (if ti 1 0))]
      (-> base-config
          (update-in [:dh-config :store :dbname] str suffix)
          (assoc-in [:dh-config :schema-flexibility] sor)
          (assoc-in [:dh-config :keep-history?] ti)))))

(def datomic-configs [{:lib          :datomic
                       :display-name "Datomic Mem"
                       :db           :dat-mem
                       :uri          "datomic:mem://performance"} ;; not working on connect
                      {:lib          :datomic
                       :display-name "Datomic Free"
                       :db           :dat-free
                       :uri          "datomic:free://localhost:4334/performance?password=clojure"}])

(def hitchhiker-configs [{:lib          :hitchhiker
                          :display-name " Hitchhiker Tree (Datoms)"
                          :db           :hht-dat
                          :tree-type    :datoms}
                         {:lib          :hitchhiker
                          :display-name " Hitchhiker Tree (Values)"
                          :db           :hht-val
                          :tree-type    :values}])

(def db-configurations (vec (concat datahike-configs datomic-configs hitchhiker-configs)))

(def libs (set (map :lib db-configurations)))


;; Resources that can be measured

(def resources #{:time :space})

(defn unit [resource]
  (case resource
    :space "kB"
    :time "ms"
    ""))


;; xchart values

(def x-colors [:red :blue :cyan :green :magenta :orange :pink :yellow :black :light-gray :dark-gray :gray])
(def x-shapes [:square :circle :diamond :triangle-up :triangle-down])
(def x-strokes [:solid :dash-dash :dash-dot :dot-dot])
