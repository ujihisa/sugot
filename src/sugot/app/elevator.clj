(ns sugot.app.elevator
  (:require [sugot.lib :as l])
  (:import [org.bukkit Material]))

(defprotocol SugotCancellable
  (setCancelled [this bool]))

(defrecord SugotBlockDamageEvent [getPlayer getBlock]
  SugotCancellable)

(defn elevator? [player block]
  (when (= Material/GOLD_BLOCK (.getType block))
    (let [pl (.getLocation player)
          bl (.getLocation block)]
      (and (= (dec (int (.getY pl)))
              (int (.getY bl)))
           (> 5 )))))

(defn BlockDamageEvent [event]
  (when-let [player (.getPlayer event)]
    (let [block (.getBlock event)]
      (when (elevator? player block)
        (.setCancelled event true)
        (l/send-message player "(WIP) elevator yay")))))
