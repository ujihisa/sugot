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

(defn find-involved-entities [elevator]
  (let [world (.getWorld (:loc-plate elevator))
        chunks (for [[xdiff zdiff] [[-1 -1] [1 1]]]
                 (.getChunkAt world
                              (doto (.clone (:loc-plate elevator))
                                (.add xdiff 0 zdiff))))
        all-entities (mapcat #(.getEntities %) (distinct chunks))
        on-elevator? (fn [e]
                       (let [loc (.getLocation e)
                             loc-diff(l/subtract loc (:loc-plate elevator))]
                         (and
                           (<= -1 (.getX loc-diff) 1)
                           (<= 0 (.getY loc-diff) 1)
                           (<= -1 (.getZ loc-diff) 1))))]
    (filter on-elevator?
            (distinct all-entities))))

(defn move-entities [entities ydiff]
  (doseq [entity entities]
    (l/teleport entity (doto (.getLocation entity)
                         (.add 0 (+ ydiff 0.1) 0)))))

(defn move-elevator-and-entities [elevator elevator-mover-f]
  (let [entities (find-involved-entities elevator)
        ydiff (elevator-mover-f elevator)]
    (l/later 0 (move-entities entities ydiff))))

(defn up-elevator
  "Raise the given elevator as an side effect,
  and returns new location's y-diff where player should teleport."
  [elevator]
  (let [ydiff 1]
    (doseq [x (range -1 2)
            z (range -1 2)]
      (b/set-block! (b/from-loc (:loc-plate elevator) x (dec ydiff) z)
                    (:base-type elevator)
                    (:base-data elevator))
      (b/set-block! (b/from-loc (:loc-plate elevator) x (- ydiff 2) z)
                    Material/AIR
                    0))
    (b/set-block! (b/from-loc (:loc-plate elevator) 0 ydiff 0)
                  Material/STONE_PLATE
                  1)
    (b/set-block! (b/from-loc (:loc-bar elevator) 0 (- ydiff 2) 0)
                  Material/IRON_FENCE
                  0)
    ; TODO
    ydiff))

(defn PlayerMoveEvent [event]
  (let [player (.getPlayer event)
        from (.getFrom event)
        to (.getTo event)]
    (when (jumping-directly-above? player from to)
      (when-let [elevator (get-elevator-from from)]
        (l/set-cancelled event)
        (l/send-message player (format "[ELEVATOR] going up. %s"
                                       (prn-str elevator)))
        (move-elevator-and-entities elevator up-elevator)
        #_ (let [y-diff 1]
          (when (up-elevator elevator)
            (l/teleport player (doto (.getLocation player)
                                 (.add 0 (+ 0.01 y-diff) 0)))))))))

(defn PlayerToggleSneakEvent [event]
  #_ (let [player (.getPlayer event)
        loc (.getLocation player)]
    (when (.isSneaking event)
      (when-let [elevator (get-elevator-from loc)]
        (l/send-message player (format "[ELEVATOR] going down. %s"
                                       (prn-str elevator)))
        (let [y-diff -1]
          (when (move-elevator elevator y-diff)
            (l/teleport player (doto (.getLocation player)
                                 (.add 0 (+ 0.01 y-diff) 0)))))))))
