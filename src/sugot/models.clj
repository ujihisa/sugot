(ns sugot.models
  (:import [org.bukkit Material Location]
           [org.bukkit.block Block]
           [org.bukkit.entity Player]))

; Represents Location
(defrecord Loc [^String world-name ^double x ^double y ^double z])

; Represents Block
(defrecord B [^Material type ^byte data ^Loc loc ^Block orig])

; Represents Player
(defrecord P [^String name ^Loc loc ^Player orig])

(defn Location->Loc [^Location location]
  (Loc. (-> location .getWorld .getName)
      (.getX location)
      (.getY location)
      (.getZ location)))

(defn Player->P [^Player player]
  (P. (.getName player)
      (Location->Loc (.getLocation player))
      player))

(defn block-set [^B b ^Material btype ^Byte data]
  (let [block (:orig b)]
    (.setType block btype)
    (.setData block data))
  nil)
