(ns sugot.app.staging-test
  (:require [clojure.test :refer :all]
            [sugot.app.staging :refer :all]
            [sugot.lib :as l]
            [sugot.mocks :as mocks]
            [sugot.event :as event]))

(deftest EntityDamageEvent-test
  (let [player (mocks/player "dummy-player")
        event (reify
                mocks/Entity (getEntity [this] player)
                mocks/Cause (getCause [this] nil))]
    ; TODO this is not really a test
    (is (event/cancelled? EntityDamageEvent event))))
