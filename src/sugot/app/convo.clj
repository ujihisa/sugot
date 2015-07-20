(ns sugot.app.convo
  (:require [clojure.string :as s]
            [sugot.lib :as l]
            [clj-http.client])
  (:import [org.bukkit #_Bukkit #_Material #_Location ChatColor]))

(def bot-verifier (System/getenv "BOT_VERIFIER"))

(defn post-lingr* [msg]
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
      (post-lingr* msg)
      (catch Exception e (-> e .printStackTrace)))))

(defn AsyncPlayerChatEvent [event]
  (let [player (-> event .getPlayer)
        message (-> event .getMessage)
        fmt (-> event .getFormat)]
    (post-lingr (format fmt (-> player .getName) message))))
