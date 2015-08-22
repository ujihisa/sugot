(ns sugot.app.limited-world)

(defn PlayerMoveEvent
  "You can't go very far.
  NOTE: This doesn't run very often just for performance"
  [event p]
  (when (and (= 0 (rand-int 10))
             (= "world" (-> :loc :world-name)))
    (let [x (-> p :loc :x)
          z (-> p :loc :z)]
      (cond
        (or (< x -300) (< 300 x))
        (.sendMessage (:orig p) (format "Your current x is %d. Go back within -300 <= x <= 300" (int x)))

        (or (< z -300) (< 300 z))
        (.sendMessage (:orig p) (format "Your current z is %d. Go back within -300 <= z <= 300" (int z)))))))
