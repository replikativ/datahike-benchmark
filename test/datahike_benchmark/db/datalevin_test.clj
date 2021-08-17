(ns datahike-benchmark.db.datalevin-test
  (:require [clojure.test :refer [is deftest]]
            [datahike-benchmark.db.api :as db]))

(deftest test-datalevin
  (let [config (db/config :datalevin)
        conn (db/connect :datalevin config)]
    (is (not (nil? conn)))
    (db/release :datalevin conn)))
