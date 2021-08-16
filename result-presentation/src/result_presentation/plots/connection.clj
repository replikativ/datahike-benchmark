(ns result-presentation.plots.connection
  (:require [result-presentation.plots.template :refer [plot]]
            [result-presentation.data :refer [get-data]]))


(defn data [resource]
  (->> (get-data "connection" resource)
       (map #(select-keys % [:datoms :mean :config :backend :sd]))
       (filter #(not (= (:backend %) "Datomic Free")))
       vec))

(def time-backends-plot
  (plot {:title      "Connection Performance of Different Backends"
         :vals       (sort-by :datoms (data "time"))
         :x          {:key :datoms :title "Datoms in Database"}
         :y          {:key :mean :title "Time (in ms)"}
         :color      {:key :backend :title "Backend"}
         :selections [{:key :config :title "Configuration" :init "simple"}]
         :zero-y     true}))

(def time-configs-plot
  (plot {:title      "Connection Performance of Different Configurations"
         :vals       (sort-by :datoms (data "time"))
         :x          {:key :datoms :title "Datoms in Database"}
         :y          {:key :mean :title "Time (in ms)"}
         :color      {:key :config :title "Configuration"}
         :selections [{:key :backend :title "Backend" :init "In-Memory (HHT)"}]
         :zero-y     false}))

(def space-backends-plot
  (plot {:title      "Connection Space Requirements of Different Backends"
         :vals       (sort-by :datoms (data "space"))
         :x          {:key :datoms :title "Datoms in Database"}
         :y          {:key :mean :title "Space (in kB)"}
         :color      {:key :backend :title "Backend"}
         :selections [{:key :config :title "Configuration" :init "simple"}]
         :zero-y     true}))

(def space-configs-plot
  (plot {:title      "Connection Space Requirements of Different Configurations"
         :vals       (sort-by :datoms (data "space"))
         :x          {:key :datoms :title "Datoms in Database"}
         :y          {:key :mean :title "Space (in kB)"}
         :color      {:key :config :title "Configuration"}
         :selections [{:key :backend :title "Backend" :init "In-Memory (HHT)"}]
         :zero-y     false}))