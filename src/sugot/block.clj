(ns sugot.block
  (:import [org.bukkit Material]))

(defn nonpolish-stone? [block]
  (and (= Material/STONE (.getType block))
       ; excluding polished ones
       (contains? #{0 1 3 5} (int (.getData block)))))

(defn polish-stone? [block]
  (and (= Material/STONE (.getType block))
       (not (contains? #{0 1 3 5} (int (.getData block))))))

(defn natural-block? [block]
  ; TODO add more
  (or (contains? #{Material/GRASS Material/DIRT Material/GRAVEL
                   Material/COAL_ORE Material/IRON_ORE
                   Material/SAND Material/LOG Material/LOG_2}
                 (.getType block))
      (nonpolish-stone? block)))
