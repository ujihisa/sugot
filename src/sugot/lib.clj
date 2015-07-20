(ns sugot.lib
  (:require [clj-http.client])
  (:import [org.bukkit ChatColor]))

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
