(ns sugot.app.egg-block
  (:require [sugot.lib :as l])
  (:import [org.bukkit.inventory ItemStack]
           [org.bukkit Material Sound]
           [org.bukkit.enchantments Enchantment]
           [org.bukkit.inventory FurnaceRecipe ShapedRecipe]))

(defn recipes []
  (let [item-stack (doto (ItemStack. Material/IRON_BLOCK)
                     (.addUnsafeEnchantment Enchantment/DURABILITY 1)
                     (l/set-display-name "Egg Block"))
        egg->eggblock (doto (ShapedRecipe. item-stack)
                        (.shape (into-array ["aaa" "aaa" "aaa"]))
                        (.addIngredient \a Material/EGG))]
    [egg->eggblock]))

(defn BlockPlaceEvent [event]
  (let [item-stack (.getItemInHand event)]
    (when (= "Egg Block" (some-> item-stack .getItemMeta .getDisplayName))
      (.setCancelled event true))))
