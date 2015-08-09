(ns sugot.util
  (:import [org.bukkit Bukkit Material]))

(def ^:dynamic *dummy-plugin*
  (delay (-> (Bukkit/getPluginManager) (.getPlugin "dynmap"))))

(defn
  ^{:doc "Convert from seconds to ticks"
    :tag Long
    :test (fn []
            (assert (= 100 (sec 5))))}
  sec [n]
  (int (* 20 n)))

(defn later* [tick f]
  (let [f* (fn []
             (try
               (f)
               (catch Exception e (.printStackTrace e))))]
    (.scheduleSyncDelayedTask
      (Bukkit/getScheduler) *dummy-plugin* f* tick)))

(defmacro later [tick & exps]
  `(later* ~tick (fn [] ~@exps)))
