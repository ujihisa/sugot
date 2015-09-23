(ns sugot.app.hardcore-test
  (:require [midje.sweet :refer :all]
            [sugot.app.hardcore :refer :all]
            [sugot.lib :as l]
            [sugot.mocks :as mocks]
            [sugot.event :as event])
  (:import [org.bukkit Material]
           [org.bukkit.inventory ItemStack]
           [org.bukkit.event.block Action]))

(defn do-nothing [& _])

(fact PlayerDropItemEvent-test
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
    (with-redefs [l/send-message do-nothing
                  l/get-display-name (constantly "Magic Compass")]
      (event/cancelled? PlayerDropItemEvent event)
      => true)))

(fact BlockPlaceEvent-test
  (let [event (reify
                mocks/Player
                (getPlayer [this] nil)
                mocks/ItemInHand
                (getItemInHand [this]
                  (ItemStack. Material/BED 1)))]
    (with-redefs [player-in-hardcore? (constantly true)]
      (event/cancelled? BlockPlaceEvent event)
      => true)))

(fact PlayerInteractEvent-test
  (let [loc (mocks/location "hardcore" 10 20 30)
        player (mocks/player "dummy-player" loc)
        event (reify
                mocks/Player
                (getPlayer [this] player)
                mocks/Action
                (getAction [this] Action/RIGHT_CLICK_AIR))]
    (with-redefs [enter-armour-stand
                  (constantly (reify
                                mocks/Location
                                (getLocation [this] loc)))
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
      (PlayerInteractEvent event)
      => :ok)))
