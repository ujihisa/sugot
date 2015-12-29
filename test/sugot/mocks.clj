(ns sugot.mocks
  (:import [org.bukkit Material]))

(defprotocol IgetEntity (getEntity [this]))
(defprotocol IgetCause (getCause [this]))
(defprotocol IgetName (getName [this]))
(defprotocol IgetLocation (getLocation [this]))
(defprotocol IgetClickedBlock (getClickedBlock [this]))
(defprotocol IgetAction (getAction [this]))
(defprotocol IgetItem (getItem [this]))
(defprotocol IgetItemDrop (getItemDrop [this]))
(defprotocol IgetItemInHand (getItemInHand [this]))
(defprotocol IgetItemMeta (getItemMeta [this]))
(defprotocol IgetDisplayName (getDisplayName [this]))
(defprotocol IgetPlayer (getPlayer [this]))

(defprotocol ^:private SugotBlock
  (getType [this])
  (getData [this]))

(defn block
  ([^Material type ^Byte data]
    (block type data nil))
  ([^Material type ^Byte data loc]
    (reify
      SugotBlock
      (getType [this] type)
      (getData [this] data)
      IgetLocation
      (getLocation [this] loc))))

(defn world [name]
  (reify IgetName
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

(defn player
  ([name]
    (player name nil))
  ([name loc]
    (reify
      IgetName
      (getName [this] name)
      IgetLocation
      (getLocation [this] loc))))

(defprotocol ItemStack (getItemStack [this]))

(defprotocol SugotCreatureSpawnEvent
  ; Use with Cancel and IgetLocation
  (getSpawnReason [this]))

(defprotocol PlayerMoveEvent
  ; Use with IgetPlayer
  (getTo [this])
  (getFrom [this]))

(defprotocol PlayerToggleSneakEvent
  ; Use with IgetPlayer
  (isSneaking [this]))
