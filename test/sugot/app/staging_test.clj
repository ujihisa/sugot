(ns sugot.app.staging-test
  (:require [clojure.test :refer :all]
            [sugot.app.staging :refer :all]
            [sugot.lib :as l]
            [sugot.mocks :as mocks]))

(deftest EntityDamageEvent-test
  (let [player (mocks/player "dummy-player")
        event (reify
                mocks/Entity (getEntity [this] player)
                mocks/Cause (getCause [this] nil)
                mocks/Cancel
                (isCancelled [this] nil)
                (setCancelled [this bool] nil))]
    ; TODO this is not really a test
    (is (not= 123123
              (EntityDamageEvent event)))))
