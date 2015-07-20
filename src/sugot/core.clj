(ns sugot.core
  (:import [org.bukkit.craftbukkit Main]
           [org.bukkit Bukkit]))

(defn apps []
  ; So far I didn't find the way how to automatically collects all namespaces.
  #{'sugot.app.convo})

(def bukkit-events
  {"AsyncPlayerChatEvent" org.bukkit.event.player.AsyncPlayerChatEvent})

(defn listeners [ns-symbol]
  ; the ns-symbol has to be `require`d in advance.
  (into {} (for [[fname-sym f] (ns-interns ns-symbol)
                 :let [klass (bukkit-events (name fname-sym))]
                 :when klass]
             [klass [f]])))

(defn -main [& args]
  (future (Main/main (make-array String 0)))

  ; call `start` once server is ready.
  (loop [server nil]
    (Thread/sleep 100)
    (if server
      (let [pm (-> server .getPluginManager)]
        ; gather events, and register them
        )
      (recur (try (Bukkit/getServer) (catch Exception e nil))))) )
