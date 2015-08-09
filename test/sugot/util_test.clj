(ns sugot.util-test
  (:require [clojure.test :refer :all]
            [sugot.util :refer :all]))

(deftest sec-test
  (testing "TODO"
    (is (= 20 (sec 1.001)))))

(deftest later-test
  (testing "nanido"
    (is (= 1 1))))
