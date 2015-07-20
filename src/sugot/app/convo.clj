(ns sugot.app.convo
  (:require [clojure.string :as s]
            [sugot.lib :as l]))

(defn AsyncPlayerChatEvent [event]
  (let [player (-> event .getPlayer)]
    (prn 'chat (-> player .getName))))
