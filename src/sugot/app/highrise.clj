(ns sugot.app.highrise
  (:require [clojure.string :as s]
            [sugot.lib :as l]
            [sugot.block :as b])
  (:import [org.bukkit Bukkit Material]
           [org.bukkit.event.entity CreatureSpawnEvent$SpawnReason]
           [org.bukkit.entity Guardian]))

(defn guardian? [entity]
  (instance? Guardian entity))

(defn prismarine? [block]
  (= Material/PRISMARINE (.getType block)))

(defn CreatureSpawnEvent [event]
  (let [entity (.getEntity event)
        reason (.getSpawnReason event)
        l (.getLocation event)]
    ; You can't use `case` for Java enum
    (when (= "world" (-> l .getWorld .getName))
      (when (contains? #{CreatureSpawnEvent$SpawnReason/NATURAL
                         CreatureSpawnEvent$SpawnReason/REINFORCEMENTS}
                       reason)
        (when (or (<= 100 (.getY l))
                  (b/polish-stone? (b/from-loc l 0 -1 0)))
          (.setCancelled event true))
        (when (and (not (guardian? entity))
                   (prismarine? (b/from-loc l 0 -1 0)))
          (.setCancelled event true))))))
