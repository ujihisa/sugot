(ns sugot.app.hardcore
  (:require [sugot.lib :as l]
            [sugot.block :as b]
            [sugot.world])
  (:import [org.bukkit Bukkit Server WorldCreator Material Location Sound
            Effect]
           [org.bukkit.block Biome]
           [org.bukkit.entity ArmorStand Monster Blaze Egg SmallFireball
            Player LivingEntity Projectile Arrow Snowball Guardian Creeper Silverfish Horse
            Pig Skeleton]
           [org.bukkit.event.block Action]
           [org.bukkit.event.entity CreatureSpawnEvent$SpawnReason]
           [org.bukkit.event.entity EntityDamageEvent$DamageCause]
           [org.bukkit.inventory ItemStack]
           [org.bukkit.enchantments Enchantment]
           [org.bukkit.util Vector]))

(defn- hardcore-world-exist? []
  (.isDirectory (clojure.java.io/as-file "hardcore")))

(defn loc-in-hardcore? [loc]
  (= "hardcore" (.getName (.getWorld loc))))

(defn- hardcore-world-dir []
  (format "%s/../hardcore"
          (.getAbsolutePath (.getWorldFolder (Bukkit/getWorld "world")))))

(defn- players-file-path []
  (format "%s/../hardcore-players.clj"
          (.getAbsolutePath (.getWorldFolder (Bukkit/getWorld "world")))))

(defn- update-players-file [f]
  (let [path (players-file-path)]
    (try
      (let [players-set (eval (read-string (slurp path)))]
        (spit path (prn-str (f players-set))))
      (catch java.io.FileNotFoundException _
        (try
          (spit path (prn-str "#{}"))
          (update-players-file f)
          (catch Exception e (.printStackTrace e))))
      (catch Exception e (.printStackTrace e)))))

(defn- get-players-set []
  (let [path (players-file-path)]
    (try
      (eval (read-string (slurp path)))
      (catch java.io.FileNotFoundException _ #{})
      (catch Exception e (.printStackTrace e)))))

(defn PlayerDropItemEvent [event]
  (when (and
          (loc-in-hardcore? (.getLocation (.getPlayer event)))
          (= "Magic Compass"
             (some-> event
                     .getItemDrop .getItemStack l/get-display-name)))
    (l/set-cancelled event)
    (l/send-message (.getPlayer event) "[HARDCORE] You should keep it for going back home!")))

(defn ProjectileHitEvent [event]
  nil)

(defn player-in-hardcore? [player]
  (let [pname (.getName player)]
    (contains? (get-players-set) pname)))

(defn BlockPlaceEvent [event]
  (when (player-in-hardcore? (.getPlayer event))
    (let [item-stack (.getItemInHand event)]
      (when (= Material/BED (.getType item-stack))
        (l/set-cancelled event)))))


; key: ^String playername, value: ^Long timestamp msec
(def came-from (atom {}))

; key: ^String playername
(def wait-for-a-moment (atom #{}))

(defn- leave-hardcore! [player]
  (let [to (some identity [(get @came-from (.getName player))
                           (.getBedSpawnLocation player)
                           (.getSpawnLocation (Bukkit/getWorld "world"))])
        to (doto to (.add 0.0 0.1 0.0))
        pname (.getName player)]
    (swap! wait-for-a-moment conj pname)
    (l/later (l/sec 5)
      (swap! wait-for-a-moment disj pname))
    (.teleport player to)
    ; To avoid suffocation due to loading
    (l/later 0
      (.teleport player to))
    (swap! came-from dissoc pname)
    (update-players-file (fn [players-set]
                           (disj players-set pname)))))

(defn PlayerLoginEvent [event]
  (let [player (.getPlayer event)]
    (when (and (player-in-hardcore? player)
               (not (contains? @came-from (.getName player))))
      (l/later 0
        (leave-hardcore! player)))))

; key: ^String playername, value: ^Long timestamp msec
(def enter-time-all (atom {}))

(defn EntityDamageEvent [event]
  (let [entity (.getEntity event)
        cause (.getCause event)]
    (when (loc-in-hardcore? (.getLocation entity))
      (when (instance? Pig entity)
        (when (.getPassenger entity)
          (when (= EntityDamageEvent$DamageCause/FALL cause)
            (l/set-cancelled event))))
      (when (instance? Skeleton entity)
        (when (contains? #{EntityDamageEvent$DamageCause/PROJECTILE
                           EntityDamageEvent$DamageCause/ENTITY_ATTACK
                           EntityDamageEvent$DamageCause/ENTITY_EXPLOSION}
                         cause)
          (when-let [vehicle (.getVehicle entity)]
            (when (instance? Pig vehicle)
              (l/set-cancelled event)
              (when-let [damager (.getDamager event)]
                (when (instance? Arrow damager)
                  (.remove damager)))
              (let [loc (.getLocation entity)]
                (sugot.world/play-sound loc Sound/BAT_TAKEOFF 1.0 1.0)
                (.setVelocity vehicle (Vector. (rand-nth [-1.0 0.0 1.0])
                                               0.4
                                               (rand-nth [-1.0 0.0 1.0])))
                (when-let [damager (.getDamager event)]
                  (when (instance? LivingEntity damager)
                    (.setTarget entity damager)
                    (.setTarget vehicle damager))))))))

      (cond
        (instance? ArmorStand entity)
        (when (.getHelmet entity)
          (l/set-cancelled event))

        (instance? Blaze entity)
        (condp = cause
          EntityDamageEvent$DamageCause/PROJECTILE
          (let [projectile (.getDamager event)
                shooter (.getShooter projectile)]
            (condp instance? projectile
              Snowball
              (.setDamage event (max (dec (.getMaxHealth entity))
                                     1))

              Arrow
              (do
                (l/set-cancelled event)
                (.setFireTicks projectile (l/sec 1))
                (sugot.world/play-sound (.getLocation projectile)
                                        Sound/ZOMBIE_METAL 1.0 2.0)
                (l/later 0
                  (.setVelocity projectile (Vector. 0.0 1.0 0.0))
                  (when shooter
                    (.setVelocity entity (l/vector-from-to (.getLocation entity)
                                                           (.getLocation shooter))))))
              nil))
          nil)

        (instance? Monster entity)
        (condp = cause
          EntityDamageEvent$DamageCause/FIRE_TICK
          (l/set-cancelled event)

          nil)))))

(defn VehicleExitEvent [event]
  (let [exited (.getExited event)
        vehicle (.getVehicle event)]
    (when-not (.isCancelled event)
      (when (and
              (loc-in-hardcore? (.getLocation exited))
              (instance? Skeleton exited)
              (instance? Pig vehicle))
        (l/set-cancelled event)))))

(defn EntityDeathEvent [event]
  (let [entity (.getEntity event)]
    (when (loc-in-hardcore? (.getLocation entity))
      (condp instance? entity
        Blaze
        (doseq [item-stack (.getDrops event)
                :when (= Material/BLAZE_ROD (.getType item-stack))]
          (.setType item-stack Material/QUARTZ)
          (.setData item-stack nil))
        nil))))

(defn- target-nearest-hardcore-player [creature]
  (let [players-set (get-players-set)
        online-players-set (remove nil?
                                   (map #(Bukkit/getPlayer %) players-set))]
    (when-let [players (seq online-players-set)]
      (.setTarget creature (apply min-key
                                  #(.distance (.getLocation creature) (.getLocation %))
                                  players)))))

(defn- loc-average [loc1 loc2]
  (.multiply (.add (.clone loc1) (.clone loc2))
             0.5))

(defn- spawn-hardcore-blaze [loc]
  (let [blaze (sugot.world/spawn loc Blaze)]
    blaze))

(defn CreatureSpawnEvent [event]
  (let [entity (.getEntity event)
        reason (.getSpawnReason event)
        l (.getLocation event)]
    (when (and (loc-in-hardcore? l)
               (contains? #{CreatureSpawnEvent$SpawnReason/NATURAL
                            CreatureSpawnEvent$SpawnReason/CHUNK_GEN
                            CreatureSpawnEvent$SpawnReason/SPAWNER_EGG}
                          reason))
      (condp instance? entity
        Guardian nil

        Horse
        (do
          (.setDomestication entity (.getMaxDomestication entity))
          (.setArmor (.getInventory entity) (ItemStack. Material/DIRT 1)))

        Pig
        (when (= 0 (rand-int 2))
          (let [skeleton (sugot.world/spawn (.getLocation entity) Skeleton)]
            (.setPassenger entity skeleton)))

        Monster
        (if (< (.getY l) 64)
          (when-not (instance? Guardian entity)
            (l/set-cancelled event))
          (l/later 0
            (dotimes [_ 2]
              (let [loc (doto (.clone l)
                          (.add (rand-nth [-0.5 0.5]) 0.5 (rand-nth [-0.5 0.5])))
                    monster
                    (case (rand-int 10)
                      0 (doto (sugot.world/spawn loc Creeper)
                          (.setPowered true))
                      1 (sugot.world/spawn loc Silverfish)
                      2 (spawn-hardcore-blaze loc)
                      3 (spawn-hardcore-blaze loc)
                      (sugot.world/spawn loc (class entity)))]
                (target-nearest-hardcore-player monster)))))
        nil))))

(defn- launch-projectile [source projectile velocity]
  (.launchProjectile source projectile velocity))

(defn ProjectileLaunchEvent [event]
  (try
    (let [projectile (.getEntity event)
          shooter (.getShooter projectile)]
      (when (and
              (loc-in-hardcore? (.getLocation projectile))
              (instance? Blaze shooter)
              (instance? SmallFireball projectile)
              (not= 0 (rand-int 50)))
        (when-let [target (.getTarget shooter)]
          (l/set-cancelled event)
          (.setVelocity shooter (l/vector-from-to (.getLocation shooter)
                                                (.getLocation target)))
          (l/later (l/sec 1)
            (target-nearest-hardcore-player shooter)
            (launch-projectile shooter org.bukkit.entity.Arrow
                               (l/vector-from-to (.getLocation shooter)
                                                (.getLocation target))))
          #_ (.setBounce egg true))))
    (catch Exception e (.printStackTrace e))))

#_ (def interesting-seeds
  [#_7352190906321318631 ; http://epicminecraftseeds.com/stronghold-in-ravine-1-8x/
   #_ 3083175 ; http://epicminecraftseeds.com/spawn-beside-jungle-temple/
   516687594611420526 ; http://epicminecraftseeds.com/minecraft-village-seed-great-loot/
   5574457897082764526 ; http://epicminecraftseeds.com/sweet-savanna-m-above-the-clouds-minecraft-1-8-seed/
   1603402340 ; underwater
   ])

(defn random-xz [radius]
  {:pre [(< 0 radius)]}
  (let [x (- (rand-int (* 2 radius)) radius)
        z (Math/round (* (Math/sqrt (- (* radius radius) (* x x)))
                         (rand-nth [1 -1])))]
    [x z]))

(defn- rand-treasure []
  (case (rand-int 50)
    0 (ItemStack. Material/DIRT (inc (rand-int 32)))
    1 (ItemStack. Material/WATER_LILY (inc (rand-int 32)))
    2 (ItemStack. Material/SAND (inc (rand-int 64)))
    3 (ItemStack. Material/STICK (inc (rand-int 64)))
    4 (ItemStack. Material/RAW_FISH (inc (rand-int 10)) (short 0) (byte (rand-int 4)))
    5 (ItemStack. Material/BANNER (inc (rand-int 10)))
    6 (ItemStack. Material/EMERALD (inc (rand-int 4)))
    7 (ItemStack. Material/GLOWSTONE_DUST (inc (rand-int 64)))
    8 (ItemStack. Material/ARROW (inc (rand-int 64)))
    9 (ItemStack. Material/BOAT 1)
    10 (ItemStack. Material/BRICK (inc (rand-int 32)))
    11 (ItemStack. Material/COAL (inc (rand-int 32)))
    12 (ItemStack. Material/INK_SACK (inc (rand-int 8)) (short 0) (byte 4)) ; LAPIS
    13 (ItemStack. Material/DIAMOND (inc (rand-int 2)))
    14 (ItemStack. Material/ACTIVATOR_RAIL 1)
    15 (ItemStack. Material/ENDER_CHEST 1)
    16 (ItemStack. Material/ENDER_PEARL 1)
    17 (ItemStack. Material/EXP_BOTTLE (inc (rand-int 5)))
    18 (ItemStack. Material/FLINT_AND_STEEL 1)
    19 (ItemStack. Material/GOLD_INGOT (inc (rand-int 10)))
    20 (ItemStack. Material/BONE (inc (rand-int 10)))
    21 (ItemStack. Material/LOG (inc (rand-int 10)))
    22 (ItemStack. Material/SADDLE 1)
    23 (ItemStack. Material/SLIME_BALL (inc (rand-int 5)))
    24 (ItemStack. Material/STRING (inc (rand-int 5)))
    25 (ItemStack. Material/APPLE (inc (rand-int 5)))
    26 (ItemStack. Material/INK_SACK (inc (rand-int 4)) (short 0) (byte (rand-int 16)))
    27 (ItemStack. Material/INK_SACK (inc (rand-int 4)) (short 0) (byte (rand-int 16)))
    28 (ItemStack. Material/INK_SACK (inc (rand-int 4)) (short 0) (byte (rand-int 16)))
    29 (ItemStack. Material/INK_SACK (inc (rand-int 4)) (short 0) (byte (rand-int 16)))
    30 (ItemStack. Material/IRON_INGOT 1)
    31 (ItemStack. Material/IRON_BARDING 1)
    32 (ItemStack. Material/GOLD_BARDING 1)
    33 (ItemStack. Material/DIAMOND_BARDING 1)
    34 (ItemStack. Material/RABBIT_FOOT (inc (rand-int 10)))
    35 (ItemStack. Material/RABBIT_STEW 1)
    36 (ItemStack. Material/STONE_PLATE (inc (rand-int 10)))
    37 (ItemStack. Material/IRON_FENCE (inc (rand-int 10)))
    38 (ItemStack. Material/ROTTEN_FLESH 1)
    39 (ItemStack. (rand-nth [Material/RECORD_10 Material/RECORD_11 Material/RECORD_12 Material/RECORD_3
                              Material/RECORD_4 Material/RECORD_5 Material/RECORD_6 Material/RECORD_7
                              Material/RECORD_8 Material/RECORD_9 Material/GOLD_RECORD Material/GREEN_RECORD])
                   1)
    40 (ItemStack. Material/ANVIL 1)
    41 (ItemStack. Material/ARMOR_STAND 1)
    42 (ItemStack. Material/BLAZE_POWDER 1)
    43 (ItemStack. Material/BOOK (inc (rand-int 10)))
    44 (ItemStack. Material/BOOK_AND_QUILL 1)
    45 (ItemStack. Material/BREWING_STAND_ITEM 1)
    46 (ItemStack. Material/BUCKET 1)
    47 (ItemStack. Material/CAKE 1)
    48 (ItemStack. Material/CARPET (inc (rand-int 64)))
    49 (ItemStack. Material/CARROT_STICK 1)
    ; (ItemStack. Material/CAULDRON_ITEM 1)
    nil))

(defn- rand-treasures [min-n max-n]
  (for [_ (range (+ min-n (rand-int (inc (- max-n min-n)))))]
    (rand-treasure)))

(defn create-treasure-chest [block]
  (doseq [x (range -1 2)
          z (range -1 2)
          :let [material (rand-nth [Material/WOOD Material/MOSSY_COBBLESTONE])]]
    (b/set-block! (b/from-loc (.getLocation block) x -1 z) material 0))
  (b/set-block! block Material/CHEST 0)
  (let [chest (.getBlock (.getLocation block))]
    (doseq [item-stack (rand-treasures 2 8)
            :when item-stack]
      (b/add-chest-inventory chest (into-array [item-stack])))))

(defn- create-main-logic [hardcore-world]
  (.setTime hardcore-world 21000)
  (let [init-y (.getHighestBlockYAt hardcore-world 0 0)]
    (l/later 0
      (doseq [x (range -1 2)
              z (range -1 2)]
        (b/set-block! (.getBlockAt hardcore-world x (dec init-y) z)
                      Material/OBSIDIAN 0)
        (b/set-block! (.getBlockAt hardcore-world x init-y z)
                      Material/TORCH 0))))
  (let [[goal-distance chest-distance]
        (let [init-biome (.getBiome hardcore-world 0 0)]
          (get {#_Biome/OCEAN #_[100 10]
                Biome/DESERT [350 15]
                Biome/TAIGA [270 15]
                Biome/EXTREME_HILLS [90 2]}
               init-biome
               [220 6]))

        [goal-x goal-z] (random-xz (int (* goal-distance (rand-nth [0.7 0.8 0.9 1.0 1.1 1.2 1.3 2.0]))))

        goal-y (.getHighestBlockYAt hardcore-world goal-x goal-z)]
    (.setSpawnLocation hardcore-world goal-x (inc goal-y) goal-z)
    (l/later 0
      (b/set-block! (.getBlockAt hardcore-world goal-x (dec goal-y) goal-z)
                    Material/BEDROCK 0)
      (let [the-range (remove zero? (range (- chest-distance) (inc chest-distance)))
            x (+ goal-x (rand-nth the-range))
            z (+ goal-z (rand-nth the-range))
            y (+ (.getHighestBlockYAt hardcore-world x z) (rand-nth [-3 -2 -1 -1 -1 0 10]))]
        (create-treasure-chest (.getBlockAt hardcore-world x y z)))
      (-> (sugot.world/spawn (Location. hardcore-world
                                        (+ 0.5 goal-x)
                                        goal-y
                                        (+ 0.5 goal-z))
                             ArmorStand)
        (.setHelmet (ItemStack. Material/OBSIDIAN 1))))))

(defn- hardcore-world []
  (Bukkit/getWorld "hardcore"))

(declare garbage-collection)

(defn create [num-retry]
  {:pre [(not (hardcore-world-exist?))]}
  (let [world-creator (-> (WorldCreator. "hardcore")
                        (.copy (Bukkit/getWorld "world"))
                        (.seed (rand-int Integer/MAX_VALUE)
                               #_ (first interesting-seeds)))
        hardcore-world (.createWorld world-creator)]
    (if (and
          (< 0 num-retry)
          (contains? #{Biome/OCEAN Biome/DEEP_OCEAN} (.getBiome hardcore-world 0 0)))
      (do
        (l/broadcast "[HARDCORE] (It's an ocean. Retrying...)")
        (garbage-collection)
        (create (dec num-retry)))
      (do
        (update-players-file (constantly #{}))
        (create-main-logic hardcore-world)))))

(defn enter-hardcore [living-entity before-loc]
  {:pre [(hardcore-world)]}
  (let [init-loc
        (Location.
          (hardcore-world) 0.5 (inc (.getHighestBlockYAt (hardcore-world) 0 0)) 0.5)]
    (.teleport living-entity init-loc)
    (when (instance? Player living-entity)
      (l/send-message living-entity "[HARDCORE] HINT: BlazeにSnowballを投げ当てると...?")
      (l/broadcast-and-post-lingr
        (format "[HARDCORE] %s entered to hardcore world. (seed: \"%d\", biome: %s)"
                (.getName living-entity)
                (.getSeed (hardcore-world))
                (.name (.getBiome (hardcore-world) 0 0))))
      (swap! came-from assoc (.getName living-entity) before-loc)
      (swap! enter-time-all assoc (.getName living-entity) (System/currentTimeMillis))

      (update-players-file (fn [players-set]
                             (conj players-set
                                   (.getName living-entity)))))))

(defn enter-armour-stand
  "returns armour to enter, stand only if it exists. No side effects."
  [player]
  (when-let [item-stack (.getItemInHand player)]
    (when (and (= "world" (.getName (.getWorld (.getLocation player))))
               (= Material/PAPER (.getType item-stack))
               (= 1 (.getAmount item-stack))
               (not (contains? @wait-for-a-moment (.getName player))))
      (when-let [armour-stand (some #(when (instance? ArmorStand %) %)
                                    (.getNearbyEntities player 0 0 0))]
        (when (= Material/PUMPKIN (.getType (.getHelmet armour-stand)))
          armour-stand)))))

(defn leave-satisfy? [player]
  (when-let [item-stack (.getItemInHand player)]
    (when (and (player-in-hardcore? player)
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
        (empty? (get-players-set)))
    (if-let [hardcore-world (hardcore-world)]
      (let [folder (.getWorldFolder hardcore-world)]
        (if (Bukkit/unloadWorld "hardcore" false)
          (do
            (try
              (dir-delete-recursively (hardcore-world-dir))
              (catch Exception e (.printStackTrace e)))
            :unloadWorld-succeeded)
          :unloadWorld-failed))
      :precondition-failed--weird)
    :precondition-failed--normal))

(defn- format-from-msec [msec]
  (let [sec-total (/ msec 1000)
        min-total (/ sec-total 60)
        hour-total (/ min-total 60)
        seconds (int (rem sec-total 60))
        minutes (int (rem min-total 60))
        hours (int hour-total)]
    (cond
      (and (< 0 hours) (< 0 minutes))
      (format "%d hours %d minutes %d seconds" hours minutes seconds)

      (< 0 minutes)
      (format "%d minutes %d seconds" minutes seconds)

      :else
      (format "%d seconds" seconds))))

(defn leave-hardcore-with-message [player]
  {:pre [(player-in-hardcore? player)]}
  (leave-hardcore! player)
  (l/broadcast-and-post-lingr (format "[HARDCORE] %s left from the hardcore world."
                                      (.getName player)))
  (try
    (when-let [enter-time (get @enter-time-all (.getName player))]
      (l/broadcast-and-post-lingr
        (format "[HARDCORE] Record: %s"
                (format-from-msec
                  (- (System/currentTimeMillis) enter-time)))))
    (catch Exception e (.printStackTrace e))))

(defn PlayerInteractAtEntityEvent [event]
  (let [player (.getPlayer event)
        entity (.getRightClicked event)
        position (.getClickedPosition event)]
    (when (and
            (instance? ArmorStand entity)
            (player-in-hardcore? player))
      (let [helmet (.getHelmet entity)]
        (when (= Material/OBSIDIAN (.getType helmet))
          (l/set-cancelled event)))
      #_ (let [x (.getX position)
            y (.getY position)
            z (.getZ position)]
        (let [b (b/from-loc (.getLocation entity) (- x -0.5) 0 (- z -0.5))]
          (when-not (.isSolid (.getType b))
            (.teleport entity (.getLocation b))))))))

(defn PlayerInteractEntityEvent [event]
  #_ (prn :PlayerInteractEntityEvent event))

(defn PlayerInteractEvent [event]
  (when-let [player (.getPlayer event)]
    (let [action (.getAction event)
          armour-stand (enter-armour-stand player)]
      (when (contains? #{Action/RIGHT_CLICK_AIR Action/RIGHT_CLICK_BLOCK} action)
        (cond
          armour-stand
          (do
            ; effect
            (let [loc (.getLocation player)]
              (sugot.world/strike-lightning-effect loc)
              (sugot.world/play-sound loc Sound/AMBIENCE_CAVE 1.0 1.0)
              (sugot.world/play-sound loc Sound/AMBIENCE_CAVE 1.0 1.0))
            ; main
            (garbage-collection)
            (let [compass (doto (ItemStack. Material/COMPASS 1)
                            (l/add-enchantment Enchantment/DURABILITY 1)
                            (l/set-display-name "Magic Compass"))]
              (l/set-item-in-hand player compass)
              (when-not (hardcore-world-exist?)
                (l/broadcast "[HARDCORE] (Creating world...)")
                (create 3))
              ; TODO living entity
              (enter-hardcore player (doto (.clone (.getLocation armour-stand))
                                       (.add 0 1 0)))))

          (leave-satisfy? player)
          (do
            (l/set-item-in-hand player (ItemStack. Material/PAPER 1))
            (leave-hardcore-with-message player)))))))

; TODO
; This is loaded from ~/.sugot-init.clj
(defn on-load []
  (try
    (when (hardcore-world-exist?)
      (dir-delete-recursively (hardcore-world-dir)))
    (catch Exception e (.printStackTrace e))))
