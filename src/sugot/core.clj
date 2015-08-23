(ns sugot.core
  (:import [org.bukkit.craftbukkit Main]
           [org.bukkit Bukkit]
           [org.bukkit.event Listener])
  (:require [sugot.events]
            [sugot.models :as m]))

(def all-apps (ref #{}))

(defn reload-all-apps []
  (letfn [(get-all-apps []
            (for [file
                  (file-seq
                    (clojure.java.io/file
                      (str (System/getProperty "user.dir")
                           "/src/sugot/app/")))
                  :when (.isFile file)
                  :let [fname (.getName file)]
                  :when (.endsWith fname ".clj")
                  :let [syn-fname
                        (format
                          "'sugot.app.%s"
                          (-> fname
                            (.replace ".clj" "")
                            (.replaceAll "_" "-")))]]
              (eval (read-string syn-fname))))]
    (let [latest-all-apps (into #{} (get-all-apps))]
      (doseq [app latest-all-apps]
        (require app :reload))
      (dosync
        (ref-set all-apps latest-all-apps)))))

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
        executor (condp #(.isAssignableFrom %1 %2) event-type
                   org.bukkit.event.player.PlayerEvent
                   (reify org.bukkit.plugin.EventExecutor
                     (execute [this listener event]
                       (when (instance? event-type event)
                         (f event (-> event .getPlayer m/Player->P)))))
                   (reify org.bukkit.plugin.EventExecutor
                     (execute [this listener event]
                       (when (instance? event-type event)
                         (f event)))))
        priority org.bukkit.event.EventPriority/NORMAL]
    (-> pm (.registerEvent event-type listener priority executor dummy-sugot-plugin))))

(defn register-all-events [pm]
  (try
    ; gather events, and register them
    (reload-all-apps)
    (doseq [app @all-apps
            [klass fs] (listeners app)
            f fs]
      (register-event pm klass f))
    (catch Exception e
      (-> e .printStackTrace)
      (System/exit 1))))

(defn register-all-commands [^org.bukkit.command.CommandMap command-map]
  (let [commands (for [app @all-apps
                       [fname-sym f] (ns-interns app)
                       :when (= "sugot-on-command" (name fname-sym))]
                   f)
        aggregated-command
        (proxy [org.bukkit.command.defaults.BukkitCommand] ["sugot"]
          (execute [sender command-label args]
            (prn :ok)
            #_ (prn :execute sender command-label args)
            #_ (doseq [c commands]
              (c sender args))))]
    (.register command-map "sugot" aggregated-command)))

(defn -main [& args]
  (future (Main/main (make-array String 0)))

  ; call `start` once server is ready.
  (loop [server nil]
    (Thread/sleep 100)
    (if server
      (let [pm (-> server .getPluginManager)
            command-map (-> server .getCommandMap)]
        (register-all-events pm)
        (register-all-commands command-map))
      (recur (try (Bukkit/getServer) (catch Exception e nil))))))

; If you have ~/.sugot-init.clj, sugot.core will include it
(try
  (load-file (format "%s/.sugot-init.clj" (System/getenv "HOME")))
  (catch java.io.FileNotFoundException e nil))
