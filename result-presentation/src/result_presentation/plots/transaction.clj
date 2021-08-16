(ns result-presentation.plots.transaction
  (:require [result-presentation.plots.template :refer [plot]]
            [clojure.set :refer [rename-keys]]
            [result-presentation.data :refer [get-data]]))


(defn data [resource]
  (->> (get-data "transaction" resource)
       (map #(select-keys % [:datoms :mean :config :db-size :backend :sd :sd2]))
       (map #(rename-keys % {:db-size :dbsize}))
       vec))

(def time-backends-tx-plot
  (plot {:title      "Transaction Performance of Different Backends"
         :vals       (sort-by :datoms (data "time"))
         :x          {:key :datoms :title "Datoms in Transaction"}
         :y          {:key :mean :title "Time (in ms)"}
         :color      {:key :backend :title "Backend"}
         :selections [{:key :config :title "Configuration" :init "simple"}
                      {:key :dbsize :title "Datoms in Database" :init 1000}]
         :zero-y     true}))

(def time-configs-tx-plot
  (plot {:title      "Transaction Performance of Different Configurations"
         :vals       (sort-by :datoms (data "time"))
         :x          {:key :datoms :title "Datoms in Transaction"}
         :y          {:key :mean :title "Time (in ms)"}
         :color      {:key :config :title "Configuration"}
         :selections [{:key :backend :title "Backend" :init "In-Memory (HHT)"}
                      {:key :dbsize :title "Datoms in Database" :init 1000}]
         :zero-y     true}))

(def time-backends-db-plot
  (plot {:title      "Transaction Performance of Different Backends"
         :vals       (sort-by :dbsize (data "time"))
         :x          {:key :dbsize :title "Datoms in Database"}
         :y          {:key :mean :title "Time (in ms)"}
         :color      {:key :backend :title "Backend"}
         :selections [{:key :config :title "Configuration" :init "simple"}
                      {:key :datoms :title "Datoms in Transaction" :init 1000}]
         :zero-y     true}))

(def time-configs-db-plot
  (plot {:title      "Transaction Performance of Different Number of Attributes per Entity"
         :vals       (sort-by :dbsize (data "time"))
         :x          {:key :dbsize :title "Datoms in Database"}
         :y          {:key :mean :title "Time (in ms)"}
         :color      {:key :config :title "Configuration"}
         :selections [{:key :backend :title "Backend" :init "In-Memory (HHT)"}
                      {:key :datoms :title "Datoms in Transaction" :init 1000}]
         :zero-y     true}))

(def space-backends-tx-plot
  (plot {:title      "Transaction Space Requirements of Different Backends"
         :vals       (sort-by :datoms (data "space"))
         :x          {:key :datoms :title "Datoms in Transaction"}
         :y          {:key :mean :title "Space (in kB)"}
         :color      {:key :backend :title "Backend"}
         :selections [{:key :config :title "Configuration" :init "simple"}
                      {:key :dbsize :title "Datoms in Database" :init 1000}]
         :zero-y     true}))

(def space-configs-tx-plot
  (plot {:title      "Transaction Space Requirements of Different Configurations"
         :vals       (sort-by :datoms (data "space"))
         :x          {:key :datoms :title "Datoms in Transaction"}
         :y          {:key :mean :title "Space (in kB)"}
         :color      {:key :config :title "Configuration"}
         :selections [{:key :backend :title "Backend" :init "In-Memory (HHT)"}
                      {:key :dbsize :title "Datoms in Database" :init 1000}]
         :zero-y     true}))

(def space-backends-db-plot
  (plot {:title      "Transaction Space Requirements of Different Backends"
         :vals       (sort-by :dbsize (data "space"))
         :x          {:key :dbsize :title "Datoms in Database"}
         :y          {:key :mean :title "Space (in kB)"}
         :color      {:key :backend :title "Backend"}
         :selections [{:key :config :title "Configuration" :init "simple"}
                      {:key :datoms :title "Datoms in Transaction" :init 1000}]
         :zero-y     true}))

(def space-configs-db-plot
  (plot {:title      "Transaction Space Requirements of Different Number of Attributes per Entity"
         :vals       (sort-by :dbsize (data "space"))
         :x          {:key :dbsize :title "Datoms in Database"}
         :y          {:key :mean :title "Space (in kB)"}
         :color      {:key :config :title "Configuration"}
         :selections [{:key :backend :title "Backend" :init "In-Memory (HHT)"}
                      {:key :datoms :title "Datoms in Transaction" :init 1000}]
         :zero-y     true}))