(ns sugot.block-test
  (:require [clojure.test :refer :all]
            [sugot.block :refer :all])
  (:import [org.bukkit Material]))

(defrecord SugotBlock [getType getData])

(deftest natural-block?-test
  (testing "I feel them natural or not natural"
    (is (natural-block? (SugotBlock. Material/DIRT 0)))
    (is (natural-block? (SugotBlock. Material/STONE 0)))
    (is (not (natural-block? (SugotBlock. Material/STONE 2))))))
