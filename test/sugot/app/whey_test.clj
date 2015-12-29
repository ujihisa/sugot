(ns sugot.app.whey-test
  (:require [midje.sweet :refer :all]
            [sugot.app.whey :refer :all]
            [sugot.lib :as l]
            [sugot.mocks :as mocks]
            [sugot.world])
  (:import [org.bukkit Material]
           [org.bukkit.inventory ItemStack]
           [org.bukkit.enchantments Enchantment]))

(facts PlayerBucketEmptyEvent-test
  (fact "If it's normal milk, nothing will happen"
    (let [player (mocks/player "dummy-player")
          item-stack (ItemStack. Material/MILK_BUCKET 1)
          called? (ref false)
          event (reify
                  mocks/IgetPlayer
                  (getPlayer [this] player)
                  mocks/ItemStack
                  (getItemStack [this] item-stack))]
      (do
        #_ (PlayerBucketEmptyEvent event)
        @called?)
      => false))
  #_ (testing "But if it's enchanted bread, something you get exp."
    (let [player (mocks/player "dummy-player")
          item-stack (reify SugotItemStack
                       (getType [this]
                         Material/BREAD)
                       (getEnchantments [this]
                         (into-array [Enchantment/DURABILITY])))
          called? (ref false)
          event (reify
                  mocks/IgetPlayer
                  (getPlayer [this] player)
                  IgetItem
                  (getItem [this] item-stack))]
      (with-redefs [give-exp (fn [& _]
                               (dosync
                                 (ref-set called? true)))
                    l/broadcast (fn [& _] nil)
                    sugot.world/play-sound (fn [& _] nil)]
        (is (true?  (do
                      (PlayerItemConsumeEvent event)
                      @called?)))))))
