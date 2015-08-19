(ns sugot.app.staging-test
  (:require [clojure.test :refer :all]
            [sugot.app.staging :refer :all]
            [sugot.lib :as l])
  (:import [sugot.models P Loc]))

(deftest PlayerLoginEvent-test
  (testing "notifies to lingr"
    (with-redefs [l/post-lingr (fn [msg] {:msg msg})]
      (is (= {:msg "[LOGIN] dummy-player logged in."}
             (PlayerLoginEvent nil (P. "dummy-player" nil nil)))))))

(deftest PlayerQuitEvent-test
  (testing "PlayerQuitEvent is nice"
    (let [player (reify org.bukkit.entity.Player
                   (getName [this] "dummy-player"))]
      (PlayerQuitEvent (org.bukkit.event.player.PlayerQuitEvent. player "dummy-message")
                       (P. "dummy-player" nil nil)))
    #_ "TODO add assertions"))

(deftest PlayerBedEnterEvent-test
  (testing "PlayerBedEnterEvent is nice"
    (let [player (reify org.bukkit.entity.Player
                   (getName [this] "dummy-player"))
          ^org.bukkit.block.Block block nil]
      ; TODO
      ; Bukkit/broadcastMessage fails with NPE...
      #_(PlayerBedEnterEvent (org.bukkit.event.player.PlayerBedEnterEvent. player block)))
    #_ "TODO add assertions"))

(defn fixture [f]
  ; before
  (f)
  ; after
  )

(use-fixtures :each fixture)
