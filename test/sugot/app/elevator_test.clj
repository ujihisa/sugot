(ns sugot.app.elevator-test
  (:require [clojure.test :refer :all]
            [sugot.app.elevator :refer :all]
            [sugot.lib :as l]
            [sugot.mocks :as mocks]))

(defprotocol SugotPlayerInteractEvent
  (isCancelled [this])
  (getPlayer [this])
  (getAction [this])
  (getBlockFace [this])
  (getClickedBlock [this]))

(deftest PlayerInteractEvent-test
  (let [event (reify SugotPlayerInteractEvent
                (isCancelled [this] false)
                (getPlayer [this] 1)
                (getAction [this] 1)
                (getBlockFace [this] 1)
                (getClickedBlock [this] 1))]
    (with-redefs [l/send-message (fn [p m] :ok)]
      (is (= nil (PlayerInteractEvent event))))))
