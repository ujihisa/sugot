(ns deletelater.core
  #_ (:require [deletelater.main :as main])
  (:import [org.bukkit Bukkit]
           [org.bukkit.plugin Plugin]
           [org.bukkit.plugin.java JavaPlugin JavaPluginLoader])
  (:gen-class))

(declare start)
(declare defevent*)

(defmacro defevent [pm event-type args body]
  `(defevent* ~pm ~event-type (fn ~args ~body)))

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (future (let [x "(org.bukkit.craftbukkit.Main/main (make-array String 0))"]
            (eval (read-string x))))
  ; wait until server is ready
  (loop [server nil]
    (Thread/sleep 100)
    (if server
      (start server)
      (recur (try (Bukkit/getServer) (catch Exception e nil))))))

(defn start [server]
  (let [pm (-> server .getPluginManager)
        player (reify org.bukkit.entity.Player
                 (getName [this] "dummy-player"))]
    (defevent pm org.bukkit.event.player.AsyncPlayerChatEvent [event]
              (prn 'event event))
    #_ (try
      (-> pm (.disablePlugin (.getPlugin pm "dynmap")))
      (catch Exception e nil))
    (-> pm (.callEvent (org.bukkit.event.player.AsyncPlayerChatEvent. true player "a" (java.util.HashSet.))))
    (System/exit 0)

    ; This didn't work
    #_ (prn (Bukkit/dispatchCommand (-> server .getConsoleSender) "stop"))))

(defn defevent* [pm event-type f]
  (let [plugin (reify Plugin
                 (getConfig [this] nil)
                 (getDatabase [this] nil)
                 (getDataFolder [this] nil)
                 (getDefaultWorldGenerator [this worldName id] nil)
                 (getDescription [this] nil)
                 (getLogger [this] nil)
                 (getName [this] "ujihisa")
                 (getPluginLoader [this] nil)
                 (getResource [this filename] nil)
                 (getServer [this] (Bukkit/getServer))
                 (isEnabled [this] true)
                 (isNaggable [this] nil)
                 (onDisable [this] nil)
                 (onEnable [this] (prn 'onEnable this))
                 (onLoad [this] (prn 'onLoad this))
                 (reloadConfig [this] nil)
                 (saveConfig [this] nil)
                 (saveDefaultConfig [this] nil)
                 (saveResource [this resourcePath, replace-b] nil)
                 (setNaggable [boolean canNag] nil))
        listener (reify org.bukkit.event.Listener)
        executor (reify org.bukkit.plugin.EventExecutor
                   (execute [this listener event]
                     (f event)
                     #_ (prn (format "Welcome, %s" (-> event .getPlayer .getName)))))
        priority org.bukkit.event.EventPriority/NORMAL]
    (-> pm (.registerEvent event-type listener priority executor plugin))))
