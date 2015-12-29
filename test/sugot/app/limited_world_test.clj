(ns sugot.app.limited-world-test
  (:require [midje.sweet :refer :all]
            [sugot.app.limited-world :refer :all]
            [sugot.lib :as l]
            [sugot.models :as m]
            [sugot.mocks :as mocks]))

#_ (deftest PlayerMoveEvent-test
  (testing "send-message for the player if it's very far"
    (let [l (mocks/location (mocks/world "world") 400 50 0)
          event (reify
                  mocks/IgetPlayer
                  (getPlayer [this] (mocks/player "dummy-player"))
                  mocks/PlayerMoveEvent
                  (getFrom [this] nil)
                  (getTo [this] l))]
      (with-redefs [rand-int (fn [_] 0)
                    l/send-message (fn [p message]
                                     :ok)]
        (is (= :ok
               (PlayerMoveEvent event)))))))
