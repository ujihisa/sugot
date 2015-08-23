(ns sugot.app.staging
  (:require [clojure.string :as s]
            [sugot.lib :as l]))

(defn PlayerBedEnterEvent [event p]
  #_ (let [player (:orig p)
        world (-> player .getLocation .getWorld)]
    (when (< 12541 (.getTime world) 23458)
      (l/broadcast-and-post-lingr "[BED] Good morning!")
      (.setTime world 0))))

(defn PlayerLoginEvent [event p]
  (.setSleepingIgnored (:orig p) true))
