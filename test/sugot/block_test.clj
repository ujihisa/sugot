(ns sugot.block-test
  (:require [clojure.test :refer :all]
            [sugot.block :refer :all]
            [sugot.mocks :as mocks])
  (:import [org.bukkit Material]))

(deftest natural-block?-test
  (testing "I feel them natural or not natural"
    (is (natural-block? (mocks/block Material/DIRT 0)))
    (is (natural-block? (mocks/block Material/STONE 0)))
    (is (not (natural-block? (mocks/block Material/STONE 2))))))
