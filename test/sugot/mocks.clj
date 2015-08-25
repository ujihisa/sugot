(ns sugot.mocks
  (:import [org.bukkit Material]))

(defprotocol SugotBlock
  (getType [this])
  (getData [this]))

(defn block [^Material type ^Byte data]
  (reify SugotBlock
    (getType [this] type)
    (getData [this] data)))

(defprotocol SugotWorld
  (getName [this]))

(defn world [name]
  (reify SugotWorld
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

(defprotocol CreatureSpawnEvent
  (getEntity [this])
  (getSpawnReason [this])
  (getLocation [this])
  (isCancelled [this])
  (setCancelled [this bool]))