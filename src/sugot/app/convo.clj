(ns sugot.app.convo
  (:require [clojure.string :as s]
            [sugot.lib :as l]))

(defn AsyncPlayerChatEvent [event]
  (let [player (-> event .getPlayer)
        message (-> event .getMessage)
        fmt (-> event .getFormat)]
    (l/post-lingr (format fmt (-> player .getName) message))))
