(ns sugot.core
  (:import [org.bukkit.craftbukkit Main]
           [org.bukkit Bukkit]
           [org.bukkit.event Listener])
  (:require [sugot.events]
            [sugot.models :as m]))

; TODO move these data out, or at least declarative
(defn register-all-recipes []
  (Bukkit/resetRecipes)
  (let [item-stack (org.bukkit.inventory.ItemStack. org.bukkit.Material/CACTUS 1)
        recipe (-> (org.bukkit.inventory.ShapedRecipe. item-stack)
                 (.shape (into-array ["aa" "aa" "aa"]))
                 (.setIngredient \a org.bukkit.Material/LEAVES))]
    (Bukkit/addRecipe recipe))

  ; TODO autoload
  (require 'sugot.app.alloy)
  (require 'sugot.app.brioche)
  (require 'sugot.app.whey)
  (require 'sugot.app.egg-block)
  (doseq [recipes [((ns-resolve 'sugot.app.alloy 'recipes))
                   ((ns-resolve 'sugot.app.brioche 'recipes))
                   ((ns-resolve 'sugot.app.whey 'recipes))
                   ((ns-resolve 'sugot.app.egg-block 'recipes))]
          recipe recipes]
    (when-not (Bukkit/addRecipe recipe)
      (binding [*out* *err*]
        (prn "Failed to Bukkit/addRecipe: " recipe)))))
#_ (register-all-recipes)

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
                     (when (instance? event-type event)
                       (try
                         (f event)
                         (catch Exception e (.printStackTrace e))))))
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
              (c sender args)))
          #_ (instanceEval []
            (set! (.description aggregated-command) "")
            (set! (.usageMessage aggregated-command) "")))]
    (.register command-map "sugot" aggregated-command)))

; TODO don't hardcode
(defn- execute-all-on-load []
  (try
    (eval (read-string "(do
                       (require 'sugot.app.hardcore)
                       (sugot.app.hardcore/on-load))"))
    (catch Exception e (.printStackTrace e))))

(defn -main [& args]
  (future
    (Main/main (make-array String 0)))

  ; call `start` once server is ready.
  (loop [server nil]
    (Thread/sleep 100)
    (if server
      (let [pm (-> server .getPluginManager)
            command-map (-> server .getCommandMap)]
        (register-all-events pm)
        (register-all-recipes)
        (execute-all-on-load)
        #_ (register-all-commands command-map))
      (recur (try (Bukkit/getServer) (catch Exception e nil))))))

; If you have ~/.sugot-init.clj, sugot.core will include it
(try
  (load-file (format "%s/.sugot-init.clj" (System/getenv "HOME")))
  (catch java.io.FileNotFoundException e nil))

; manual update
#_ (register-event (-> (Bukkit/getServer) .getPluginManager)
                org.bukkit.event.player.PlayerItemConsumeEvent
                #'sugot.app.brioche/PlayerItemConsumeEvent)
