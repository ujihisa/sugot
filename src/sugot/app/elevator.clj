(ns sugot.app.elevator
  (:require [sugot.lib :as l])
  (:import [org.bukkit Material]
           [org.bukkit.event.block Action]))

(defprotocol SugotCancellable
  (setCancelled [this bool]))

(defrecord SugotBlockDamageEvent [getPlayer getBlock]
  SugotCancellable)

(defn elevator? [player block]
  #_ (when (= Material/GOLD_BLOCK (.getType block))
    (let [pl (.getLocation player)
          bl (.getLocation block)]
      (and (= (dec (int (.getY pl)))
              (int (.getY bl)))
           (> 5 )))))

#_ (defn BlockDamageEvent [event]
  (when-let [player (.getPlayer event)]
    (let [block (.getBlock event)]
      #_ (when (= Material/THIN_GLASS (.getType block))
        (.playSound (.getWorld (.getLocation player)) (.getLocation player)
                    org.bukkit.Sound/ZOMBIE_METAL 0.5 2))
      (when (elevator? player block)
        (.setCancelled event true)
        (l/send-message player "(WIP) elevator yay")))))

(defn PlayerInteractEvent [event]
  (when-not (.isCancelled event)
    (prn :event event)
    (when-let [player (.getPlayer event)]
      (let [action (.getAction event)
            block-face (.getBlockFace event)
            block (.getClickedBlock event)]
        (condp = action
          Action/PHYSICAL
          (l/send-message player (prn-str {:block-face block-face :block block}))
          nil)))))
