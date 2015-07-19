(ns deletelater.main
  (:import [org.bukkit.event.player AsyncPlayerChatEvent])
  (:gen-class
    :extends org.bukkit.plugin.java.JavaPlugin))

(defn f []
  (prn 'hi AsyncPlayerChatEvent))
