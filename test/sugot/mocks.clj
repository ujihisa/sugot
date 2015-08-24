(ns sugot.mocks
  (:import [org.bukkit Material]))

(defprotocol SugotBlock
  (getType [this])
  (getData [this]))

(defn block [^Material type ^Byte data]
  (reify SugotBlock
    (getType [this] type)
    (getData [this] data)))

(defprotocol CreatureSpawnEvent
  (getEntity [this])
  (getSpawnReason [this])
  (getLocation [this])
  (isCancelled [this])
  (setCancelled [this bool]))

