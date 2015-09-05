(ns sugot.app.hardcore
  (:require [sugot.lib :as l])
  (:import [org.bukkit Bukkit]))

(defn TODO [player-name]
  (when-let [player (Bukkit/getPlayer player-name)]
    (let [world (Bukkit/getWorld "world")]
      (prn :player player :world world))))
