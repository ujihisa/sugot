(ns sugot.app.brioche-test
  (:require [clojure.test :refer :all]
            [sugot.app.brioche :refer :all]
            [sugot.lib :as l]
            [sugot.mocks :as mocks]
            [sugot.world])
  (:import [org.bukkit Material]
           [org.bukkit.inventory ItemStack]
           [org.bukkit.enchantments Enchantment]))

(defprotocol Item
  (getItem [this]))

(defprotocol SugotItemStack
  (getType [this])
  (getEnchantments [this]))

(deftest PlayerItemConsumeEvent-test
  (testing "If it's normal bread, nothing will happen"
    (let [player (mocks/player "dummy-player")
          item-stack (ItemStack. Material/BREAD 1)
          called? (ref false)
          event (reify
                  mocks/Player
                  (getPlayer [this] player)
                  Item
                  (getItem [this] item-stack))]
      (with-redefs [give-exp (fn [& _]
                               (dosync
                                 (ref-set called? true)))
                    l/later-fn (fn [& _] nil)]
        (is (false? (do
                      (PlayerItemConsumeEvent event)
                      @called?))))))
  (testing "But if it's enchanted bread, something you get exp."
    (let [player (mocks/player "dummy-player")
          item-stack (reify SugotItemStack
                       (getType [this]
                         Material/BREAD)
                       (getEnchantments [this]
                         (into-array [Enchantment/DURABILITY])))
          called? (ref false)
          event (reify
                  mocks/Player
                  (getPlayer [this] player)
                  Item
                  (getItem [this] item-stack))]
      (with-redefs [give-exp (fn [& _]
                               (dosync
                                 (ref-set called? true)))
                    l/later-fn (fn [& _] nil)
                    l/broadcast (fn [& _] nil)
                    sugot.world/play-sound (fn [& _] nil)]
        (is (true?  (do
                      (PlayerItemConsumeEvent event)
                      @called?)))))))
