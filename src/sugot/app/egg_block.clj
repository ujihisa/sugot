(ns sugot.app.egg-block
  (:require [sugot.lib :as l])
  (:import [org.bukkit.inventory ItemStack]
           [org.bukkit Material Sound]
           [org.bukkit.enchantments Enchantment]
           [org.bukkit.inventory FurnaceRecipe ShapelessRecipe]))

(defn recipes []
  (let [item-stack (doto (ItemStack. Material/IRON_BLOCK)
                     (.addUnsafeEnchantment Enchantment/DURABILITY 1)
                     (l/set-display-name "Egg Block"))
        egg->eggblock (doto (ShapelessRecipe. item-stack)
                     (.addIngredient 16 Material/EGG))]
    [egg->eggblock]))

(defn BlockPlaceEvent [event]
  (let [item-stack (.getItemInHand event)]
    (prn :item-stack item-stack)
    (.setCancelled event true)))
