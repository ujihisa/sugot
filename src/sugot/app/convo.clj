(ns sugot.app.convo
  (:import [org.bukkit Bukkit])
  (:require [clojure.string :as s]
            [sugot.lib :as l]))

(defn- english->hiragana [english-str]
  english-str)

(defn AsyncPlayerChatEvent [event p]
  (let [message (-> event .getMessage)
        fmt (-> event .getFormat)
        msg (format fmt (:name p) message)]
    #_ (Bukkit/broadcastMessage msg)
    (l/post-lingr msg)))
