(ns sugot.app.elevator
  (:require [sugot.lib :as l])
  (:import [org.bukkit Material]))

(defprotocol SugotCancellable
  (setCancelled [this bool]))

(defrecord SugotBlockDamageEvent [getPlayer getBlock]
  SugotCancellable)

(defn BlockDamageEvent [event]
  (when-let [player (.getPlayer event)]
    (let [block (.getBlock event)]
      (when (= Material/GOLD_BLOCK (.getType block))
        (l/send-message player "(WIP) elevator yay")))))
