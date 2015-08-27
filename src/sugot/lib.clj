(ns sugot.lib
  (:require [clj-http.client])
  (:import [org.bukkit ChatColor Bukkit Material]))

(def bot-verifier (System/getenv "BOT_VERIFIER"))

(defn post-lingr-sync [msg]
  (when bot-verifier
    (clj-http.client/post
      "http://lingr.com/api/room/say"
      {:form-params
       {:room "mcujm"
        :bot 'sugoicraft
        :text (ChatColor/stripColor (str msg))
        :bot_verifier bot-verifier}})))

(defn post-lingr [msg]
  (future
    (try
      (post-lingr-sync msg)
      (catch Exception e (-> e .printStackTrace)))))

(defn broadcast [msg]
  (Bukkit/broadcastMessage msg))

(defn broadcast-and-post-lingr [msg]
  (broadcast msg)
  (post-lingr msg))

(defn send-message [player s]
  (.sendMessage player (str s)))

(def ^:dynamic *dummy-plugin*
  (delay (-> (Bukkit/getPluginManager) (.getPlugin "dynmap"))))

(defn
  ^{:doc "Convert from seconds to ticks"
    :tag Long
    :test (fn []
            (assert (= 100 (sec 5))))}
  sec [n]
  (int (* 20 n)))

(defn later-fn [tick f]
  (let [f* (fn []
             (try
               (f)
               (catch Exception e (.printStackTrace e))))]
    (.runTaskLater
      (Bukkit/getScheduler) @*dummy-plugin* f* tick)))

(defmacro later [tick & exps]
  `(later-fn ~tick (fn [] ~@exps)))

(defn set-display-name [item-stack s]
  (let [item-meta (.getItemMeta item-stack)]
    (.setDisplayName item-meta s)
    (.setItemMeta item-stack item-meta)))
