(ns sugot.app.brioche
  (:require [sugot.lib :as l]
            [sugot.world])
  (:import [org.bukkit.inventory ItemStack]
           [org.bukkit Material Sound]
           [org.bukkit.enchantments Enchantment]))

(defn give-exp [player value]
  (.setExp player (min 1.0
                       (+ value (.getExp player)))))

(defn PlayerItemConsumeEvent [event]
  (let [player (.getPlayer event)
        item-stack (.getItem event)]
    (when (and (= Material/BREAD (.getType item-stack))
               (seq (.getEnchantments item-stack)))
      (let [msg (format "<%s> Qu'ils mangent de la brioche." (.getName player))]
        (l/broadcast msg))
      (give-exp player 0.29)
      #_ (l/later (l/sec 0.5)
        (sugot.world/play-sound (.getLocation player) Sound/EAT 0.8 2.0)))))

(defn recipes []
  (let [item-stack (doto (ItemStack. Material/BREAD 1)
                     (.addUnsafeEnchantment Enchantment/DURABILITY 1)
                     (l/set-display-name "Brioche"))
        recipe (-> (org.bukkit.inventory.ShapedRecipe. item-stack)
                 (.shape (into-array ["a" "b"]))
                 (.setIngredient \a Material/EGG)
                 (.setIngredient \b Material/BREAD))]
    [recipe]))
