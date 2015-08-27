(ns sugot.app.alloy
  (:require [sugot.lib :as l])
  (:import [org.bukkit.inventory ItemStack]
           [org.bukkit Material]
           [org.bukkit.enchantments Enchantment]))

(defn recipes []
  (let [item-stack (doto (ItemStack. Material/DIAMOND 1)
                     (.addUnsafeEnchantment Enchantment/DURABILITY 1)
                     (l/set-display-name "Alloy"))
        recipe (-> (org.bukkit.inventory.ShapedRecipe. item-stack)
                 (.shape (into-array ["a" "b"]))
                 (.setIngredient \a Material/GOLD_NUGGET)
                 (.setIngredient \b Material/IRON_INGOT))]
    [recipe]))
