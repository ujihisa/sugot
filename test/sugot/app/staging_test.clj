(ns sugot.app.staging-test
  (:require [midje.sweet :refer :all]
            [sugot.app.staging :refer :all]
            [sugot.lib :as l]
            [sugot.mocks :as mocks]
            [sugot.event :as event]))

(fact EntityDamageEvent-test
  (let [player (mocks/player "dummy-player")
        event (reify
                mocks/Entity (getEntity [this] player)
                mocks/Cause (getCause [this] nil))]
    ; TODO this is not really a test
    (event/cancelled? EntityDamageEvent event)
    ; TODO fix this
    => false))
