(ns sugot.app.highrise-test
  (:require [clojure.test :refer :all]
            [sugot.app.highrise :refer :all]
            [sugot.lib :as l])
  (:import [org.bukkit Location]))

(defn- create-entity [l entity-class]
  #_ (let [world (.getWorld l)]
    (.spawn world l entity-class)))

(declare world)
#_ (def world
  (org.bukkit.Bukkit/getWorld "world"))

(deftest a-test
  (testing "prevent spawning monster at high space"
    (let [entity (create-entity (comment Location. world 0 0 0) org.bukkit.entity.Zombie)
          reason org.bukkit.event.entity.CreatureSpawnEvent$SpawnReason/NATURAL
          event (org.bukkit.event.entity.CreatureSpawnEvent. entity reason)]
      #_ (is (= 0
             (do
               (CreatureSpawnEvent event)
               (.isCancelled event)))))))
