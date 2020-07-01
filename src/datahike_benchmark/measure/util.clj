(ns datahike-benchmark.measure.util)

(defn mean [samples] (/ (reduce + samples) (count samples)))

(defn sd [samples]
  (if (< (count samples) 2)
    0
    (Math/sqrt (/ (reduce + (map #(* % %) (map - samples (repeat (mean samples)))))
                  (- (count samples) 1)))))

(defn median [samples]
  (let [ns (sort samples)
        c (count samples)
        mid (bit-shift-right c 1)]
    (if (odd? c)
      (nth ns mid)
      (/ (+ (nth ns mid) (nth ns (dec mid))) 2))))
