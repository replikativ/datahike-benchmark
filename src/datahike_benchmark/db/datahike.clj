(ns datahike-benchmark.db.datahike
  (:require [datahike-benchmark.db.interface :as db]
            [datahike.api :as d]
            ;[datahike-leveldb.core]
            [datahike-jdbc.core]))

(defmethod db/connect :datahike [_ {:keys [dh-config]}] (d/connect dh-config))

(defmethod db/transact :datahike [_ conn tx] (d/transact conn tx))

(defmethod db/release :datahike [_ conn] (d/release conn))

(defmethod db/db :datahike [_ conn] (d/db conn))

(defmethod db/q :datahike [_ query db] (d/q query db))

(defmethod db/init :datahike [_ {:keys [dh-config]}]
  (when (d/database-exists? dh-config)
    (d/delete-database dh-config))
  (d/create-database dh-config))

(defmethod db/prepare-and-connect :datahike [_ {:keys [dh-config]} schema tx]
  (when (d/database-exists? dh-config)
    (d/delete-database dh-config))
  (d/create-database dh-config)
  (let [conn (d/connect dh-config)]
    (when (= :write (:schema-flexibility dh-config))
      (d/transact conn schema))
    (d/transact conn tx)
    conn))

(defmethod db/delete :datahike [_ {:keys [dh-config]}]
  (when (d/database-exists? dh-config)
    (d/delete-database dh-config)))

(def datahike-base-configs
  (let [common-info   {:lib :datahike}
        specific-info [{:display-name "In-Memory (HHT)"
                        :db           :dh-mem-hht
                        :dh-config    {:store {:backend :mem
                                               :dbname  "performance-hht"}
                                       :index :datahike.index/hitchhiker-tree
                                       :name "performance-hht"}}
                       {:display-name "In-Memory (Set)"
                        :db           :dh-mem-set
                        :dh-config    {:store {:backend :mem
                                               :dbname  "performance-ps"}
                                       :index :datahike.index/persistent-set
                                       :name "performance-ps"}}
                       {:display-name "File-based (HHT)"
                        :db           :dh-file
                        :dh-config    {:index :datahike.index/hitchhiker-tree
                                       :store {:backend :file
                                               :path "/tmp/performance-file"
                                               :dbname  "performance-file"}
                                       :name  "performance-file"}}
                       #_{:display-name "LevelDB (HHT)"
                          :db           :dh-level
                          :dh-config    {:index :datahike.index/hitchhiker-tree}
                          :store {:backend :level
                                  :path "/tmp/performance-lvl"
                                  :dbname "performance-lvl"}
                          :name "performance-lvl"}
                       #_{:display-name "JDBC Postgres (HHT)"
                          :db           :dh-psql
                          :dh-config    {:index :datahike.index/hitchhiker-tree
                                         :store {:backend  :jdbc
                                                 :dbtype   "postgresql"
                                                 :host     "localhost"
                                                 :port     5440
                                                 :user     "datahike"
                                                 :password "clojure"
                                                 :dbname   "performance_psql"}
                                         :name "performance-psql"}}
                       #_{:display-name "JDBC MySql (HHT)"
                          :db           :dh-mysql
                          :dh-config    {:index :datahike.index/hitchhiker-tree
                                         :store {:backend  :jdbc
                                                 :dbtype   "mysql"
                                                 :host     "localhost"
                                                 :port     3306
                                                 :user     "datahike"
                                                 :password "clojure"
                                                 :dbname   "performance_msql"}
                                         :name   "performance-msql"}}
                       {:display-name "JDBC H2 (HHT)"
                        :db           :dh-h2
                        :dh-config    {:index :datahike.index/hitchhiker-tree
                                       :store {:backend :jdbc
                                               :dbtype  "h2"
                                               :dbname  "/tmp/performance-h2"}
                                       :name  "performance-h2"}}]]
    (vec (for [info specific-info]
           (merge common-info info)))))

(defmethod db/configs :datahike [_]
  (for [ti          [false true]
        sor         [:read :write]
        base-config datahike-base-configs]
    (let [suffix (str "_s" (if (= sor :read) "r" "w")
                      "_t" (if ti 1 0))]
      (-> base-config
          (update-in [:dh-config :store :dbname] str suffix)
          (assoc-in [:dh-config :schema-flexibility] sor)
          (assoc-in [:dh-config :keep-history?] ti)
          (update-in [:dh-config :name] str suffix)))))
