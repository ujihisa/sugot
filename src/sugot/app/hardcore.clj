(ns sugot.app.hardcore
  (:require [sugot.lib :as l]
            [sugot.block :as b]
            [sugot.world])
  (:import [org.bukkit Bukkit Server WorldCreator Material]
           [org.bukkit.entity ArmorStand]
           [org.bukkit.event.block Action]
           [org.bukkit.event.entity CreatureSpawnEvent$SpawnReason]))

(defn- hardcore-world-exist? []
  (.isDirectory (clojure.java.io/as-file "hardcore")))

(defn in-hardcore? [loc]
  (= "hardcore" (.getName (.getWorld loc))))

(defn BlockPlaceEvent [event]
  nil)

(defn CreatureSpawnEvent [event]
  (let [entity (.getEntity event)
        reason (.getSpawnReason event)
        l (.getLocation event)]
    ; You can't use `case` for Java enum
    (when (and (hardcore-world-exist?)
               (in-hardcore? l)
               (instance? org.bukkit.entity.Monster entity)
               (= CreatureSpawnEvent$SpawnReason/NATURAL reason))
      (dotimes [_ 2]
        (let [monster
              (sugot.world/spawn (doto (.clone l)
                                   (.add 0.0 0.5 0.0))
                                 (class entity))]
          (when-let [player
                     (rand-nth (filter in-hardcore?
                                       (map #(.getLocation %) (Bukkit/getOnlinePlayers))))]
            (.target monster player)))))))

(def interesting-seeds
  [7352190906321318631 ; http://epicminecraftseeds.com/stronghold-in-ravine-1-8x/
   3083175 ; http://epicminecraftseeds.com/spawn-beside-jungle-temple/
   516687594611420526 ; http://epicminecraftseeds.com/minecraft-village-seed-great-loot/
   5574457897082764526 ; http://epicminecraftseeds.com/sweet-savanna-m-above-the-clouds-minecraft-1-8-seed/
   ])

(defn create []
  {:pre [(not (hardcore-world-exist?))]}
  (let [world-creator (-> (WorldCreator. "hardcore")
                        (.copy (Bukkit/getWorld "world"))
                        (.seed #_(rand-int 8000000000000000000)
                               (first interesting-seeds)))
        hardcore-world (.createWorld world-creator)]
    (.setTime hardcore-world 21000)
    (let [spawn-loc (.getSpawnLocation hardcore-world)
          x (.getX spawn-loc)
          z (.getZ spawn-loc)
          highest-y (.getHighestBlockYAt hardcore-world x z)]
      (b/set-block (.getBlockAt hardcore-world x highest-y z)
                   Material/OBSIDIAN
                   0)
      (.setSpawnLocation hardcore-world x (inc highest-y) z))))

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
    (when (and (= "world" (.getName (.getWorld (.getLocation player))))
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
        ; TODO Replace current item with Map for this world
        #_ (.setItemInHand player (ItemStack. ))
        (l/send-message player "[HARDCORE] OK")
        (when-not (hardcore-world-exist?)
          (create))
        (enter player)))))

(defn garbage-collection []
  (when (and
          (hardcore-world-exist?)
          (not (some in-hardcore?
                     (map #(.getLocation %) (Bukkit/getOnlinePlayers)))))
    (Bukkit/unloadWorld "hardcore" false)
    ; TODO remove the dir
    ))

(defn return-back [player]
  {:pre [(in-hardcore? (.getLocation player))]}
  (let [to (some identity [(.getBedSpawnLocation player)
                           (.getSpawnLocation (Bukkit/getWorld "world"))])]
    (l/broadcast-and-post-lingr (format "[HARDCORE] %s returns back to the original world."
                                        (.getName player)))
    (.teleport player to)))
