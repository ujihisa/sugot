(ns sugot.core
  (:import [org.bukkit.craftbukkit Main]))

(defn start [server]
  (prn :yay server))

(defn -main [& args]
  (future (Main/main (make-array String 0)))

  ; call `start` once server is ready.
  (loop [server nil]
    (println "trying...")
    (if server
      (start server)
      (recur (try (Bukkit/getServer) (catch Exception e nil))))) )
