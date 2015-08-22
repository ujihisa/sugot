(ns sugot.app.highrise-test
  (:require [clojure.test :refer :all]
            [sugot.app.highrise :refer :all]
            [sugot.lib :as l])
  (:import [[org.bukkit.event.entity CreatureSpawnEvent$SpawnReason]]))

(deftest a-test
  (testing "prevent spawning monster at high space"
    (let [event (org.bukkit.event.entity.CreatureSpawnEvent. nil nil)]
      (is (= 0
             (CreatureSpawnEvent event))))))
