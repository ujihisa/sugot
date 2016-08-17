(ns sugot.world
  (:import [org.bukkit Sound]))

(defn strike-lightning-effect [loc]
  (.strikeLightningEffect (.getWorld loc) loc))

(defn play-sound [loc sound volume pitch]
  ; Looks like World.playSound as of 2016-08-16 is buggy. Wrap it with try here.
  (try
    (.playSound (.getWorld loc) loc ^Sound sound (float volume) (float pitch))
    (catch Exception e e)))

(defn play-effect [loc effect data]
  (.playEffect (.getWorld loc) loc effect data))

(defn spawn [loc klass]
  (.spawn (.getWorld loc) loc klass))

(defn drop-item [loc item-stack]
  (.dropItemNaturally (.getWorld loc) loc item-stack))
