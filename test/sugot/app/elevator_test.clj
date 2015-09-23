(ns sugot.app.elevator-test
  (:require [midje.sweet :refer :all]
            [sugot.app.elevator :refer :all]
            [sugot.lib :as l]
            [sugot.mocks :as mocks]
            [sugot.block :as b]
            [sugot.world]
            [sugot.event])
  (:import [org.bukkit Material]
           [sugot.app.elevator Elevator]
           [org.bukkit.event.block Action]))

(defprotocol SugotPlayerInteractEvent
  (isCancelled [this])
  (getPlayer [this])
  (getAction [this])
  (getBlockFace [this])
  (getClickedBlock [this]))

#_ (deftest PlayerInteractEvent-test
  (let [event (reify SugotPlayerInteractEvent
                (isCancelled [this] false)
                (getPlayer [this] 1)
                (getAction [this] 1)
                (getBlockFace [this] 1)
                (getClickedBlock [this] 1))]
    (with-redefs [l/send-message (fn [p m] :ok)]
      (is (= nil (PlayerInteractEvent event))))))

(def block-map
  (let [coll {}
        coll (reduce #(assoc %1 (first %2) (second %2))
                     coll
                     (for [x (range -1 2)
                           z (range -1 2)]
                       [[(+ 10 x) 19 (+ 30 z)]
                        (mocks/block Material/PRISMARINE 1)]))
        coll (reduce #(assoc %1 (first %2) (second %2))
                     coll
                     (for [x (range -1 2)
                           z (range -1 2)]
                       [[(+ 10 x) 20 (+ 30 z)]
                        (mocks/block Material/AIR 0)]))
        coll (assoc coll [11 20 30]
                    (mocks/block Material/IRON_FENCE 0))
        coll (assoc coll [10 20 30]
                    (mocks/block Material/STONE_PLATE 0
                                 (mocks/location "anywhere" 10 20 30)))]
    coll))

(fact "move-elevator"
  #_ (defrecord Elevator [loc-plate loc-bar base-type base-data])
  (let [elevator (Elevator. (mocks/location "anywhere" 50 60 70)
                            (mocks/location "anywhere" 50 60 71)
                            Material/DIRT
                            0)]
    (with-redefs [b/set-block! (constantly :ok)
                  sugot.world/play-sound (constantly :okk)
                  find-ydiff-up (constantly 2)]
      ; TODO real tests
      (up-elevator elevator)
      => 2)))

(fact "PlayerMoveEvent"
  (let [loc (mocks/location "anywhere" 10 20 30 block-map)
        player (mocks/player "dummy-player" loc)
        event (reify
                mocks/Player
                (getPlayer [this] player)
                mocks/PlayerMoveEvent
                (getFrom [this] loc)
                (getTo [this] nil))]
    (with-redefs [l/set-cancelled (constantly :o)
                  b/critical-block? (constantly false)
                  sugot.world/play-sound (constantly :ok)
                  move-elevator-and-entities (constantly :okkk)]
      (with-redefs-fn {#'sugot.app.elevator/jumping-directly-above? (constantly true)}
                      #(PlayerMoveEvent event))
      => :okkk)))

#_ (deftest PlayerToggleSneakEvent-test
  (let [block (mocks/block Material/STONE_PLATE 0)
        loc (mocks/location "world" 10 20 30 block-map)
        event (reify
                mocks/Player
                (getPlayer [this] (mocks/player "dummy-player" loc))
                mocks/PlayerToggleSneakEvent
                (isSneaking [this] true))]
    (with-redefs [move-elevator (constantly true)
                  l/teleport (constantly :okkk)]
      (is (= :okkk (PlayerToggleSneakEvent event))))))

(fact find-elevator-from-bar
  (let [block-map {}
        loc (mocks/location "anywhere" 10 20 30 block-map)
        block (mocks/block Material/IRON_FENCE 0 loc)]
    (find-elevator-from-bar block 20)
    => nil))

(fact PlayerInteractEvent-test
  (let [loc (mocks/location "anywhere" 10 20 30)
        player (mocks/player "dummy-player" loc)
        block (mocks/block Material/IRON_FENCE 0)
        event (reify
                mocks/Player (getPlayer [this] player)
                mocks/ClickedBlock (getClickedBlock [this] block)
                mocks/Action (getAction [this] Action/LEFT_CLICK_BLOCK))]
    (with-redefs [find-elevator-from-bar (constantly :an-elevator)]
      (sugot.event/cancelled? PlayerInteractEvent event)
      => true)))
