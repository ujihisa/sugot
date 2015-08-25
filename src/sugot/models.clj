(ns sugot.models
  (:import [org.bukkit Bukkit Material Location]
           [org.bukkit.block Block]
           [org.bukkit.entity Player]
           [org.bukkit.inventory ItemStack]))

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

(defn Block->B [^Block block]
  (B. (.getType block)
      (.getData block)
      (Location->Loc (.getLocation block))
      block))

#_ (defn block-at [^Loc loc]
  (-> loc :orig .getBlock Block->B))

(defn block-set [^B b ^Material btype ^Byte data]
  (let [block (:orig b)]
    (.setType block btype)
    (.setData block data))
  nil)

(defn player-all []
  (seq (map Player->P (Bukkit/getOnlinePlayers))))

(defn block-set-virtual
  [^B b ^Material btype data]
  {:pre [(instance? Byte data)]}
  (doseq [p (player-all)]
    (.sendBlockChange (:orig p) ^Location (-> b :orig .getLocation) ^Material btype data)))

(defn block-break [^B b]
  (.breakNaturally b (ItemStack. Material/DIAMOND_PICKAXE 1)))

(def operations
  {:block-break (fn [^B b]
                  (.breakNaturally (:orig b)
                                   (ItemStack. Material/DIAMOND_PICKAXE 1)))
   :block-set! (fn [^B b ^Material btype ^Byte data]
                 (let [block (:orig b)]
                   (.setType block btype)
                   (.setData block data)))})
