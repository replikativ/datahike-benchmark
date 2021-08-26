(ns datahike-benchmark.config
  (:import (java.util Date)
           (java.text SimpleDateFormat)))


;; Maximum value for integer databases

(def max-int Integer/MAX_VALUE)


;; Output

(def default-plot-dir "./plots")
(def default-data-dir "./data")
(def default-error-dir "./errors")

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


;; Benchmark Configurations

(def default-schema-flexibility :write)
(def default-keep-history? false)


;; Resources that can be measured

(defn unit [resource]
  (case resource
    :space "kB"
    :time "ms"
    ""))


;; xchart values

(def x-colors [:red :blue :cyan :green :magenta :orange :pink :yellow :black :light-gray :dark-gray :gray])
(def x-shapes [:square :circle :diamond :triangle-up :triangle-down])
(def x-strokes [:solid :dash-dash :dash-dot :dot-dot])
