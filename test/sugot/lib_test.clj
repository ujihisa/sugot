(ns sugot.lib-test
  (:require [clojure.test :refer :all]
            [sugot.lib :refer :all]))

(deftest sec-test
  (testing "TODO"
    (is (= 20 (sec 1.001)))))

(deftest later-test
  (testing "nanido"
    (is (= 1 1))))
