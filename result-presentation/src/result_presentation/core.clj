(ns result-presentation.core
  (:require [oz.core :as oz]
            [result-presentation.plots.connection :as connection]
            [result-presentation.plots.transaction :as transaction]
            [result-presentation.plots.random-query :as random-query]
            [clojure.java.io :as io]))

(def output-file-name "index.html")


(def connection-time-section
  [:div
   [:p "This benchmark measures the time consumed by datahike's and respectively datomic's 'connect' function."]
   [:p "The underlying data series could not be retrieved for datomic's in-memory database since it does not allow connecting to existing databases."]

   [:h4 "Backend Comparison"]
   [:div {:style {:display "flex" :flex-direction "row"}}
    [:vega connection/time-backends-plot]]
   [:p "Here, you can see"]
   [:ul
    [:li "Time grows linearly with number of datoms in database for datahike (but not for datomic? -> investigate!)"]
    [:li "Set index is 2 x faster than hitchhiker-tree index"]
    [:li "File backend is 4 x slower than in-memory"]
    [:li "LevelDB backend is 16 x slower than in-memory"]
    [:li "Postgres backend is 18 x slower than in-memory"]
    [:li "High variance for LevelDB and Postgres backends"]]

   [:h4 "Configuration Comparison"]
   [:div {:style {:display "flex" :flex-direction "row"}}
    [:vega connection/time-configs-plot]]
   [:p "Here, you can see"]
   [:ul
    [:li "Almost no difference for the performance for different datahike configuration"]]])

(def transaction-time-section
  [:div
   [:h4 "Backend Comparison for Datoms in Transaction"]
   [:div {:style {:display "flex" :flex-direction "row"}}
    [:vega transaction/time-backends-tx-plot]]
   [:p "Here, you can see"]
   [:ul
    [:li "Linear growth with number of datoms in transaction"]
    [:li "Transaction with in-memory database and hitchhiker-tree index is 10 x slower than inserts into a hitchhiker tree directly "]
    [:li "Set index is 30 x faster than hitchhiker-tree index"]
    [:li "File backend is 2 x slower than in-memory"]
    [:li "LevelDB backend is 2.5 x slower than in-memory"]
    [:li "Postgres backend is 3 x slower than in-memory"]
    [:li "Datomic free and in-memory databases show a performance similar to in-memory database with set index"]]

   [:h4 "Configuration Comparison for Datoms in Transaction"]
   [:div {:style {:display "flex" :flex-direction "row"}}
    [:vega transaction/time-configs-tx-plot]]
   [:p "Here, you can see"]
   [:ul
    [:li "Almost no difference for the performance for different datahike configuration"]]

   [:h4 "Backend Comparison for Datoms in Database"]
   [:div {:style {:display "flex" :flex-direction "row"}}
    [:vega transaction/time-backends-db-plot]]
   [:p "Here, you can see"]
   [:ul
    [:li "Numbers of datoms in database mostly irrelevant for performance"]]

   [:h4 "Configuration Comparison for Datoms in Database"]
   [:div {:style {:display "flex" :flex-direction "row"}}
    [:vega transaction/time-configs-db-plot]]
   [:p "Here, you can see"]
   [:ul
    [:li "Numbers of datoms in database mostly irrelevant for performance"]]])

(def query-time-section
  [:div
   [:h4 "Backend Comparison"]
   [:div {:style {:display "flex" :flex-direction "row"}}
    [:vega random-query/time-backends-plot]]
   [:p "Here, you can see"]
   [:ul
    [:li "Linear growth with number of joins"]
    [:li "Query performance only depends on index used"]
    [:li "Set index is 3 x faster than hitchhiker-tree index"]
    [:li "Datomic 4 x slower than datahike databases"]]

   [:h4 "Configuration Comparison"]
   [:div {:style {:display "flex" :flex-direction "row"}}
    [:vega random-query/time-configs-plot]]
   [:p "Here, you can see"]
   [:ul
    [:li "Fixed schema slows down query performance with a factor of 0.5-1.0"]]

   [:h4 "Type Comparison"]
   [:div {:style {:display "flex" :flex-direction "row"}}
    [:vega random-query/time-type-plot]]
   [:p "Here, you can see"]
   [:ul
    [:li "No significant differences for different data types for datahike databases"]
    [:li "Slight differences for different data types for datomic databases (constant around 5 ms)"]]

   [:h4 "Attribute Count Comparison"]
   [:div {:style {:display "flex" :flex-direction "row"}}
    [:vega random-query/time-attr-plot]]
   [:p "Here, you can see"]
   [:ul
    [:li "Performance depends on the number of attributes per entity \n(but probably even more on number of datoms in database which is not controlled for here)"]]])

(def connection-space-section
  [:div
   [:h4 "Backend Comparison"]
   [:div {:style {:display "flex" :flex-direction "row"}}
    [:vega connection/space-backends-plot]]
   [:p "Here, you can see"]
   [:ul
    [:li ""]]

   [:h4 "Configuration Comparison"]
   [:div {:style {:display "flex" :flex-direction "row"}}
    [:vega connection/space-configs-plot]]
   [:p "Here, you can see"]
   [:ul
    [:li ""]]])

(def transaction-space-section
  [:div
   [:h4 "Backend Comparison for Datoms in Transaction"]
   [:div {:style {:display "flex" :flex-direction "row"}}
    [:vega transaction/space-backends-tx-plot]]
   [:p "Here, you can see"]
   [:ul
    [:li ""]]

   [:h4 "Configuration Comparison for Datoms in Transaction"]
   [:div {:style {:display "flex" :flex-direction "row"}}
    [:vega transaction/space-configs-tx-plot]]
   [:p "Here, you can see"]
   [:ul
    [:li ""]]

   [:h4 "Backend Comparison for Datoms in Database"]
   [:div {:style {:display "flex" :flex-direction "row"}}
    [:vega transaction/space-backends-db-plot]]
   [:p "Here, you can see"]
   [:ul
    [:li ""]]

   [:h4 "Configuration Comparison for Datoms in Database"]
   [:div {:style {:display "flex" :flex-direction "row"}}
    [:vega transaction/space-configs-db-plot]]
   [:p "Here, you can see"]
   [:ul
    [:li ""]]])

(def query-space-section
  [:div
   [:h4 "Backend Comparison"]
   [:div {:style {:display "flex" :flex-direction "row"}}
    [:vega random-query/space-backends-plot]]
   [:p "Here, you can see"]
   [:ul
    [:li ""]]

   [:h4 "Configuration Comparison"]
   [:div {:style {:display "flex" :flex-direction "row"}}
    [:vega random-query/space-configs-plot]]
   [:p "Here, you can see"]
   [:ul
    [:li ""]]

   [:h4 "Type Comparison"]
   [:div {:style {:display "flex" :flex-direction "row"}}
    [:vega random-query/space-type-plot]]
   [:p "Here, you can see"]
   [:ul
    [:li ""]]

   [:h4 "Attribute Count Comparison"]
   [:div {:style {:display "flex" :flex-direction "row"}}
    [:vega random-query/space-attr-plot]]
   [:p "Here, you can see"]
   [:ul
    [:li ""]]])

(def time-section
  [:div
   [:h2 "Time"]
   [:h3 "Connection"]
   connection-time-section
   [:h3 "Transaction"]
   transaction-time-section
   [:h3 "Query"]
   query-time-section])

(def space-section
  [:div
   [:h2 "Space"]
   [:h3 "Connection"]
   connection-space-section
   [:h3 "Transaction"]
   transaction-space-section
   [:h3 "Query"]
   query-space-section])
(def html
  [:div
   [:h1 "Benchmarking Results"]
   time-section
   ;;space-section
   ])

;;(oz/start-server! 10555)
;;(oz/view! html)
;;(oz/export! html "results.html")


(defn -main [& args]
  (let [output-dir (first args)
        output-file (str output-dir output-file-name)]
    (when-not (.exists (io/file output-dir))
      (.mkdir (io/file output-dir)))
    (oz/export! html output-file)
    (println (str "Document has been exported to " output-file))))
