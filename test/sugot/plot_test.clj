(ns sugot.plot-test
  (:require [clojure.test :refer :all]
            [sugot.plot :refer :all]))

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
