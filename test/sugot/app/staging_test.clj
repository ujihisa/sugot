(ns sugot.app.staging-test
  (:require [clojure.test :refer :all]
            [sugot.app.staging :refer :all]))

(deftest PlayerLoginEvent-test
  (testing "PlayerLoginEvent is nice"
    (let [player (reify org.bukkit.entity.Player
                   (getName [this] "dummy-player"))
          ^java.net.InetAddress  address nil
          result #_PlayerLoginEvent.Result nil
          ^java.net.InetAddress real-address nil]
      (PlayerLoginEvent (org.bukkit.event.player.PlayerLoginEvent. player "dummy-hostname" address result "dummy-message" real-address)))
    #_ "TODO add assertions"))
