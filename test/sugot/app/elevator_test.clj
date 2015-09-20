(ns sugot.app.elevator-test
  (:require [clojure.test :refer :all]
            [sugot.app.elevator :refer :all]
            [sugot.lib :as l]
            [sugot.mocks :as mocks]
            [sugot.block :as b])
  (:import [org.bukkit Material]
           [sugot.app.elevator Elevator]))

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
        coll (assoc coll [11 20 30] (mocks/block Material/IRON_FENCE 0))
        coll (assoc coll [10 20 30] (mocks/block Material/STONE_PLATE 0))]
    coll))

(deftest raise-elevator-test
  #_ (defrecord Elevator [loc-plate loc-bar base-type base-data])
  (let [elevator (Elevator. (mocks/location "anywhere" 50 60 70)
                            nil
                            Material/DIRT
                            0)]
    (with-redefs [b/set-block! (constantly :ok)]
      ; TODO real tests
      (is (= 1 (raise-elevator elevator))))))

(deftest PlayerMoveEvent-test
  (let [loc (mocks/location "anywhere" 10 20 30 block-map)
        player (mocks/player "dummy-player" loc)
        event (reify
                mocks/Player
                (getPlayer [this] player)
                mocks/PlayerMoveEvent
                (getFrom [this] loc)
                (getTo [this] :to))]
    (with-redefs [l/set-cancelled (constantly :o)
                  l/send-message (constantly :ok)
                  jumping-directly-above? (constantly true)
                  raise-elevator (constantly 5)
                  l/teleport (constantly :okkk)]
      (is (= :okkk (PlayerMoveEvent event))))))

(deftest PlayerToggleSneakEvent-test
  (let [block (mocks/block Material/STONE_PLATE 0)
        loc (mocks/location "world" 10 20 30 block-map)
        event (reify
                mocks/Player
                (getPlayer [this] (mocks/player "dummy-player" loc))
                mocks/PlayerToggleSneakEvent
                (isSneaking [this] true))]
    (with-redefs [l/send-message (fn [& _] :ok)]
      (is (= :ok (PlayerToggleSneakEvent event))))))
