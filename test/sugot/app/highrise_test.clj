(ns sugot.app.highrise-test
  (:require [clojure.test :refer :all]
            [sugot.app.highrise :refer :all]
            [sugot.lib :as l]
            [sugot.mocks :as mocks])
  (:import [org.bukkit Location]))

(deftest CreatureSpawnEvent-test
  (let [reason org.bukkit.event.entity.CreatureSpawnEvent$SpawnReason/NATURAL
        world (mocks/world "world")]
    (testing "prevent spawning monster at high space"
      (with-redefs [guardian? (fn [entity] false)
                    prismarine? (fn [entity] false)]
        (is (let [cancelled (ref false)
                  event (reify
                          mocks/SugotCreatureSpawnEvent
                          (getEntity [this] nil)
                          (getSpawnReason [this] reason)
                          mocks/Location
                          (getLocation [this] (mocks/location world 0 120 0))
                          mocks/Cancel
                          (setCancelled [this bool]
                            (dosync
                              (ref-set cancelled true)))
                          (isCancelled [this]
                            @cancelled))]
              (CreatureSpawnEvent event)
              (.isCancelled event)))))
    #_ (testing "prevent spawning on polished stone"
      (is (let [cancelled (ref false)
                event (reify
                        mocks/SugotCreatureSpawnEvent
                        (getEntity [this] nil)
                        (getSpawnReason [this] reason)
                        mocks/Location
                        (getLocation [this] (mocks/location world 0 60 0))
                        mocks/Cancel
                        (setCancelled [this bool]
                          (dosync
                            (ref-set cancelled true)))
                        (isCancelled [this]
                          @cancelled))]
            (CreatureSpawnEvent event)
            (.isCancelled event))))))
