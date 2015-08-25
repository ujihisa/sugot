(ns sugot.mocks
  (:import [org.bukkit Material]))

(defprotocol Name
  (getName [this]))

(defprotocol SugotBlock
  (getType [this])
  (getData [this]))

(defn block [^Material type ^Byte data]
  (reify SugotBlock
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

(defn location [world x y z]
  (reify SugotLocation
    (getWorld [this] world)
    (getX [this] x)
    (getY [this] y)
    (getZ [this] z)
    (clone [this] this)
    (add [this x0 y0 z0] (location world (+ x x0) (+ y y0) (+ z z0)))
    ; TODO getBlock
    (getBlock [this] nil)))

(defn player [name]
  (reify Name
    (getName [this] name)))

(defprotocol CreatureSpawnEvent
  (getEntity [this])
  (getSpawnReason [this])
  (getLocation [this])
  (isCancelled [this])
  (setCancelled [this bool]))

(defprotocol SugotPlayerMoveEvent
  (getTo [this]))
