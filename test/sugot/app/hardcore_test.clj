(ns sugot.app.hardcore-test
  (:require [clojure.test :refer :all]
            [sugot.app.hardcore :refer :all]
            [sugot.lib :as l]
            [sugot.mocks :as mocks])
  (:import [org.bukkit Material]
           [org.bukkit.inventory ItemStack]))

(defn do-nothing [& _])

(deftest PlayerDropItemEvent-test
  (let [event (let [state (atom {:cancel false})]
                (reify
                  mocks/Cancel
                  (isCancelled [this]
                    (:cancel @state))
                  (setCancelled [this bool]
                    (swap! state update-in [:cancel] (constantly bool)))
                  mocks/Player
                  (getPlayer [this]
                    (let [world (mocks/world "hardcore")
                          loc (mocks/location world 0 0 0)]
                      (mocks/player "dummy-player" loc)))
                  mocks/ItemDrop
                  (getItemDrop [this]
                    (reify
                      mocks/ItemStack
                      (getItemStack [this] :dummy-item-stack)))))]
    (is (with-redefs [l/send-message do-nothing
                      l/get-display-name (constantly "Magic Compass")]
          (PlayerDropItemEvent event)
          (true? (.isCancelled event))))))

(defn event-cancelled? [f event]
  (let [flag (ref false)]
    (with-redefs [l/set-cancelled
                  (fn [e]
                    (when (= event e)
                      (dosync
                        (ref-set flag true))))]
      (f event)
      flag)))

(deftest BlockPlaceEvent-test
  (let [event (reify
                mocks/ItemInHand
                (getItemInHand [this]
                  (ItemStack. Material/BED 1)))]
    (is (event-cancelled? BlockPlaceEvent event))))
