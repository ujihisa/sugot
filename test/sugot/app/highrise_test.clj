(ns sugot.app.highrise-test
  (:require [clojure.test :refer :all]
            [sugot.app.highrise :refer :all]
            [sugot.lib :as l]
            [sugot.mocks :as mocks]
            [sugot.event :as event])
  (:import [org.bukkit Location Material]))

(deftest CreatureSpawnEvent-test
  (let [reason org.bukkit.event.entity.CreatureSpawnEvent$SpawnReason/NATURAL
        world (mocks/world "world")]
    (testing "prevent spawning monster at high space"
      (with-redefs [guardian? (fn [entity] false)
                    prismarine? (fn [entity] false)]
        (is (let [event (reify
                          mocks/SugotCreatureSpawnEvent
                          (getEntity [this] nil)
                          (getSpawnReason [this] reason)
                          mocks/Location
                          (getLocation [this] (mocks/location world 0 120 0)))]
              (event/cancelled? CreatureSpawnEvent event)))))
    (testing "prevent spawning on polished stone"
      (with-redefs [guardian? (fn [entity] false)
                    prismarine? (fn [entity] false)]
        (is (let [block-map {[0 59 0] (mocks/block Material/STONE 2)}
                  event (reify
                          mocks/SugotCreatureSpawnEvent
                          (getEntity [this] nil)
                          (getSpawnReason [this] reason)
                          mocks/Location
                          (getLocation [this]
                            (mocks/location world 0 60 0 block-map)))]
              (event/cancelled? CreatureSpawnEvent event)))))))
