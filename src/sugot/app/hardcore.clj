(ns sugot.app.hardcore
  (:require [sugot.lib :as l])
  (:import [org.bukkit Bukkit Server WorldCreator]))

(defn TODO [player-name]
  (when-let [player (Bukkit/getPlayer player-name)]
    #_ (let [world-creator (-> (WorldCreator. "hardcore")
                          (.copy (Bukkit/getWorld "world"))
                          (.seed (rand-int 10000)))
          hardcore-world (.createWorld world-creator)]
      (.setTime hardcore-world 22000)
      (let [spawn-loc (.getSpawnLocation hardcore-world)
            highest-y (.getHighestBlockYAt hardcore-world (.getX spawn-loc) (.getZ spawn-loc))]
        (when (not= highest-y (.getY spawn-loc))
          (.setSpawnLocation hardcore-world
                             (.getX spawn-loc)
                             highest-y
                             (.getZ spawn-loc)))))
    #_ (let [hardcore-world (Bukkit/getWorld "hardcore")
          spawn-loc (.getSpawnLocation hardcore-world)]
      (.teleport player spawn-loc)
      (.teleport (Bukkit/getPlayer "mozukusoba") spawn-loc)
      (.teleport (Bukkit/getPlayer "kamichidu") spawn-loc))
    #_ (.teleport player (.getBedSpawnLocation player))
    #_ (Bukkit/unloadWorld "hardcore" false)))


#_ (l/later 0 (TODO "kamichidu"))
