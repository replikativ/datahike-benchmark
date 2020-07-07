(ns datahike-benchmark.measure.api
  (:require [datahike-benchmark.measure.function-specific :as f]
            [datahike-benchmark.measure.util :as u]
            [datahike-benchmark.config :as c]
            [clj-async-profiler.core :as prof]
            [criterium.core :as cr]
            [clojure.string :as str])
  (:import (java.io StringWriter File)))


;;
;; Measure resources like time and space (= heap allocations)
;;

(defmulti measure
          "Get statistics for measurements.
          Arguments: [resource method options iterations function lib f-args]"
          (fn [resource method _ _ _ _ _] [resource method]))


;; Common functions

(defn return-map [samples]
  {:samples samples
   :mean (u/mean samples)
   :sd (u/sd samples)
   :median (u/median samples)})

(defn error [^Throwable error resource iteration]
  (ex-info (.getMessage error)
           {:iteration iteration
            :resource resource}))

;; Measure time using clojure.core/time

(defmacro with-out-str-data-map
  [& body]
  `(let [s# (new StringWriter)]
     (binding [*out* s#]
       (let [r# ~@body]
         {:res r#
          :str (str s#)}))))

(defn get-time-with-res
  "Return result and execution time of function as vector [result time]"
  [f]
  (let [res (with-out-str-data-map (time (f)))]
    [(:res res) (read-string (nth (str/split (:str res) #" ")
                                  2))]))

(defmethod measure [:time :core.time]
  [_ _ _ iterations function lib f-args]
  (let [setup-fn (f/get-setup-fn function lib f-args)
        fn-to-measure (f/get-fn-to-measure function lib f-args)
        tear-down-fn (f/get-tear-down-fn function lib f-args)

        times (vec (for [i (range 0 iterations)]
                     (try (let [args (setup-fn)
                                [res t] (get-time-with-res (fn [] (fn-to-measure args)))]
                            (tear-down-fn args res)
                            t)
                          (catch Exception e (throw (error e i :time)))
                          (catch AssertionError e (throw (error e i :time))))))]
    (return-map times)))


;; Measure time using criterium library

(defmethod measure [:time :criterium]
  [_ _ _ _ function lib f-args]
  (let [one-time-setup-fn (f/get-one-time-setup-fn function lib f-args)
        fn-to-measure (f/get-fn-to-measure function lib f-args)
        one-time-tear-down-fn (f/get-one-time-tear-down-fn function lib f-args)

        args (one-time-setup-fn)
        stats (cr/benchmark (fn-to-measure args) {:verbose false})]
    (one-time-tear-down-fn args)
    stats))


;; Measure space using Java Runtime methods

(defn current-space-used []
  (/ (- (-> (Runtime/getRuntime) (.totalMemory))
        (-> (Runtime/getRuntime) (.freeMemory))) 1024.0)) ;; byte -> kb

(defn measure-max-space-used
  "Function executed in thread to monitor memory used"
  [time-step res-atom stop-thread]
  (while true
    (when (not (deref stop-thread))
      (let [current-space (current-space-used)]
        (when (< (deref res-atom) current-space)
          (reset! res-atom current-space))
        (Thread/sleep time-step)))))


(defmethod measure [:space :java]
  [_ _ options iterations function lib f-args]              ;; TODO: options hier destructuren
  (let [{:keys [time-step]} options  ;; in ms
        setup-fn (f/get-setup-fn function lib f-args)
        fn-to-measure (f/get-fn-to-measure function lib f-args)
        tear-down-fn (f/get-tear-down-fn function lib f-args)
        max-space-used (atom (current-space-used))
        stop-thread (atom false)
        thread (Thread. ^Runnable (fn [] (measure-max-space-used time-step max-space-used stop-thread)))]
    (.start thread)
    (let [s (doall (for [i (range 0 iterations)]
                     (let [args (setup-fn)
                           initial-space (current-space-used)]
                       (try
                         (reset! max-space-used initial-space)
                         (let [res (fn-to-measure args)
                               max-space (deref max-space-used)
                               space (- max-space initial-space)]
                           (tear-down-fn args res)
                           space)
                         (catch Exception e (throw (error e :space i)))
                         (catch AssertionError e (throw (error e :space i)))))))]
      (reset! stop-thread true)
      (return-map s))))


(defn profile-space [space-step iterations fn-to-measure args]
  (let [file (try (prof/profile
                    {:event       :alloc
                     :interval    space-step
                     :generate-flamegraph? false
                     :return-file true}
                    (doall (dotimes [i iterations]
                             (try
                               (fn-to-measure args)
                               (catch Exception e (throw (error e :space i)))
                               (catch AssertionError e (throw (error e :space i)))))))
                  (catch Exception e (if (clojure.string/includes? (.getMessage e) "No stack counts found")
                                       (println "Warning:" (.getMessage e))
                                       (throw e))))
        data (if (nil? file)
               []
               (let [txt-name (.getAbsolutePath ^File file)]
                 (mapv #(clojure.string/split % #" ")
                       (clojure.string/split-lines (slurp txt-name)))))]
    data))



(defmethod measure [:space :profiler]
  [_ _ options iterations function lib f-args]
   (let [{:keys [space-step]} options
         one-time-setup-fn (f/get-one-time-setup-fn function lib f-args)
         fn-to-measure (f/get-fn-to-measure function lib f-args)
         one-time-tear-down-fn (f/get-one-time-tear-down-fn function lib f-args)
         args (one-time-setup-fn)
         data (profile-space space-step iterations fn-to-measure args)
         relevant-data (filter #(map (fn [lib] clojure.string/includes? (first %) (name lib)) ;; TODO: bbatsov, clojure styleguide, nicht mischen
                                     c/libs)
                                data)
         sample-counts (remove nil?
                               (map #(try (->> (filter (fn [x] (> (count x) 0)) %)
                                               last
                                               Integer/parseInt)
                                          (catch Exception _ nil))
                                    relevant-data))
         overall-sample-count (apply + sample-counts)
         avg-space-allocated (/ (* overall-sample-count space-step) (float iterations))]
     (one-time-tear-down-fn args)
     (return-map [avg-space-allocated])))
