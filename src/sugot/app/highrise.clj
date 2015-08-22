(ns sugot.app.highrise
  (:require [clojure.string :as s]
            [sugot.lib :as l])
  (:import [org.bukkit Bukkit]
           [org.bukkit.event.entity CreatureSpawnEvent$SpawnReason]))

(defn CreatureSpawnEvent [event]
  (let [entity (.getEntity event)
        reason (.getSpawnReason event)
        l (.getLocation event)]
    ; You can't use `case` for Java enum
    (condp = reason
      CreatureSpawnEvent$SpawnReason/NATURAL
      (when (<= 100 (.getY l))
        (.setCancelled event true))
      :else)))
