(ns sugot.app.staging-test
  (:require [midje.sweet :refer :all]
            [sugot.app.staging :refer :all]
            [sugot.lib :as l]
            [sugot.mocks :as mocks]
            [sugot.event :as event])
  (:import [org.bukkit.event.entity EntityDamageEvent$DamageCause]
           [org.bukkit Material]))

(fact EntityDamageEvent-test
  (let [loc (mocks/location "anywhere" 10 20 30 {[10 20 30] (mocks/block Material/AIR 0)})
        player (mocks/player "dummy-player" loc)
        event (reify
                mocks/Entity (getEntity [this] player)
                mocks/Cause (getCause [this] EntityDamageEvent$DamageCause/SUFFOCATION))]
    (with-redefs-fn {#'sugot.app.staging/player? (constantly true)}
                    #(event/cancelled? EntityDamageEvent event))
    => true))
