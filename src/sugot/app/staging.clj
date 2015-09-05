(ns sugot.app.staging
  (:require [clojure.string :as s]
            [sugot.lib :as l]
            [sugot.world])
  (:import [org.bukkit.event.entity CreatureSpawnEvent$SpawnReason]))

(defn PlayerLoginEvent [event]
  (.setSleepingIgnored (.getPlayer event) true))

(defn CreatureSpawnEvent
  "bigger slimes"
  [event]
  (let [entity (.getEntity event)
        reason (.getSpawnReason event)
        l (.getLocation event)]
    (when (and
            (= "world" (-> l .getWorld .getName))
            (= CreatureSpawnEvent$SpawnReason/NATURAL reason)
            (instance? org.bukkit.entity.Slime entity))
      (.setSize entity (+ 2 (.getSize entity))))))

(def notes
  {:A0 (/ 1.0 2.0)
   :B0 (/ 1.059463 2.0)
   :H0 (/ 1.122462 2.0)
   :C0 (/ 1.189207 2.0)
   :Db0 (/ 1.259921 2.0)
   :D0 (/ 1.334840 2.0)
   :Es0 (/ 1.414214 2.0)
   :E0 (/ 1.498307 2.0)
   :F0 (/ 1.587401 2.0)
   :Gb0 (/ 1.681793 2.0)
   :G0 (/ 1.781797 2.0)
   :Ab0 (/ 1.887749 2.0)
   :A1 1.0
   :B1 1.059463
   :H1 1.122462
   :C1 1.189207
   :Db1 1.259921
   :D1 1.334840
   :Es1 1.414214
   :E1 1.498307
   :F1 1.587401
   :Gb1 1.681793
   :G1 1.781797
   :Ab1 1.887749
   :A2 2.0})

#_ (doseq [[pitchkey duration] [[:A :C :E :A2 :A :A :D :F :A2 :H :D :E :Ab :A2]]]
  (when pitchkey
    (sugot.lib/later 0
                     (play-sound (.getLocation memo/ujm) org.bukkit.Sound/NOTE_PIANO 1
                                 (pitchkey notes)))))

(defn play-score [loc score]
  (when (seq score)
    (let [[pitchkey interval] (first score)]
      (when pitchkey
        (sugot.world/play-sound loc org.bukkit.Sound/NOTE_PIANO 1 (pitchkey notes))
        #_ (play-sound loc org.bukkit.Sound/NOTE_PLING 1 (pitchkey notes)))
      (sugot.lib/later (if (zero? interval)
                         0
                         (long (/ 32 interval)))
                       (play-score loc (rest score))))))

#_ (doseq [player (org.bukkit.Bukkit/getOnlinePlayers)]
  (sugot.lib/later 0
                   (play-score (.getLocation player)
                               [[:D0 4] [:D1 2]
                                [nil 4] [:Db1 8] [:H1 8]
                                [:A1 8] [:G0 8] [:F0 8] [:E0 8]
                                [:D0 8] [:Db0 8] [:D0 8] [:E0 8]
                                [:F0 8] [:D0 8] [:E0 8] [:F0 8]
                                [:G0 8] [:Gb0 8] [:G0 8] [:A1 8]
                                [:B1 8] [:G0 8] [:A1 8] [:B1 8]
                                [:C1 16] [:B1 16] [:C1 16] [:B1 16] [:A1 4]
                                [nil 8] [:A1 8] [:G0 8] [:A1 8]
                                [:B1 8] [:D1 8] [:C1 8] [:B1 8]
                                [:C1 8] [:B1 8] [:A1 8] [:G0 8]
                                [:F0 8] [:B1 8] [:A1 8] [:G0 8]
                                [:A1 8] [:G0 8] [:F0 8] [:E0 8]
                                [:D0 8]; [:G0 8] [:F0 8] [:E0 8]
                                ;[:F0 8] [:E0 8] [:D0 8] [:C0 8]
                                ;[:H0 8]
                                ])))
