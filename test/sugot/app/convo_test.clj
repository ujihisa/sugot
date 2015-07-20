(ns sugot.app.convo-test
  (:import [org.bukkit.craftbukkit Main]
           [org.bukkit Bukkit]) 
  (:import [org.bukkit.entity Player])
  (:require [clojure.test :refer :all]
            [sugot.app.convo :refer :all]))

(deftest AsyncPlayerChatEvent-test
  (testing "AsyncPlayerChatEvent is nice"
    (let [player (reify org.bukkit.entity.Player
                   #_ (getName [this] "dummy-player")
                   #_ (getMaxHealth [this] 20)
                   #_ (getMaxHealth [this] 20.0))]
      (prn player)
      #_ (AsyncPlayerChatEvent (org.bukkit.event.player.AsyncPlayerChatEvent. true player "a" (java.util.HashSet.))))
    #_ "TODO add assertions"))

(defn fixture [f]
  (future (Main/main (make-array String 0)))

  ; call `f` once server is ready.
  (loop [server nil]
    (Thread/sleep 100)
    (if server
      (f)
      (recur (try (Bukkit/getServer) (catch Exception e nil)))))

  ; dirty hack: wait until report completes.
  (future
    (Thread/sleep 100)
    (System/exit 0)))

#_ (use-fixtures :once fixture)
