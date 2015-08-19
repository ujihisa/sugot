(ns sugot.models-test
  (:require [clojure.test :refer :all]
            [sugot.models :refer :all])
  (:import [sugot.models Loc B P]
           [org.bukkit Material]))

(deftest Loc-test
  (testing "TODO"
    #_ (is (= 1 (B. Material/DIRT 0 (Loc. "world" 0 1 2))))
    #_ (is (= 1 (Loc. "world" 1.0 2.0 3)))))
