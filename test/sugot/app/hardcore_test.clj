(ns sugot.app.hardcore-test
  (:require [clojure.test :refer :all]
            [sugot.app.hardcore :refer :all]
            [sugot.lib :as l]
            [sugot.mocks :as mocks]
            [sugot.event :as event])
  (:import [org.bukkit Material]
           [org.bukkit.inventory ItemStack]
           [org.bukkit.event.block Action]))

(defn do-nothing [& _])

(deftest PlayerDropItemEvent-test
  (let [event (reify
                mocks/Player
                (getPlayer [this]
                  (let [world (mocks/world "hardcore")
                        loc (mocks/location world 0 0 0)]
                    (mocks/player "dummy-player" loc)))
                mocks/ItemDrop
                (getItemDrop [this]
                  (reify
                    mocks/ItemStack
                    (getItemStack [this] :dummy-item-stack))))]
    (is (with-redefs [l/send-message do-nothing
                      l/get-display-name (constantly "Magic Compass")]
          (event/cancelled? PlayerDropItemEvent event)))))

(deftest BlockPlaceEvent-test
  (let [event (reify
                mocks/Player
                (getPlayer [this] nil)
                mocks/ItemInHand
                (getItemInHand [this]
                  (ItemStack. Material/BED 1)))]
    (with-redefs [player-in-hardcore? (constantly true)]
      (is (event/cancelled? BlockPlaceEvent event)))))

(deftest PlayerInteractEvent-test
  (let [loc (mocks/location "hardcore" 10 20 30)
        player (mocks/player "dummy-player" loc)
        event (reify
                mocks/Player
                (getPlayer [this] player)
                mocks/Action
                (getAction [this] Action/RIGHT_CLICK_AIR)
                )]
    (with-redefs [enter-satisfy? (constantly true)
                  sugot.world/strike-lightning-effect do-nothing
                  sugot.world/play-sound do-nothing
                  l/add-enchantment do-nothing
                  l/set-display-name do-nothing
                  l/set-item-in-hand do-nothing
                  l/broadcast do-nothing
                  ;hardcore-world-exist? (constantly false)
                  create do-nothing
                  garbage-collection do-nothing
                  enter-hardcore (constantly :ok)]
      (is (= :ok (PlayerInteractEvent event))))))
