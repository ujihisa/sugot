(ns sugot.app.playlog
  "Notifications mostly to lingr.
  This is not specific for notifications inside gameplay"
  (:require [clojure.string :as s]
            [sugot.lib :as l]
            [sugot.world]))

(defn PlayerLoginEvent [event]
  (l/later 0
    (sugot.world/strike-lightning-effect (.getLocation (.getPlayer event))))
  (l/post-lingr (format "[LOGIN] %s logged in." (.getName (.getPlayer event)))))

(defn PlayerQuitEvent [event]
  (l/post-lingr (format "[LOGOUT] %s logged out." (.getName (.getPlayer event)))))

(defn PlayerBedEnterEvent [event]
  (l/broadcast-and-post-lingr (format "[BED] %s went to bed." (.getName (.getPlayer event)))))

(defn PlayerDeathEvent [event]
  (let [player (.getEntity event)
        x (int (.getX (.getLocation player)))
        y (int (.getY (.getLocation player)))
        z (int (.getZ (.getLocation player)))
        cause (.getLastDamageCause player)]
    (l/broadcast-and-post-lingr
      (format "[DEATH] %s (at [%d, %d, %d]) %s"
              (.getDeathMessage event) x y z cause))))
