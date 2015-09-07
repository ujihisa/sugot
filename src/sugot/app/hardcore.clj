(ns sugot.app.hardcore
  (:require [sugot.lib :as l]
            [sugot.block :as b]
            [sugot.world])
  (:import [org.bukkit Bukkit Server WorldCreator Material Location]
           [org.bukkit.entity ArmorStand Monster Blaze Egg SmallFireball]
           [org.bukkit.event.block Action]
           [org.bukkit.event.entity CreatureSpawnEvent$SpawnReason]
           [org.bukkit.event.entity EntityDamageEvent$DamageCause]
           [org.bukkit.inventory ItemStack]
           [org.bukkit.enchantments Enchantment]))

(defn PlayerDropItemEvent [event]
  (when (= "Magic Compass"
           (some-> event
                   .getItemDrop .getItemStack .getItemMeta .getDisplayName))
    (.setCancelled event true)
    (l/send-message (.getPlayer event) "[HARDCORE] You should keep it for going back home!")))

(defn- hardcore-world-exist? []
  (.isDirectory (clojure.java.io/as-file "hardcore")))

(defn in-hardcore? [loc]
  (= "hardcore" (.getName (.getWorld loc))))

(defn BlockPlaceEvent [event]
  nil)

(defn EntityDamageEvent [event]
  (try
    (let [entity (.getEntity event)
          cause (.getCause event)]
      (when (in-hardcore? (.getLocation entity))
        (cond
          (instance? ArmorStand entity)
          (.setCancelled event true)

          (instance? Monster entity)
          (condp = cause
            EntityDamageEvent$DamageCause/FIRE_TICK
            (.setCancelled event true)
            nil))))
    (catch Exception e (.printStackTrace e))))

(defn EntityDeathEvent [event]
  )

(defn- hardcore-players []
  (seq (filter #(in-hardcore? (.getLocation %))
               (Bukkit/getOnlinePlayers))))

(defn CreatureSpawnEvent [event]
  (let [entity (.getEntity event)
        reason (.getSpawnReason event)
        l (.getLocation event)]
    ; You can't use `case` for Java enum
    (when (and (hardcore-world-exist?)
               (in-hardcore? l)
               (instance? Monster entity)
               (= CreatureSpawnEvent$SpawnReason/NATURAL reason))
      (l/later 0
        (dotimes [_ 2]
          (let [loc (doto (.clone l)
                      (.add (rand-nth [-0.5 0.5]) 0.5 (rand-nth [-0.5 0.5])))
                klass (if (= 0 (rand-int 3))
                        Blaze
                        (class entity))
                monster
                (sugot.world/spawn loc klass)]
            (when-let [players (hardcore-players)]
              (.setTarget monster (rand-nth players)))))))))

(defn- launch-projectile [source projectile velocity]
  (.launchProjectile source projectile velocity))

(defn ProjectileLaunchEvent [event]
  (try
    (let [projectile (.getEntity event)
          shooter (.getShooter projectile)]
      (when (and
              (instance? Blaze shooter)
              (instance? SmallFireball projectile))
        (when-let [target (.getTarget shooter)]
          (.setCancelled event true)
          (let [velocity
                (.normalize (.getDirection
                              (.subtract (.getLocation shooter)
                                         (.getLocation target))))
                #_ egg #_(launch-projectile shooter org.bukkit.entity.Arrow velocity)]
            (.setVelocity shooter velocity)
            #_ (.setBounce egg true)))))
    (catch Exception e (.printStackTrace e))))

#_ (def interesting-seeds
  [#_7352190906321318631 ; http://epicminecraftseeds.com/stronghold-in-ravine-1-8x/
   #_ 3083175 ; http://epicminecraftseeds.com/spawn-beside-jungle-temple/
   516687594611420526 ; http://epicminecraftseeds.com/minecraft-village-seed-great-loot/
   5574457897082764526 ; http://epicminecraftseeds.com/sweet-savanna-m-above-the-clouds-minecraft-1-8-seed/
   ])

(defn random-xz [radius]
  {:pre [(< 0 radius)]}
  (let [x (- (rand-int (* 2 radius)) radius)
        z (Math/round (* (Math/sqrt (- (* radius radius) (* x x)))
                         (rand-nth [1 -1])))]
    [x z]))

(defn create []
  {:pre [(not (hardcore-world-exist?))]}
  (let [world-creator (-> (WorldCreator. "hardcore")
                        (.copy (Bukkit/getWorld "world"))
                        (.seed (rand-int Integer/MAX_VALUE)
                               #_ (first interesting-seeds)))
        hardcore-world (.createWorld world-creator)]
    (.setTime hardcore-world 21000)
    (let [[goal-x goal-z] (random-xz (+ 100 (rand-int 100)))
          goal-y (.getHighestBlockYAt hardcore-world goal-x goal-z)]
      (.setSpawnLocation hardcore-world goal-x (inc goal-y) goal-z)
      (l/later 0
        (b/set-block! (.getBlockAt hardcore-world goal-x (dec goal-y) goal-z)
                      Material/BEDROCK 0)
        (-> (sugot.world/spawn (Location. hardcore-world
                                          (+ 0.5 goal-x)
                                          goal-y
                                          (+ 0.5 goal-z))
                               ArmorStand)
          (.setHelmet (ItemStack. Material/OBSIDIAN 1)))))
    (let [init-y (.getHighestBlockYAt hardcore-world 0 0)]
      (l/later 0
        (doseq [x (range -1 2)
                z (range -1 2)]
          (b/set-block! (.getBlockAt hardcore-world x (dec init-y) z)
                        Material/OBSIDIAN 0)
          (b/set-block! (.getBlockAt hardcore-world x init-y z)
                        Material/TORCH 0))))))

(defn hardcore-world []
  (Bukkit/getWorld "hardcore"))

(defn enter-hardcore [player]
  {:pre [(hardcore-world)]}
  (let [init-loc
        (Location.
          (hardcore-world) 0.5 (inc (.getHighestBlockYAt (hardcore-world) 0 0)) 0.5)]
    (.teleport player init-loc)
    (l/broadcast-and-post-lingr
      (format "[HARDCORE] %s entered to hardcore world. (seed: \"%d\", biome: %s)"
              (.getName player)
              (.getSeed (hardcore-world))
              (.name (.getBiome (hardcore-world) 0 0))))))

(defn enter-satisfy? [player]
  (when-let [item-stack (.getItemInHand player)]
    (when (and (= "world" (.getName (.getWorld (.getLocation player))))
               (= Material/PAPER (.getType item-stack))
               (= 1 (.getAmount item-stack)))
      (when-let [armour-stand (some #(when (instance? ArmorStand %) %)
                                    (.getNearbyEntities player 0 0 0))]
        (= Material/PUMPKIN (.getType (.getHelmet armour-stand)))))))

(defn leave-satisfy? [player]
  (when-let [item-stack (.getItemInHand player)]
    (when (and (in-hardcore? (.getLocation player))
               (= Material/COMPASS (.getType item-stack))
               (= 1 (.getAmount item-stack)))
      (when-let [armour-stand (some #(when (instance? ArmorStand %) %)
                                    (.getNearbyEntities player 0 0 0))]
        (= Material/OBSIDIAN (.getType (.getHelmet armour-stand)))))))

(defn dir-delete-recursively
  "From https://gist.github.com/edw/5128978"
  [fname]
  (let [func (fn [func f]
               (when (.isDirectory f)
                 (doseq [f2 (.listFiles f)]
                   (func func f2)))
               (clojure.java.io/delete-file f))]
    (func func (clojure.java.io/file fname))))

(defn garbage-collection []
  (if (and
        (hardcore-world-exist?)
        (empty? (hardcore-players)))
    (if-let [hardcore-world (hardcore-world)]
      (let [folder (.getWorldFolder hardcore-world)]
        (if (Bukkit/unloadWorld "hardcore" false)
          (do
            (try
              (dir-delete-recursively (.getAbsolutePath folder))
              (catch Exception e (.printStackTrace e)))
            :unloadWorld-succeeded)
          :unloadWorld-failed))
      :precondition-failed--weird)
    :precondition-failed--normal))

(defn leave-hardcore [player]
  {:pre [(in-hardcore? (.getLocation player))]}
  (let [to (some identity [(.getBedSpawnLocation player)
                           (.getSpawnLocation (Bukkit/getWorld "world"))])]
    (l/broadcast-and-post-lingr (format "[HARDCORE] %s left from the hardcore world."
                                        (.getName player)))
    (.teleport player to)
    (l/later 0
      (garbage-collection))))

(defn PlayerInteractEvent [event]
  (when-let [player (.getPlayer event)]
    (let [action (.getAction event)]
      (when (contains? #{Action/RIGHT_CLICK_AIR Action/RIGHT_CLICK_BLOCK} action)
        (cond
          (enter-satisfy? player)
          (let [compass (doto (ItemStack. Material/COMPASS 1)
                          (.addUnsafeEnchantment Enchantment/DURABILITY 1)
                          (l/set-display-name "Magic Compass"))]
            (.setItemInHand player compass)
            (when-not (hardcore-world-exist?)
              (l/broadcast "[HARDCORE] (Creating world...)")
              (create))
            (l/send-message player "[HARDCORE] Go!")
            (enter-hardcore player))

          (leave-satisfy? player)
          (do
            (.setItemInHand player (ItemStack. Material/PAPER 1))
            (leave-hardcore player)))))))
