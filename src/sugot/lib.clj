(ns sugot.lib
  (:require [clj-http.client])
  (:import [org.bukkit ChatColor Bukkit Material]))

(def ^:private bot-verifier (System/getenv "BOT_VERIFIER"))

(defn post-lingr-sync [msg]
  (when bot-verifier
    (clj-http.client/post
      "http://lingr.com/api/room/say"
      {:form-params
       {:room "mcujm"
        :bot 'spifax
        :text (ChatColor/stripColor (str msg))
        :bot_verifier bot-verifier}})))

(defn post-lingr [msg]
  (future
    (try
      (post-lingr-sync msg)
      (catch Exception e (.printStackTrace e)))))

(defn broadcast [msg]
  (Bukkit/broadcastMessage msg))

(defn broadcast-and-post-lingr [msg]
  (broadcast msg)
  (post-lingr msg))

(defn send-message [player s]
  {:pre [player]}
  (.sendMessage player (str s)))

(def ^:dynamic *dummy-plugin*
  (delay (-> (Bukkit/getPluginManager) (.getPlugin "dynmap"))))

(defn
  #_ {:test (fn []
           (assert (= 100 (sec 5))))}
  sec
  "Convert from seconds to ticks"
  [n]
  (long (* 20 n)))

(defn later-fn [tick f]
  (let [f* (fn []
             (try
               (f)
               (catch Exception e (.printStackTrace e))))]
    (.runTaskLater
      (Bukkit/getScheduler) @*dummy-plugin* f* tick)))

(defmacro later [tick & exps]
  `(later-fn ~tick (fn [] ~@exps)))

(defn get-display-name [item-stack]
  (some-> item-stack .getItemMeta .getDisplayName))

(defn set-display-name [item-stack s]
  (let [item-meta (.getItemMeta item-stack)]
    (.setDisplayName item-meta s)
    (.setItemMeta item-stack item-meta)))

(defn consume-item [player]
  {:pre [player
         (.getItemInHand player)]}
  (let [item-stack (.getItemInHand player)
        amount (.getAmount item-stack)]
    (if (= 1 amount)
      (.setItemInHand player nil)
      (.setAmount item-stack (dec amount)))))

(defn vector-from-to [from-loc to-loc]
  (when (= (.getWorld from-loc) (.getWorld to-loc))
    (.normalize (.getDirection
                  (.subtract from-loc to-loc)))))

(defn set-cancelled [event]
  (.setCancelled event true))

(defn set-item-in-hand [player item-stack]
  (.setItemInHand player item-stack))

(defn add-enchantment [item-stack enchantment level]
  (.addUnsafeEnchantment item-stack enchantment level))

(defn teleport [entity loc]
  (.teleport entity loc))

(defn subtract [loc1 loc2]
  (.subtract (.clone loc1) (.clone loc2)))
