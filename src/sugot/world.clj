(ns sugot.world)

(defn strike-lightning-effect [loc]
  (.strikeLightningEffect (.getWorld loc) loc))

(defn play-sound [loc sound volume pitch]
  (.playSound (.getWorld loc) loc sound volume pitch))

(defn play-effect [loc effect data]
  (.playEffect (.getWorld loc) loc effect data))

(defn spawn [loc klass]
  (.spawn (.getWorld loc) loc klass))

(defn drop-item [loc item-stack]
  (.dropItemNaturally (.getWorld loc) loc item-stack))
