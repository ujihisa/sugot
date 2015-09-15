(ns sugot.app.elevator
  (:require [sugot.lib :as l]
            [sugot.block :as b])
  (:import [org.bukkit Material]
           [org.bukkit.event.block Action]))

(defrecord Elevator [loc-plate])

(defn- get-elevator-from [loc]
  (when (= Material/STONE_PLATE (-> loc .getBlock .getType))
    (Elevator. loc)))

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

(defn- jumping-directly-above? [player from to]
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
                                       elevator))))))

(defn PlayerToggleSneakEvent [event]
  (let [player (.getPlayer event)]
    (when (and
            (.isSneaking event)
            (= Material/STONE_PLATE (-> player .getLocation .getBlock .getType)))
      (l/send-message player "[ELEVATOR] going down"))))
