(ns sugot.app.highrise-test
  (:require [midje.sweet :refer :all]
            [sugot.app.highrise :refer :all]
            [sugot.lib :as l]
            [sugot.mocks :as mocks]
            [sugot.event :as event])
  (:import [org.bukkit Material]))

(facts CreatureSpawnEvent-test
  (let [reason org.bukkit.event.entity.CreatureSpawnEvent$SpawnReason/NATURAL
        world (mocks/world "world")]
    (fact "prevent spawning monster at high space"
      (with-redefs [guardian? (fn [entity] false)
                    prismarine? (fn [entity] false)]
        (let [event (reify
                      mocks/IgetEntity
                      (getEntity [this] nil)
                      mocks/SugotCreatureSpawnEvent
                      (getSpawnReason [this] reason)
                      mocks/IgetLocation
                      (getLocation [this] (mocks/location world 0 120 0)))]
          (event/cancelled? CreatureSpawnEvent event)
          => true)))
    (fact "prevent spawning on polished stone"
      (with-redefs [guardian? (fn [entity] false)
                    prismarine? (fn [entity] false)]
        (let [block-map {[0 59 0] (mocks/block Material/STONE 2)}
              event (reify
                      mocks/IgetEntity
                      (getEntity [this] nil)
                      mocks/SugotCreatureSpawnEvent
                      (getSpawnReason [this] reason)
                      mocks/IgetLocation
                      (getLocation [this]
                        (mocks/location world 0 60 0 block-map)))]
          (event/cancelled? CreatureSpawnEvent event))
        => true))))
