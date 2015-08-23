(ns sugot.plot
  (:import [clojure.lang PersistentArrayMap]))

(defn dot! [^PersistentArrayMap table ^Long x ^Long y ^Long z value]
  (assoc table [x y z] value))

(defn dot [^PersistentArrayMap table ^Long x ^Long y ^Long z value]
  {:pre [(not (table [x y z]))]}
  (dot! table x y z value))

(defn debug-render-2d
  "NOTE: This inserts () into the centre automatically"
  [^PersistentArrayMap table]
  (let [table (dot! table 0 0 0 "()")
        coll (keys table)
        xs (map first coll)
        zs (map last coll)]
    (doseq [x (range (apply min xs) (inc (apply max xs)))]
      (print "|")
      (doseq [z (range (apply min zs) (inc (apply max zs)))]
        (print (get table [x 0 z] "  ")))
      (println "|"))))

(defn line
  "https://en.wikipedia.org/wiki/Bresenham%27s_line_algorithm"
  [^PersistentArrayMap table ^Long x0 ^Long z0 ^Long x1 ^Long z1 value]
  (let [dx (Math/abs (- x1 x0))
        dz (Math/abs (- z1 z0))
        sx (if (< x0 x1) 1 -1)
        sz (if (< z0 z1) 1 -1)]
    (loop [table table x x0 z z0 err (- dx dz)]
      (let [table (dot table x 0 z value)]
        (if (and (= x x1) (= z z1))
          table
          (let [e2 (* 2 err)]
            (cond
              (and (> e2 (- dz)) (< e2 dx)) (recur table (+ x sx) (+ z sz) (+ (- err dz) dx))
              (> e2 (- dz)) (recur table (+ x sx) z (- err dz))
              (< e2 dx) (recur table x (+ z sz) (+ err dx))
              :else (recur table x z err))))))))

(defn line3d
  [^PersistentArrayMap table ^Long x0 ^Long y0 ^Long z0 ^Long x1 ^Long y1 ^Long z1 value]
  (let [steeps {:x (Math/abs (- x1 x0)) :y (Math/abs (- y1 y0)) :z (Math/abs (- z1 z0))}
        smooth (first (apply max-key second (seq steeps))) ; e.g. :x
        [x0 y0 z0 x1 y1 z1] (smooth {:x [x0 y0 z0 x1 y1 z1]
                                     :y [y0 z0 x0 y1 z1 x1]
                                     :z [z0 x0 y0 z1 x1 y1]})
        dx (Math/abs (- x1 x0))
        dy (Math/abs (- y1 y0))
        dz (Math/abs (- z1 z0))
        sx (if (< x0 x1) inc dec)
        sy (if (< y0 y1) inc dec)
        sz (if (< z0 z1) inc dec)]
    (loop [table table error (/ dx 2) x x0 y y0 z z0]
      (let [table (let [[x y z] (smooth {:x [x y z] :y [z x y] :z [y z x]})]
                    (dot table x y z value))]
        (if (= x x1)
          table
          (recur table error (sx x) y z))))))
#_ (prn (line3d {} 1 0 1 5 0 8 "[]"))
#_ (debug-render-2d (line3d {} 1 0 1 5 0 8 "[]"))

(defn circle
  "https://en.wikipedia.org/wiki/Midpoint_circle_algorithm"
  [^PersistentArrayMap table ^Long x0 ^Long z0 ^Long radius value]
  (loop [table table x radius z 0 decision-over-2 (- 1 x)]
    (if (< x z)
      table
      (let [table (-> table
                    (dot! (+ x x0) 0 (+ z z0) value)
                    (dot! (+ z x0) 0 (+ x z0) value)
                    (dot! (+ (- x) x0) 0 (+ z z0) value)
                    (dot! (+ (- z) x0) 0 (+ x z0) value)
                    (dot! (+ (- x) x0) 0 (+ (- z) z0) value)
                    (dot! (+ (- z) x0) 0 (+ (- x) z0) value)
                    (dot! (+ x x0) 0 (+ (- z) z0) value)
                    (dot! (+ z x0) 0 (+ (- x) z0) value))]
        (if (<= decision-over-2 0)
          (recur table x (inc z) (+ decision-over-2 (* 2 z) 1))
          (recur table (dec x) (inc z) (+ decision-over-2 (* 2 (- z (dec x))) 1)))))))
