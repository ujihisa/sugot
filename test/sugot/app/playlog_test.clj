(ns sugot.app.playlog-test
  (:require [midje.sweet :refer :all]
            [sugot.app.playlog :refer :all]
            [sugot.lib :as l]
            [sugot.mocks :as mocks]
            [sugot.world]))

(facts PlayerLoginEvent-test
  (fact "notifies to lingr"
    (let [event (reify mocks/Player
             (getPlayer [this]
               (mocks/player "dummy-player" nil)))]
      (with-redefs [l/later-fn (fn [sec & expr] expr)
                    l/post-lingr (fn [msg] {:msg msg})
                    sugot.world/strike-lightning-effect (fn [loc] nil)]
        (= {:msg "[LOGIN] dummy-player logged in."}
           (PlayerLoginEvent event))
        => true))))

(facts PlayerQuitEvent-test
  (fact "notifies to lingr"
    (with-redefs [l/post-lingr (fn [msg] {:msg msg})]
      (= {:msg "[LOGOUT] dummy-player logged out."}
         (PlayerQuitEvent (reify mocks/Player
                            (getPlayer [this] (mocks/player "dummy-player")))))
      => true)))

(facts PlayerBedEnterEvent-test
  (fact "notifies both to lingr and server"
    (with-redefs [l/broadcast-and-post-lingr (fn [msg] {:post-lingr msg})]
      ; TODO test if braodcast is also called
      (= {:post-lingr "[BED] dummy-player went to bed."}
         (PlayerBedEnterEvent (reify mocks/Player
                                (getPlayer [this] (mocks/player "dummy-player")))))
      => true)))

; TODO TODO TODO
#_ (deftest PlayerDeathEvent-test
  (testing "notifies both to lingr and server"
    (let [player nil
          event
          (let [drops nil
                dropped-exp nil
                new-exp nil
                new-total-exp nil
                new-level nil
                death-message "dummy-death-message"]
            ; PlayerDeathEvent(
            ;   Player player, List<ItemStack> drops, int droppedExp, int newExp, int newTotalExp, int newLevel, String deathMessage)
            (proxy [] []
              (getDeathMessage [this] "dummy-death-message")))]
          (with-redefs [l/broadcast-and-post-lingr (fn [msg] {:post-lingr msg})]
            (is (= nil
                   (PlayerDeathEvent event (P. "dummy-player" nil player))))))))
