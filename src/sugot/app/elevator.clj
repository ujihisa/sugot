(ns sugot.app.elevator
  (:require [sugot.lib :as l]
            [sugot.block :as b]
            [sugot.world])
  (:import [org.bukkit Material Sound]
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
        (let [base-block (b/from-loc loc 0 -1 0)]
          (when (and
                  (not (b/critical-block? base-block))
                  (not (contains? #{Material/SAND Material/GRAVEL}
                                  (.getType base-block))))
            (let [base-type (.getType base-block)
                  base-data (.getData base-block)
                  base-blocks (for [x (range -1 2)
                                    z (range -1 2)
                                    :when (not-both-nonzero? x z)]
                                (b/from-loc loc x -1 z))]
              (when (every? #(and (= base-type (.getType %))
                                  (= base-data (.getData %)))
                            base-blocks)
                (Elevator. (-> loc .getBlock .getLocation) (.getLocation (first bars)) base-type base-data)))))))))

(defn- jumping-directly-above? [player from to]
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
                             loc-diff (l/subtract loc (:loc-plate elevator))]
                         (and
                           (< -1.0 (.getX loc-diff) 2.0)
                           (<= 0 (.getY loc-diff) 1)
                           (< -1.0 (.getZ loc-diff) 2.0))))]
    (filter on-elevator?
            (distinct all-entities))))

(defn move-entities [entities ydiff]
  (doseq [entity entities
          :let [loc (doto (.clone (.getLocation entity))
                      (.add 0 (+ ydiff 0.4) 0))]]
    #_ (l/teleport entity loc)
    (l/later 1
      (l/teleport entity loc))))

(defn move-elevator-and-entities [elevator elevator-mover-f]
  (let [entities (find-involved-entities elevator)
        ydiff (elevator-mover-f elevator)]
    (when ydiff
      (move-entities entities ydiff))))

(defn move-elevator [elevator ydiff]
  (b/set-block! (b/from-loc (:loc-plate elevator) 0 0 0)
                Material/AIR
                1)
  (doseq [x (range -1 2)
          z (range -1 2)]
    (b/set-block! (b/from-loc (:loc-plate elevator) x (dec ydiff) z)
                  (:base-type elevator)
                  (:base-data elevator))
    (b/set-block! (b/from-loc (:loc-plate elevator) x -1 z)
                  Material/AIR
                  0))
  (b/set-block! (b/from-loc (:loc-plate elevator) 0 ydiff 0)
                Material/STONE_PLATE
                1)
  (b/set-block! (b/from-loc (:loc-bar elevator) 0 -1 0)
                Material/IRON_FENCE
                0)
  (sugot.world/play-sound (:loc-bar elevator) Sound/MINECART_BASE 0.5 2.0)
  (sugot.world/play-sound (:loc-plate elevator) Sound/MINECART_BASE 0.5 2.0))

(defn find-ydiff-up [elevator]
  (let [available? (fn [ydiff]
                     (let [bs (for [x (range -1 2)
                                    z (range -1 2)
                                    :when (not-both-nonzero? x z)]
                                (b/from-loc (:loc-plate elevator) x ydiff z))
                           airs (filter #(= Material/AIR (.getType %)) bs)
                           bars (filter #(= Material/IRON_FENCE (.getType %)) bs)]
                       (and (= Material/AIR (.getType (b/from-loc (:loc-plate elevator) 0 ydiff 0)))
                            (= Material/AIR (.getType (b/from-loc (:loc-plate elevator) 0 (inc ydiff) 0)))
                            (= 7 (count airs))
                            (= 1 (count bars)))))
        ydiffs (take-while available?
                           (range 1 51))]
    (when (seq ydiffs)
      (last ydiffs))))

(defn find-ydiff-down [elevator]
  (let [available? (fn [ydiff]
                     (let [bs (for [x (range -1 2)
                                    z (range -1 2)
                                    :when (not-both-nonzero? x z)]
                                (b/from-loc (:loc-plate elevator) x (dec ydiff) z))
                           airs (filter #(= Material/AIR (.getType %)) bs)
                           bars (filter #(= Material/IRON_FENCE (.getType %)) bs)]
                       (and (= Material/AIR (.getType (b/from-loc (:loc-plate elevator) 0 (dec ydiff) 0)))
                            (= 7 (count airs))
                            (= 1 (count bars)))))
        ydiffs (take-while available?
                           (map - (range 1 51)))]
    (when (seq ydiffs)
      (last ydiffs))))


(defn up-elevator
  "Raise the given elevator as an side effect,
  and returns new location's y-diff where player should teleport."
  [elevator]
  (let [ydiff (find-ydiff-up elevator)]
    (when ydiff
      (move-elevator elevator ydiff))
    ydiff))

(defn down-elevator
  "similar to `up-elevator`"
  [elevator]
  (let [ydiff (find-ydiff-down elevator)]
    (when ydiff
      (move-elevator elevator ydiff))
    ydiff))

(defn PlayerMoveEvent [event]
  (let [player (.getPlayer event)
        from (.getFrom event)
        to (.getTo event)]
    (when (jumping-directly-above? player from to)
      (when-let [elevator (get-elevator-from from)]
        (l/set-cancelled event)
        (move-elevator-and-entities elevator up-elevator)
        #_ (let [y-diff 1]
          (when (up-elevator elevator)
            (l/teleport player (doto (.getLocation player)
                                 (.add 0 (+ 0.01 y-diff) 0)))))))))

(defn PlayerToggleSneakEvent [event]
  (let [player (.getPlayer event)
        loc (.getLocation player)]
    (when (.isSneaking event)
      (when-let [elevator (get-elevator-from loc)]
        (move-elevator-and-entities elevator down-elevator)))))

(defn- iron-fence? [block]
  (= Material/IRON_FENCE (.getType block)))

(defn- stone-plate? [block]
  (= Material/STONE_PLATE (.getType block)))

(defn- find-elevator-from-bar
  "Lookup nearest elevator base for given iron bar block,
  and returns the elevator object. Otherwise nil.

  The directions to lookup are only streight up/down,
  and the return value will be one of them which is shorter
  (if same, it takes upper one.)"
  [iron-bar-block]
  (letfn [(find-down [loc]
            (for [ydiff (map - (range 0 10))
                  :let [b (b/from-loc loc 0 ydiff 0)]
                  :when (iron-fence? b)
                  :let [b-below (b/from-loc loc 0 (dec ydiff) 0)]
                  :when (not (iron-fence? b-below))]
              (some stone-plate?
                    (for [xdiff (range -1 2)
                          zdiff (range -1 2)]
                      (b/from-loc loc xdiff ydiff zdiff)))))]
    (letfn [(find-up [loc]
              ; TODO
              nil)]
      (let [loc (.getLocation iron-bar-block)
            stone-plate (or (first (find-down loc))
                            (first (find-up loc)))]
        (get-elevator-from (.getLocation stone-plate))
        #_ (prn :down down))))
  #_ (let [ydiffs (mapcat vector (map - (range 0 10)) (range 1 10))
  [x z] [(.getX loc) (.getZ loc)]
  result (for [ydiff ydiffs
  :let [y (+ ydiff player-y)
  b (b/from-loc loc x y z)]]
  )]
  (prn result)))

(defn PlayerInteractEvent [event]
  (let [player (.getPlayer event)
        block (.getClickedBlock event)
        action (.getAction event)]
    (when (and
            (contains? #{Action/LEFT_CLICK_BLOCK Action/RIGHT_CLICK_BLOCK} action)
            (= Material/IRON_FENCE (.getType block)))
      (when-let [elevator (find-elevator-from-bar block)]
        (l/set-cancelled event)))))
