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

(deftest from-loc-test
  (let [loc (mocks/location "world" 10 20 30
                            {[10 21 32] (mocks/block Material/DIRT 0)})
        result (from-loc loc 0 1 2)]
    (is (= 30 (.getZ loc)) "Make sure there's no side effect")
    (is result)
    (is (= Material/DIRT (.getType result)))
    (is (= 0 (.getData result)))))
