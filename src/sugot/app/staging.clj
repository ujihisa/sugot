(ns sugot.app.staging
  (:require [clojure.string :as s]
            [sugot.lib :as l])
  (:import [org.bukkit.event.entity CreatureSpawnEvent$SpawnReason]))

(defn PlayerBedEnterEvent [event p]
  #_ (let [player (:orig p)
        world (-> player .getLocation .getWorld)]
    (when (< 12541 (.getTime world) 23458)
      (l/broadcast-and-post-lingr "[BED] Good morning!")
      (.setTime world 0))))

(defn PlayerLoginEvent [event p]
  (.setSleepingIgnored (:orig p) true))

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
