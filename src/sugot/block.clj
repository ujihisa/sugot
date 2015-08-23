(ns sugot.block
  (:import [org.bukkit Material]))

(defn natural-block? [block]
  (or (contains? #{Material/GRASS Material/DIRT Material/GRAVEL
                   Material/COAL_ORE Material/IRON_ORE
                   Material/SAND Material/LOG Material/LOG_2}
                 (.getType block))
      (and (= Material/STONE (.getType block))
           ; excluding polished ones
           (contains? #{0 1 3 5} (.getData block)))))

