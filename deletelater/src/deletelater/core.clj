(ns deletelater.core
  #_ (:require [deletelater.main :as main])
  (:gen-class))

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (println "Hello, World!")
  (let [x "(org.bukkit.craftbukkit.Main/main (make-array String 0))"]
    (eval (read-string x))))
