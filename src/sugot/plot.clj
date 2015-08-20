(ns sugot.plot
  (:require [clojure.test :refer :all])
  (:import [clojure.lang PersistentArrayMap]))

(defn dot! [^PersistentArrayMap table ^Long x ^Long y ^Long z value]
  (assoc table [x y z] value))

(defn dot [^PersistentArrayMap table ^Long x ^Long y ^Long z value]
  {:pre [(not (table [x y z]))]}
  (dot! table x y z value))

(defn debug-render-2d
  "NOTE: This inserts () into the centre automatically"
  [^PersistentArrayMap table]
  (let [table (dot! table 0 0 0 "()")
        coll (keys table)
        xs (map first coll)
        zs (map last coll)]
    (doseq [x (range (apply min xs) (inc (apply max xs)))]
      (print "|")
      (doseq [z (range (apply min zs) (inc (apply max zs)))]
        (print (get table [x 0 z] "  ")))
      (println "|")))
  #_ (let [[min* max* (loop [min* nil max* nil coll (seq table)]
                     (if (empty? coll)
                       [min* max*]
                       (let [[[x y z] value] (first coll)
                             min* (if min*

                                    min*)]
                         (recur min* max* (rest coll)))))]]))

(defn line
  "https://en.wikipedia.org/wiki/Bresenham%27s_line_algorithm"
  [^PersistentArrayMap table ^Long x0 ^Long z0 ^Long x1 ^Long z1 value]
  (let [dx (Math/abs (- x1 x0))
        dz (Math/abs (- z1 z0))
        sx (if (< x0 x1) 1 -1)
        sz (if (< z0 z1) 1 -1)]
    (loop [table table x x0 z z0 err (- dx dz)]
      (let [table (dot table x 0 z value)]
        (if (and (= x x1) (= z z1))
          table
          (let [e2 (* 2 err)]
            (cond
              (and (> e2 (- dz)) (< e2 dx)) (recur table (+ x sx) (+ z sz) (+ (- err dz) dx))
              (> e2 (- dz)) (recur table (+ x sx) z (- err dz))
              (< e2 dx) (recur table x (+ z sz) (+ err dx))
              :else (recur table x z err))))))))

(defn circle
  "https://en.wikipedia.org/wiki/Midpoint_circle_algorithm"
  [^PersistentArrayMap table ^Long x0 ^Long z0 ^Long radius value]
  (loop [table table x radius z 0 decision-over-2 (- 1 x)]
    (if (< x z)
      table
      (let [table (-> table
                    (dot! (+ x x0) 0 (+ z z0) value)
                    (dot! (+ z x0) 0 (+ x z0) value)
                    (dot! (+ (- x) x0) 0 (+ z z0) value)
                    (dot! (+ (- z) x0) 0 (+ x z0) value)
                    (dot! (+ (- x) x0) 0 (+ (- z) z0) value)
                    (dot! (+ (- z) x0) 0 (+ (- x) z0) value)
                    (dot! (+ x x0) 0 (+ (- z) z0) value)
                    (dot! (+ z x0) 0 (+ (- x) z0) value)
                    )]
        (if (<= decision-over-2 0)
          (recur table x (inc z) (+ decision-over-2 (* 2 z) 1))
          (recur table (dec x) (inc z) (+ decision-over-2 (* 2 (- z (dec x))) 1)))))))
#_ (prn (debug-render-2d (line {} 2 3 10 12 :x)))
#_ (prn (debug-render-2d (circle {} 4 5 7 :x)))

(deftest dot-test
  (testing "normal case"
    (is (= {[0 0 0] :something, [1 2 3] :new}
           (dot {[0 0 0] :something} 1 2 3 :new))))
  (testing "fails with existing data"
    (is (thrown? AssertionError
                 (dot {[0 0 0] :something} 0 0 0 :new)))))

(deftest dot!-test
  (testing "normal case"
    (is (= {[0 0 0] :something, [1 2 3] :new}
           (dot! {[0 0 0] :something} 1 2 3 :new))))
  (testing "ignores existing data"
    (is (= {[0 0 0] :new}
           (dot! {[0 0 0] :something} 0 0 0 :new)))))

(deftest line-test
  (testing "normal case"
    (is (= {[3 0 4] :x, [4 0 5] :x, [6 0 8] :x, [2 0 3] :x, [6 0 7] :x, [7 0 9] :x, [9 0 11] :x, [10 0 12] :x, [8 0 10] :x, [5 0 6] :x}
           (line {} 2 3 10 12 :x)))))

(deftest circle-test
  (testing "normal case"
    (is (= {[-6 0 -4] :x, [-6 0 10] :x, [-8 0 6] :x, [1 0 -7] :x, [-5 0 11] :x, [8 0 12] :x, [12 0 2] :x,
            [8 0 -6] :x, [11 0 -2] :x, [0 0 13] :x, [-8 0 1] :x, [1 0 13] :x, [-8 0 4] :x, [-5 0 -5] :x,
            [-1 0 -7] :x, [-7 0 -3] :x, [12 0 0] :x, [0 0 -7] :x, [12 0 6] :x, [-3 0 12] :x, [12 0 7] :x,
            [-8 0 -1] :x, [-4 0 -6] :x, [-7 0 -2] :x, [3 0 13] :x, [-7 0 8] :x, [-8 0 2] :x, [12 0 4] :x,
            [6 0 13] :x, [-1 0 13] :x, [-8 0 3] :x, [10 0 -4] :x, [-2 0 -7] :x, [9 0 11] :x, [-3 0 -6] :x,
            [7 0 12] :x, [-7 0 9] :x, [-2 0 13] :x, [9 0 -5] :x, [-4 0 12] :x, [4 0 -7] :x, [12 0 5] :x,
            [12 0 -1] :x, [10 0 10] :x, [3 0 -7] :x, [2 0 13] :x, [5 0 -7] :x, [4 0 13] :x, [-8 0 7] :x,
            [7 0 -6] :x, [-8 0 5] :x, [5 0 13] :x, [11 0 8] :x, [-8 0 0] :x, [12 0 3] :x, [6 0 -7] :x,
            [11 0 9] :x, [2 0 -7] :x, [11 0 -3] :x, [12 0 1] :x}
           (circle {} 2 3 10 :x)))))

(run-tests)
