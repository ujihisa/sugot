(ns sugot.app.highrise-test
  (:require [clojure.test :refer :all]
            [sugot.app.highrise :refer :all]
            [sugot.lib :as l])
  (:import [org.bukkit Location]))

(defrecord SugotWorld [^String getName])

(defrecord SugotLocation [^SugotWorld getWorld getX getY getZ])

(defprotocol SugotCreatureSpawnEvent
  (getEntity [this])
  (getSpawnReason [this])
  (getLocation [this])
  (isCancelled [this])
  (setCancelled [this bool]))

(deftest a-test
  (testing "prevent spawning monster at high space"
    (let [entity nil
          reason org.bukkit.event.entity.CreatureSpawnEvent$SpawnReason/NATURAL
          world (SugotWorld. "world")
          event (reify SugotCreatureSpawnEvent
                  (getEntity [this] entity)
                  (getSpawnReason [this] reason)
                  (getLocation [this] (SugotLocation. world 0 0 0))
                  (isCancelled [this] true)
                  (setCancelled [this bool] nil))]
      (is (= true
             (do
               (CreatureSpawnEvent event)
               (.isCancelled event)))))))
