(ns datahike-benchmark.db.datascript-test
  (:require [clojure.test :refer [is deftest]]
            [datahike-benchmark.db.api :as db]))

(deftest test-datascript
  (let [config (db/config :datascript)
        conn (db/connect :datascript config)]
    (is (not (nil? conn)))
    (db/release :datascript conn)))
