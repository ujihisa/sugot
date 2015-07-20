(ns sugot.app.staging
  (:require [clojure.string :as s]
            [sugot.lib :as l]
            [clj-http.client]))

(defn PlayerLoginEvent [event]
  (let [player (-> event .getPlayer)]
    (l/post-lingr (format "[LOGIN] %s logged in." (.getName player)))))

(defn PlayerQuitEvent [event]
  (let [player (-> event .getPlayer)]
    (l/post-lingr (format "[LOGOUT] %s logged out" (.getName player)))))
