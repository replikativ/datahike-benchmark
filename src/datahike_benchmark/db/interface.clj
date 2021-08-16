(ns datahike-benchmark.db.interface)

(defmulti connect (fn [lib _] lib))

(defmulti transact (fn [lib _ _] lib))

(defmulti release (fn [lib _] lib))

(defmulti db (fn [lib _] lib))

(defmulti q (fn [lib _ _] lib))

(defmulti init (fn [lib _] lib))

(defmulti prepare-and-connect (fn [lib _ _ _] lib))

(defmulti delete (fn [lib _] lib))
