(ns sugot.core
  (:import [org.bukkit.craftbukkit Main]
           [org.bukkit Bukkit]
           [org.bukkit.event Listener])
  (:require [sugot.events]
            [sugot.models :as m]))

(defn apps []
  ; So far I didn't find the way how to automatically collects all namespaces.
  #{'sugot.app.convo 'sugot.app.staging}
  #_ (into #{}
        (for [ns- (all-ns)
              :when (.startsWith (-> ns- ns-name name) "sugot.app.")]
          (ns-name ns-))))

(def bukkit-events
  (into {} (for [event  sugot.events/all]
             [(.getSimpleName event) event])))

(defn listeners [ns-symbol]
  ; the ns-symbol has to be `require`d in advance.
  (into {} (for [[fname-sym f] (ns-interns ns-symbol)
                 :let [klass (bukkit-events (name fname-sym))]
                 :when klass]
             [klass [f]])))

(def dummy-sugot-plugin
  (reify org.bukkit.plugin.Plugin
    (getConfig [this] nil)
    (getDatabase [this] nil)
    (getDataFolder [this] nil)
    (getDefaultWorldGenerator [this worldName id] nil)
    (getDescription [this] nil)
    (getLogger [this] nil)
    (getName [this] "sugot")
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
    (saveResource [this resourcePath replace-b] nil)
    (setNaggable [boolean canNag] nil)))

(defn register-event [pm event-type f]
  (let [listener (reify Listener)
        executor (reify org.bukkit.plugin.EventExecutor
                   (execute [this listener event]
                     (cond
                       (instance? org.bukkit.event.player.PlayerEvent event)
                       (f event (-> event .getPlayer m/Player->P))
                       :else
                       (f event))))
        priority org.bukkit.event.EventPriority/NORMAL]
    (-> pm (.registerEvent event-type listener priority executor dummy-sugot-plugin))))

(defn register-all [pm]
  (try
    ; gather events, and register them
    (doseq [app (apps)
            :let [_ (require app)]
            [klass fs] (listeners app)
            f fs]
      (register-event pm klass f))
    (catch Exception e
      (-> e .printStackTrace)
      (System/exit 1))))

(defn -main [& args]
  (future (Main/main (make-array String 0)))

  ; call `start` once server is ready.
  (loop [server nil]
    (Thread/sleep 100)
    (if server
      (let [pm (-> server .getPluginManager)]
        (register-all pm))
      (recur (try (Bukkit/getServer) (catch Exception e nil))))))
