(ns sugot.app.highrise
  (:require [clojure.string :as s]
            [sugot.lib :as l]
            [sugot.block :as b])
  (:import [org.bukkit Bukkit]
           [org.bukkit.event.entity CreatureSpawnEvent$SpawnReason]))

(defn CreatureSpawnEvent [event]
  (let [entity (.getEntity event)
        reason (.getSpawnReason event)
        l (.getLocation event)]
    ; You can't use `case` for Java enum
    (when (= "world" (-> l .getWorld .getName))
      (condp = reason
        CreatureSpawnEvent$SpawnReason/NATURAL
        (when (or (<= 100 (.getY l))
                  (b/polish-stone? (b/from-loc l 0 -1 0)))
          (.setCancelled event true))
        :else))))
