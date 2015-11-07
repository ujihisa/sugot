(ns sugot.app.egg-block
  (:require [sugot.lib :as l]
            [sugot.block :as b]
            [sugot.world])
  (:import [org.bukkit.inventory ItemStack]
           [org.bukkit Material Sound]
           [org.bukkit.enchantments Enchantment]
           [org.bukkit.inventory FurnaceRecipe ShapedRecipe]))

(defn recipes []
  (let [item-stack (doto (ItemStack. Material/DIRT)
                     (.addUnsafeEnchantment Enchantment/DURABILITY 1)
                     (l/set-display-name "Egg Block"))
        egg->eggblock (doto (ShapedRecipe. item-stack)
                        (.shape (into-array ["aaa" "aaa" "aaa"]))
                        (.setIngredient \a Material/EGG))]
    [egg->eggblock]))

(defn BlockPlaceEvent [event]
  (let [item-stack (.getItemInHand event)
        player (.getPlayer event)]
    (when (= "Egg Block" (some-> item-stack .getItemMeta .getDisplayName))
      (.setCancelled event true)
      (l/consume-item player)
      (sugot.world/drop-item (.getLocation (.getBlock event))
                             (ItemStack. Material/EGG 9)))))
