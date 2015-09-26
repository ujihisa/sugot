(ns sugot.app.staging
  (:require [clojure.string :as s]
            [sugot.lib :as l]
            [sugot.block :as b]
            [sugot.world])
  (:import [org.bukkit Bukkit Material Sound]
           [org.bukkit.entity ArmorStand Villager Painting]
           [org.bukkit.event.entity CreatureSpawnEvent$SpawnReason]
           [org.bukkit.event.block Action]
           [org.bukkit.event.entity EntityDamageEvent$DamageCause]))

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

(def notes
  {:A0 (/ 1.0 2.0)
   :B0 (/ 1.059463 2.0)
   :H0 (/ 1.122462 2.0)
   :C0 (/ 1.189207 2.0)
   :Db0 (/ 1.259921 2.0)
   :D0 (/ 1.334840 2.0)
   :Es0 (/ 1.414214 2.0)
   :E0 (/ 1.498307 2.0)
   :F0 (/ 1.587401 2.0)
   :Gb0 (/ 1.681793 2.0)
   :G0 (/ 1.781797 2.0)
   :Ab0 (/ 1.887749 2.0)
   :A1 1.0
   :B1 1.059463
   :H1 1.122462
   :C1 1.189207
   :Db1 1.259921
   :D1 1.334840
   :Es1 1.414214
   :E1 1.498307
   :F1 1.587401
   :Gb1 1.681793
   :G1 1.781797
   :Ab1 1.887749
   :A2 2.0})

#_ (doseq [[pitchkey duration] [[:A :C :E :A2 :A :A :D :F :A2 :H :D :E :Ab :A2]]]
  (when pitchkey
    (sugot.lib/later 0
                     (play-sound (.getLocation memo/ujm) org.bukkit.Sound/NOTE_PIANO 1
                                 (pitchkey notes)))))

(defn play-score [loc score]
  (when (seq score)
    (let [[pitchkey interval] (first score)]
      (when pitchkey
        #_ (sugot.world/play-sound loc org.bukkit.Sound/NOTE_PIANO 1 (pitchkey notes))
        (sugot.world/play-sound loc org.bukkit.Sound/NOTE_PLING 1 (pitchkey notes)))
      (sugot.lib/later (if (zero? interval)
                         0
                         (long (/ 32 interval)))
                       (play-score loc (rest score))))))

#_ (doseq [player (org.bukkit.Bukkit/getOnlinePlayers)]
  (sugot.lib/later 0
                   (play-score (.getLocation player)
                               [[:D0 4] [:D1 2]
                                [nil 4] [:Db1 8] [:H1 8]
                                [:A1 8] [:G0 8] [:F0 8] [:E0 8]
                                [:D0 8] [:Db0 8] [:D0 8] [:E0 8]
                                [:F0 8] [:D0 8] [:E0 8] [:F0 8]
                                [:G0 8] [:Gb0 8] [:G0 8] [:A1 8]
                                [:B1 8] [:G0 8] [:A1 8] [:B1 8]
                                [:C1 16] [:B1 16] [:C1 16] [:B1 16] [:A1 4]
                                [nil 8] [:A1 8] [:G0 8] [:A1 8]
                                [:B1 8] [:D1 8] [:C1 8] [:B1 8]
                                [:C1 8] [:B1 8] [:A1 8] [:G0 8]
                                [:F0 8] [:B1 8] [:A1 8] [:G0 8]
                                [:A1 8] [:G0 8] [:F0 8] [:E0 8]
                                [:D0 8] [:G0 8] [:F0 8] [:E0 8]
                                [:F0 8] [:E0 8] [:D0 8] [:C0 8]
                                [:H0 8]
                                ])))

(defn PlayerInteractEvent [event]
  (when-let [player (.getPlayer event)]
    (let [action (.getAction event)
          block (.getClickedBlock event) ]
      (when (and (= Action/RIGHT_CLICK_BLOCK action)
                 (= Material/SOIL (.getType block))
                 (.isSneaking player))
        (when-let [item-in-hand (.getItemInHand player)]
          (when (= Material/SEEDS (.getType item-in-hand))
            (let [targets
                  (for [x (range -2 3)
                        z (range -2 3)
                        :let [b (b/from-loc (.getLocation block) x 0 z)
                              b-above (b/from-loc (.getLocation block) x 1 z)]
                        :when (and (= Material/SOIL (.getType b))
                                   (= Material/AIR (.getType b-above)))]
                    b-above)]
              (when (seq targets)
                (sugot.world/play-sound (.getLocation player)
                                        Sound/CAT_MEOW 0.8 1.5))
              (doseq [b-above (take (.getAmount item-in-hand) targets)]
                (b/set-block b-above Material/CROPS (byte 0))
                (l/consume-item player)))))))))

(defn- player? [entity]
  (instance? org.bukkit.entity.Player entity))

(defn EntityDamageEvent [event]
  (let [entity (.getEntity event)
        player (when (player? entity)
                 entity)
        cause (.getCause event)]
    (when (= EntityDamageEvent$DamageCause/SUFFOCATION cause)
      (when player
        (when (= Material/AIR (.getType (.getBlock (.getLocation player))))
          (l/set-cancelled event))
        #_ (l/broadcast (prn-str :type (.getType (.getBlock (.getLocation player)))
                              :entity entity))))
    (when (= EntityDamageEvent$DamageCause/FALL cause)
      #_ (l/send-message player (str (format "%.2f" (-> player .getVelocity .getY))))
      (when (< 0 (-> entity .getVelocity .getY))
        (l/set-cancelled event))
      #_ (l/send-message player (prn-str (-> player .getVelocity .getY)))
      #_ (let [block (-> player .getLocation .getBlock)]
        (when (= Material/PISTON_MOVING_PIECE (.getType block))
          (l/send-message player
                          #_ (prn-str :here (-> player .getLocation (b/from-loc 0 0 0) .getType .name)
                                   (-> player .getLocation (b/from-loc 0 0 0) .getData)
                                   :shita (-> player .getLocation (b/from-loc 0 -1 0) .getType .name)
                                   (-> player .getLocation (b/from-loc 0 -1 0) .getData))
                          (prn-str (.getData block)
                                   (-> block .getState)
                                   (-> block .getState .getData)
                                   (-> block .getState .getRawData))))))))

(defn headbang [ticks pname]
  (when (< 0 ticks)
    (when-let [player (Bukkit/getPlayer pname)]
      (let [pitch (- (rand-int 180) 90)
            yaw (- (rand-int 360) 180)]
        (.teleport player (doto (.getLocation player)
                            (.setPitch pitch)
                            (.setYaw yaw))))
      (l/later 1 (headbang (dec ticks) pname)))))

(defn- villager? [entity]
  (instance? Villager entity))

(defn- count-villagers
  "Returns villagers located at the chunks around the given location.
  Search radius is 5*5 chunks around the centre."
  [loc-centre]
  (let [chunk-centre (.getChunk loc-centre)
        chunks (for [xdiff (range -2 3)
                     zdiff (range -2 3)]
                 (.getChunkAt (.getWorld loc-centre)
                              (+ xdiff (.getX chunk-centre))
                              (+ zdiff (.getZ chunk-centre))))
        villagers (for [chunk chunks
                        entity (.getEntities chunk)
                        :when (villager? entity)]
                    entity)]
    (count villagers)))

(defn PlayerInteractEntityEvent [event]
  (let [paint? (fn [entity]
                 (instance? Painting) event)]
    (let [entity (.getEntity event)]
      (when (paint? entity)
        (prn :ok)))))

#_ (sugot.lib/later 0 (prn (sugot.app.staging/count-villagers (.getLocation (Bukkit/getPlayer "ujm")))))

#_ (try
  (when-let [ujm (Bukkit/getPlayer "mozukusoba") ]
    (let [armour-stands (for [entity (.getNearbyEntities ujm 2 2 2)
                              :when (instance? ArmorStand entity)]
                          entity)
          armour-stand (first armour-stands)]
      (.setVisible armour-stand true)
      (.setPassenger armour-stand ujm)))
  (catch Exception e nil))
