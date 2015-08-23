(ns sugot.app.staging
  (:require [clojure.string :as s]
            [sugot.lib :as l]))

(defn PlayerBedEnterEvent [event p]
  (l/later (l/sec 2)
           (let [world (-> p :orig .getLocation .getWorld )]
             (when (< 12541 (.getTime world) 23458)
               (l/broadcast "[BED] Good morning!")
               (.setTime world 0)))))
