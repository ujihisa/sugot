(ns sugot.app.staging-test
  (:require [clojure.test :refer :all]
            [sugot.app.staging :refer :all]))

(deftest PlayerLoginEvent-test
  (testing "PlayerLoginEvent is nice"
    (let [player (reify org.bukkit.entity.Player
                   (getName [this] "dummy-player"))
          address nil
          result #_PlayerLoginEvent.Result nil
          ^java.net.InetAddress real-address nil]
      (PlayerLoginEvent (org.bukkit.event.player.PlayerLoginEvent. player "dummy-hostname" ^java.net.InetAddress address result "dummy-message" real-address)))
    #_ "TODO add assertions"))

(deftest PlayerQuitEvent-test
  (testing "PlayerQuitEvent is nice"
    (let [player (reify org.bukkit.entity.Player
                   (getName [this] "dummy-player"))]
      (PlayerQuitEvent (org.bukkit.event.player.PlayerQuitEvent. player "dummy-message")))
    #_ "TODO add assertions"))

(deftest PlayerBedEnterEvent-test
  (testing "PlayerBedEnterEvent is nice"
    (let [player (reify org.bukkit.entity.Player
                   (getName [this] "dummy-player"))
          ^org.bukkit.block.Block block nil]
      (PlayerBedEnterEvent (org.bukkit.event.player.PlayerBedEnterEvent. player block)))
    #_ "TODO add assertions"))

(defn fixture [f]
  ; before
  (f)
  ; after
  )

(use-fixtures :each fixture)
