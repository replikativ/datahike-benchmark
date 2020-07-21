(ns result-presentation.plots.template
  (:require [clojure.string :as s]))


(defn plot [description]
  (let [{:keys [title vals x y color selections zero-y]} description
        tooltip (str "{\"" (:title x) "\":datum." (name (:key x)) ", "
                     "\"" (:title y) "\":datum." (name (:key y)) ", "
                     "\"" (:title color) "\":datum." (name (:key color)) ", "
                     (s/join ", " (map #(str "\"" (:title %) "\":datum." (name (:key %))) ""
                                       selections))
                     "}")]
    {:title   {:text  title
               :frame "group"}
     :width   600
     :height  400
     :padding 50

     :signals (conj (mapv #(hash-map :name (name (:key %))
                                     :value (:init %)
                                     :bind {:options (vec (sort (set (map (:key %) vals))))
                                            :input   "select"
                                            :name    (str (:title %) ":   ")})
                          selections)
                    {:name  "errorbars"
                     :value true
                     :bind  {:input "checkbox"
                             :name  "Show error bars:  "}})

     :data    [{:name   "results"
                :values vals}
               {:name      "specific"
                :source    "results"
                :transform (conj (mapv #(hash-map :type "filter"
                                                  :expr (str "datum." (name (:key %))
                                                             " === " (name (:key %))))
                                       selections)
                                 {:type "formula", :as "lo", :expr "datum.mean - datum.sd"}
                                 {:type "formula", :as "hi", :expr "datum.mean + datum.sd"})}]

     :scales  [{:name   "x"
                :type   "linear"
                :round  true
                :nice   true
                :zero   true
                :domain {:data "specific", :field (name (:key x))},
                :range  "width"}
               {:name   "y"
                :type   "linear"
                :round  true
                :nice   true
                :zero   zero-y
                :domain {:data "specific" :fields [(name (:key y)), "hi", "lo"]}
                :range  "height"}
               {:name   "color"
                :type   "ordinal"
                :range  "category"
                :domain {:data "results" :field (name (:key color))}}]

     :legends [{:fill   "color"
                :title  (:title color)
                :encode {:symbols {:enter {:fillOpacity {:value 1}}},
                         :labels  {:update {:text {:field "value"}}}}}]
     :axes    [{:scale        "x"
                :grid         true
                :orient       "bottom"
                :titlePadding 5
                :title        (:title x)}
               {:scale        "y"
                :grid         true
                :orient       "left"
                :titlePadding 5
                :title        (:title y)}]

     :marks   [{:type "group"
                :from {:facet
                       {:name    "series"
                        :data    "specific"
                        :groupby [(name (:key color))]}}
                :marks
                      [{:type "rect"
                        :from {:data "series"}
                        :encode
                              {:enter  {:fill  {:scale "color" :field (name (:key color))}
                                        :width {:value 1}}
                               :update {:x           {:scale "x" :field (name (:key x)) :band 0.5}
                                        :y           {:scale "y" :field "lo"}
                                        :y2          {:scale "y" :field "hi"}
                                        :fillOpacity {:signal "errorbars"}}}}
                       {:type "rect"
                        :from {:data "series"}
                        :encode
                              {:enter  {:fill   {:scale "color" :field (name (:key color))}
                                        :height {:value 1}}
                               :update {:x           {:scale "x" :field (name (:key x)) :band 0.5 :offset -3}
                                        :x2          {:scale "x" :field (name (:key x)) :band 0.5 :offset 4}
                                        :y           {:scale "y" :field "lo"}
                                        :fillOpacity {:signal "errorbars"}}}}
                       {:type "rect"
                        :from {:data "series"}
                        :encode
                              {:enter  {:fill   {:scale "color" :field (name (:key color))}
                                        :height {:value 1}}
                               :update {:x           {:scale "x" :field (name (:key x)) :band 0.5 :offset -3}
                                        :x2          {:scale "x" :field (name (:key x)) :band 0.5 :offset 4}
                                        :y           {:scale "y" :field "hi"}
                                        :fillOpacity {:signal "errorbars"}}}}

                       {:type "symbol"
                        :from {:data "series"}
                        :encode
                              {:update {:x      {:scale "x" :field (name (:key x))}
                                        :y      {:scale "y" :field (name (:key y))}
                                        :fill   {:scale "color" :field (name (:key color))}
                                        :update {:tooltip {:signal tooltip}}
                                        :hover  {:fillOpacity   {:value 0.5}
                                                 :strokeOpacity {:value 0.5}}}}}
                       {:type "line"
                        :from {:data "series"}
                        :encode
                              {:enter  {:x             {:scale "x" :field (name (:key x))}
                                        :y             {:scale "y" :field (name (:key y))}
                                        :stroke        {:scale "color" :field (name (:key color))}
                                        :shape         "circle"
                                        :strokeWidth   {:value 2}
                                        :strokeOpacity {:value 1}}
                               :update {:tooltip {:signal tooltip}}
                               :hover  {:strokeOpacity {:value 0.5}}}}]}]}) )