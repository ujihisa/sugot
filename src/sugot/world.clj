(ns sugot.world)

(defn strike-lightning-effect [loc]
  (.strikeLightningEffect (.getWorld loc) loc))

(defn play-sound [loc sound volume pitch]
  (.playSound (.getWorld loc) loc sound volume pitch))

(defn spawn [loc klass]
  (.spawn (.getWorld loc) loc klass))
