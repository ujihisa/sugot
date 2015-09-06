(ns sugot.app.hardcore
  (:require [sugot.lib :as l])
  (:import [org.bukkit Bukkit Server WorldCreator Material]
           [org.bukkit.entity ArmorStand]
           [org.bukkit.event.block Action]))

(defn exist? []
  (.isDirectory (clojure.java.io/as-file "hardcore")))

(defn create []
  {:pre [(not (exist?))]}
  (let [world-creator (-> (WorldCreator. "hardcore")
                        (.copy (Bukkit/getWorld "world"))
                        (.seed (rand-int 10000)))
        hardcore-world (.createWorld world-creator)]
    (.setTime hardcore-world 22000)
    (let [spawn-loc (.getSpawnLocation hardcore-world)
          x (.getX spawn-loc)
          z (.getZ spawn-loc)
          highest-y (.getHighestBlockYAt hardcore-world x z)]
      (.setSpawnLocation hardcore-world x highest-y z))))

(defn world []
  (Bukkit/getWorld "hardcore"))

(defn enter [player]
  (let [hardcore-world (world)
        spawn-loc (.getSpawnLocation hardcore-world)]
    (.teleport player spawn-loc)
    (l/broadcast-and-post-lingr
      (format "[HARDCORE] %s entered to hardcore world. (seed: %d)"
              (.getName player)
              (.getSeed hardcore-world)))))

(defn enter-satisfy? [player]
  (when-let [item-stack (.getItemInHand player)]
    (when (and (= "world" (.getWorld (.getLocation player)))
               (= Material/PAPER (.getType item-stack))
               (= 1 (.getAmount item-stack)))
      (when-let [armour-stand (some #(when (instance? ArmorStand %) %)
                                    (.getNearbyEntities player 0 0 0))]
        (= Material/PUMPKIN (.getType (.getHelmet armour-stand)))))))

(defn PlayerInteractEvent [event]
  (when-let [player (.getPlayer event)]
    (let [action (.getAction event)]
      (when (and
              (contains? #{Action/RIGHT_CLICK_AIR Action/RIGHT_CLICK_BLOCK} action)
              (enter-satisfy? player))
        (l/send-message player "[HARDCORE] OK")))))

#_ (defn TODO [player-name]
  (when-let [player (Bukkit/getPlayer player-name)]
    (let [hardcore-world (world)
          spawn-loc (.getSpawnLocation hardcore-world)]
      (.teleport player spawn-loc)
      (.teleport (Bukkit/getPlayer "mozukusoba") spawn-loc)
      (.teleport (Bukkit/getPlayer "kamichidu") spawn-loc))
    #_ (.teleport player (.getBedSpawnLocation player))
    #_ (Bukkit/unloadWorld "hardcore" false)))


#_ (l/later 0 (TODO "ujm"))
#_ (l/later 0 (TODO "mozukusoba"))
#_ (l/later 0 (TODO "kamichidu"))
