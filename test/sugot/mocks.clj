(ns sugot.mocks
  (:import [org.bukkit Material]))

(defprotocol Name
  (getName [this]))

(defprotocol ^:private SugotBlock
  (getType [this])
  (getData [this]))

(defn block [^Material type ^Byte data]
  (reify
    SugotBlock
    (getType [this] type)
    (getData [this] data)))

(defn world [name]
  (reify Name
    (getName [this] name)))

(defprotocol SugotLocation
  (getWorld [this])
  (getX [this])
  (getY [this])
  (getZ [this])
  (clone [this])
  (add [this x y z])
  (getBlock [this]))

(defn location
  ([world x y z]
    (location world x y z {}))
  ([world x y z block-map]
    (let [state (atom {:x x :y y :z z})]
      (reify SugotLocation
        (getWorld [this] world)
        (getX [this] (:x @state))
        (getY [this] (:y @state))
        (getZ [this] (:z @state))
        (clone [this]
          (location world
                    (.getX this)
                    (.getY this)
                    (.getZ this)
                    block-map))
        (add [this x0 y0 z0]
          (swap! state (fn [coll]
                         (conj coll {:x (+ (:x coll) x0)
                                     :y (+ (:y coll) y0)
                                     :z (+ (:z coll) z0)})))
          nil)
        (getBlock [this]
          (get block-map [(.getX this)
                          (.getY this)
                          (.getZ this)]))))))

(defn player [name]
  (reify Name
    (getName [this] name)))

(defprotocol SugotCreatureSpawnEvent
  (getEntity [this])
  (getSpawnReason [this])
  (getLocation [this])
  (isCancelled [this])
  (setCancelled [this bool]))

(defprotocol SugotPlayerMoveEvent
  (getPlayer [this])
  (getTo [this])
  (getFrom [this]))

(defprotocol SugotPlayerToggleSneakEvent
  (getPlayer [this])
  (isSneaking [this]))
