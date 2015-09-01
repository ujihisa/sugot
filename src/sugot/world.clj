(ns sugot.world)

(defn strike-lightning-effect [loc]
  (.strikeLightningEffect (.getWorld loc) loc))
