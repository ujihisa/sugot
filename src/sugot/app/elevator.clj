(ns sugot.app.elevator
  (:require [sugot.lib :as l]
            [sugot.block :as b])
  (:import [org.bukkit Material]
           [org.bukkit.event.block Action]))

(defrecord Elevator [loc-plate loc-bar base-type base-data])

(defn- not-both-nonzero? [a b]
  (not (and (zero? a)
            (zero? b))))

(defn- get-elevator-from [loc]
  (when (= Material/STONE_PLATE (-> loc .getBlock .getType))
    (let [bar-candidates (for [x (range -1 2)
                               z (range -1 2)
                               :when (not-both-nonzero? x z)]
                           (b/from-loc loc x 0 z))
          {bars true nonbars false} (group-by
                                      #(= Material/IRON_FENCE (.getType %))
                                      bar-candidates)]
      (when (and
              (= 1 (count bars))
              (every? #(= Material/AIR (.getType %)) nonbars))
        (let [base-block (b/from-loc loc 0 -1 0)
              base-type (.getType base-block)
              base-data (.getData base-block)
              base-blocks (for [x (range -1 2)
                                z (range -1 2)
                                :when (not-both-nonzero? x z)]
                            (b/from-loc loc x -1 z))]
          (when (every? #(and (= base-type (.getType %))
                              (= base-data (.getData %)))
                        base-blocks)
            (Elevator. loc (.getLocation (first bars)) base-type base-data)))))))

; TODO make it private
(defn jumping-directly-above? [player from to]
  (and (< (.getY from) (.getY to))
       (< (- (Math/abs (.getX from)) (Math/abs (.getX to))) 0.1)
       (< (- (Math/abs (.getZ from)) (Math/abs (.getZ to))) 0.1)
       (.isOnGround player)
       (not (contains? #{Material/LADDER Material/VINE}
                       (-> from .getBlock .getType)))
       (not (-> from .getBlock .isLiquid))))

(defn raise-elevator
  "Raise the given elevator as an side effect,
  and returns new location y-diff where player should teleport."
  [elevator]
  (doseq [x (range -1 2)
          z (range -1 2)]
    (b/set-block! (b/from-loc (:loc-plate elevator) x 0 z)
                  (:base-type elevator)
                  (:base-data elevator))
    (b/set-block! (b/from-loc (:loc-plate elevator) x -1 z)
                  Material/AIR
                  0))
  (b/set-block! (b/from-loc (:loc-plate elevator) 0 1 0)
                Material/STONE_PLATE
                1)
  (b/set-block! (b/from-loc (:loc-bar elevator) 0 -1 0)
                Material/IRON_FENCE
                0)
  1)

(defn PlayerMoveEvent [event]
  (let [player (.getPlayer event)
        from (.getFrom event)
        to (.getTo event)]
    (when (jumping-directly-above? player from to)
      (when-let [elevator (get-elevator-from from)]
        (l/set-cancelled event)
        (l/send-message player (format "[ELEVATOR] going up. %s"
                                       (prn-str elevator)))
        (when-let [y-diff (raise-elevator elevator)]
          (l/teleport player (doto (.getLocation player)
                               (.add 0 y-diff 0))))))))

(defn PlayerToggleSneakEvent [event]
  (let [player (.getPlayer event)
        loc (.getLocation player)]
    (when (.isSneaking event)
      (when-let [elevator (get-elevator-from loc)]
        (l/send-message player (format "[ELEVATOR] going down. %s"
                                       (prn-str elevator)))))))
