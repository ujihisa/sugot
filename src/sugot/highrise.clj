(ns sugot.highrise
  (:require [clojure.string :as s]
            [sugot.lib :as l])
  (:import [org.bukkit Bukkit]
           [org.bukkit.event.entity CreatureSpawnEvent$SpawnReason]))

#_ (defn CreatureSpawnEvent [event]
  (let [entity-type (.getEntityType event)
        reason (.getSpawnReason event)]
    (case reason
      CreatureSpawnEvent$SpawnReason$NATURAL
      :wow-natural
      nil)))
