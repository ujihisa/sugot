(ns sugot.app.staging
  (:require [clojure.string :as s]
            [sugot.lib :as l])
  (:import [org.bukkit.event.entity CreatureSpawnEvent$SpawnReason]))

(defn PlayerLoginEvent [event]
  (.setSleepingIgnored (.getPlayer event) true))

(defn CreatureSpawnEvent
  "bigger slimes"
  [event]
  (let [entity (.getEntity event)
        reason (.getSpawnReason event)
        l (.getLocation event)]
    (when (and
            (= "world" (-> l .getWorld .getName))
            (= CreatureSpawnEvent$SpawnReason/NATURAL reason)
            (instance? org.bukkit.entity.Slime entity))
      (.setSize entity (+ 2 (.getSize entity))))))
