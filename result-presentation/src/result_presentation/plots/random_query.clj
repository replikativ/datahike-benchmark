(ns result-presentation.plots.random-query
  (:require [result-presentation.plots.template :refer [plot]]
            [clojure.set :refer [rename-keys]]
            [result-presentation.data :refer [get-data]]))


(defn data [resource]
  (->> (get-data "random-query" resource)
       (map #(select-keys % [:dtype :mean :config :n-joins :entities :n-attr :backend :sd :sd2]))
       (map #(rename-keys % {:n-joins :njoins :n-attr :nattr}))
       vec))


(def time-backends-plot
  (plot {:title      "Query Performance of Different Backends"
         :vals       (sort-by :njoins (data "time"))
         :x          {:key :njoins :title "Number of Joins for Query (10 clauses)"}
         :y          {:key :mean :title "Time (in ms)"}
         :color      {:key :backend :title "Backend"}
         :selections [{:key :config :title "Configuration" :init "simple"}
                      {:key :dtype :title "Type" :init "long"}
                      {:key :entities :title "Entities" :init 10}
                      {:key :nattr :title "Attributes" :init 101}]
         :zero-y     true}))

(def time-configs-plot
  (plot {:title      "Query Performance of Different Configurations"
         :vals       (sort-by :njoins (data "time"))
         :x          {:key :njoins :title "Number of Joins for Query (10 clauses)"}
         :y          {:key :mean :title "Time (in ms)"}
         :color      {:key :config :title "Configuration"}
         :selections [{:key :backend :title "Backend" :init "In-Memory (HHT)"}
                      {:key :dtype :title "Type" :init "long"}
                      {:key :entities :title "Entities" :init 10}
                      {:key :nattr :title "Attributes" :init 101}]
         :zero-y     false}))

(def time-type-plot
  (plot {:title      "Query Performance of Different Data Types"
         :vals       (sort-by :njoins (data "time"))
         :x          {:key :njoins :title "Number of Joins for Query (10 clauses)"}
         :y          {:key :mean :title "Time (in ms)"}
         :color      {:key :dtype :title "Type"}
         :selections [{:key :backend :title "Backend" :init "In-Memory (HHT)"}
                      {:key :config :title "Configuration" :init "simple"}
                      {:key :entities :title "Entities" :init 10}
                      {:key :nattr :title "Attributes" :init 101}]
         :zero-y     false}))

(def time-attr-plot
  (plot {:title      "Query Performance of Different Number of Attributes per Entity"
         :vals       (sort-by :njoins (data "time"))
         :x          {:key :njoins :title "Number of Joins for Query (10 clauses)"}
         :y          {:key :mean :title "Time (in ms)"}
         :color      {:key :nattr :title "Attributes per Entity"}
         :selections [{:key :backend :title "Backend" :init "In-Memory (HHT)"}
                      {:key :config :title "Configuration" :init "simple"}
                      {:key :entities :title "Entities" :init 10}
                      {:key :dtype :title "Type" :init "string"}]
         :zero-y     false}))

(def space-backends-plot
  (plot {:title      "Query Space Requirements of Different Backends"
         :vals       (sort-by :njoins (data "space"))
         :x          {:key :njoins :title "Number of Joins for Query (10 clauses)"}
         :y          {:key :mean :title "Space (in kB)"}
         :color      {:key :backend :title "Backend"}
         :selections [{:key :config :title "Configuration" :init "simple"}
                      {:key :dtype :title "Type" :init "long"}
                      {:key :entities :title "Entities" :init 10}
                      {:key :nattr :title "Attributes" :init 101}]
         :zero-y     true}))

(def space-configs-plot
  (plot {:title      "Query Space Requirements of Different Configurations"
         :vals       (sort-by :njoins (data "space"))
         :x          {:key :njoins :title "Number of Joins for Query (10 clauses)"}
         :y          {:key :mean :title "Space (in kB)"}
         :color      {:key :config :title "Configuration"}
         :selections [{:key :backend :title "Backend" :init "In-Memory (HHT)"}
                      {:key :dtype :title "Type" :init "long"}
                      {:key :entities :title "Entities" :init 10}
                      {:key :nattr :title "Attributes" :init 101}]
         :zero-y     false}))

(def space-type-plot
  (plot {:title      "Query Space Requirements of Different Data Types"
         :vals       (sort-by :njoins (data "space"))
         :x          {:key :njoins :title "Number of Joins for Query (10 clauses)"}
         :y          {:key :mean :title "Space (in kB)"}
         :color      {:key :dtype :title "Type"}
         :selections [{:key :backend :title "Backend" :init "In-Memory (HHT)"}
                      {:key :config :title "Configuration" :init "simple"}
                      {:key :entities :title "Entities" :init 10}
                      {:key :nattr :title "Attributes" :init 101}]
         :zero-y     false}))

(def space-attr-plot
  (plot {:title      "Query Space Requirements of Different Number of Attributes per Entity"
         :vals       (sort-by :njoins (data "space"))
         :x          {:key :njoins :title "Number of Joins for Query (10 clauses)"}
         :y          {:key :mean :title "Space (in kB)"}
         :color      {:key :nattr :title "Attributes per Entity"}
         :selections [{:key :backend :title "Backend" :init "In-Memory (HHT)"}
                      {:key :config :title "Configuration" :init "simple"}
                      {:key :entities :title "Entities" :init 10}
                      {:key :dtype :title "Type" :init "string"}]
         :zero-y     false}))
