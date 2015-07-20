(ns sugot.app.staging
  (:require [clojure.string :as s]
            [sugot.lib :as l]
            [clj-http.client]
            ; just for now
            [sugot.app.convo]))

(defn PlayerLoginEvent [event]
  (let [player (-> event .getPlayer)]
    (sugot.app.convo/post-lingr (format "<%s> logged in." (.getName player)))))

(defn PlayerQuitEvent [event]
  (let [player (-> event .getPlayer)]
    (sugot.app.convo/post-lingr (format "<%s> logged out" (.getName player)))))
