(ns sugot.app.limited-world
  (:require [sugot.lib :as l]))

(defn PlayerMoveEvent
  "You can't go very far.
  NOTE: This doesn't run very often just for performance"
  [event]
  (when (and (= 0 (rand-int 10))
             (= "world" (-> event .getTo .getWorld .getName)))
    (let [x (-> event .getTo .getX)
          z (-> event .getTo .getZ)]
      (cond
        (or (< x -300) (< 300 x))
        (l/send-message (:orig p) (format "Your current x is %d. Go back within -300 <= x <= 300" (int x)))

        (or (< z -300) (< 300 z))
        (l/send-message (:orig p) (format "Your current z is %d. Go back within -300 <= z <= 300" (int z)))))))
