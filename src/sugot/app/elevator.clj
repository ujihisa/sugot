(ns sugot.app.elevator
  (:require [sugot.lib :as l]
            [sugot.block :as b])
  (:import [org.bukkit Material]
           [org.bukkit.event.block Action]))

(defrecord Elevator [loc-plate base-type base-data])

(defn- get-elevator-from [loc]
  (when (= Material/STONE_PLATE (-> loc .getBlock .getType))
    (let [base-block (b/from-loc loc 0 -1 0)
          base-type (.getType base-block)
          base-data (.getData base-block)
          base-blocks (for [x (range -1 2)
                            z (range -1 2)
                            :when (and (not (zero? x)) (not (zero? z)))]
                        (b/from-loc loc x -1 z))]
      (when (every? #(and (= base-type (.getType %))
                          (= base-data (.getData %)))
                    base-blocks)
        (Elevator. loc base-type base-data)))))

(defn elevator? [player block]
  #_ (when (= Material/GOLD_BLOCK (.getType block))
    (let [pl (.getLocation player)
          bl (.getLocation block)]
      (and (= (dec (int (.getY pl)))
              (int (.getY bl)))
           (> 5 )))))

#_ (defn BlockDamageEvent [event]
  (when-let [player (.getPlayer event)]
    (let [block (.getBlock event)]
      #_ (when (= Material/THIN_GLASS (.getType block))
        (.playSound (.getWorld (.getLocation player)) (.getLocation player)
                    org.bukkit.Sound/ZOMBIE_METAL 0.5 2))
      (when (elevator? player block)
        (.setCancelled event true)
        (l/send-message player "(WIP) elevator yay")))))

#_ (defn PlayerInteractEvent [event]
  #_ (when-not (.isCancelled event)
    (prn :event event)
    (when-let [player (.getPlayer event)]
      (let [action (.getAction event)
            block-face (.getBlockFace event)
            block (.getClickedBlock event)]
        (condp = action
          Action/PHYSICAL
          (l/send-message player (prn-str {:block-face block-face :block block}))
          nil)))))

; TODO make it private
(defn jumping-directly-above? [player from to]
  (and (< (.getY from) (.getY to))
       (< (- (Math/abs (.getX from)) (Math/abs (.getX to))) 0.1)
       (< (- (Math/abs (.getZ from)) (Math/abs (.getZ to))) 0.1)
       (.isOnGround player)
       (not (contains? #{Material/LADDER Material/VINE}
                       (-> from .getBlock .getType)))
       (not (-> from .getBlock .isLiquid))))

(defn PlayerMoveEvent [event]
  (let [player (.getPlayer event)
        from (.getFrom event)
        to (.getTo event)]
    (when (jumping-directly-above? player from to)
      (when-let [elevator (get-elevator-from from)]
        #_ (.setCancelled event true)
        (l/send-message player (format "[ELEVATOR] going up. %s"
                                       (prn-str elevator)))))))

(defn PlayerToggleSneakEvent [event]
  (let [player (.getPlayer event)
        loc (.getLocation player)]
    (when (.isSneaking event)
      (when-let [elevator (get-elevator-from loc)]
        (l/send-message player (format "[ELEVATOR] going down. %s"
                                       (prn-str elevator)))))))
