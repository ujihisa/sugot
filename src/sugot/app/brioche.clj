(ns sugot.app.brioche
  (:require [sugot.lib :as l]
            [sugot.world])
  (:import [org.bukkit.inventory ItemStack]
           [org.bukkit Material Sound]
           [org.bukkit.enchantments Enchantment]))

(defn PlayerItemConsumeEvent [event]
  (let [player (.getPlayer event)
        item-stack (.getItem event)]
    (when (and (= Material/BREAD (.getType item-stack))
               (seq (.getEnchantments item-stack)))
      (let [msg (format "<%s> Qu'ils mangent de la brioche." (.getName player))]
        (l/broadcast msg))
      (sugot.world/play-sound (.getLocation player) Sound/EAT 0.8 2.0)
      (.setExp player (+ 0.30 (.getExp player))))))

(defn recipes []
  (let [item-stack (doto (ItemStack. Material/BREAD 1)
                     (.addUnsafeEnchantment Enchantment/DURABILITY 1)
                     (l/set-display-name "Brioche"))
        recipe (-> (org.bukkit.inventory.ShapedRecipe. item-stack)
                 (.shape (into-array ["a" "b"]))
                 (.setIngredient \a Material/EGG)
                 (.setIngredient \b Material/BREAD))]
    [recipe]))
