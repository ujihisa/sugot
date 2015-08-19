(ns sugot.app.staging
  (:require [clojure.string :as s]
            [sugot.lib :as l]
            [clj-http.client]))

(defn PlayerLoginEvent [event p]
  (l/post-lingr (format "[LOGIN] %s logged in." (:name p))))

(defn PlayerQuitEvent [event p]
  (l/post-lingr (format "[LOGOUT] %s logged out." (:name p))))

(defn PlayerBedEnterEvent [event p]
  (let [msg (format "[BED] %s went to bed." (:name p))]
    (l/broadcast msg)
    (l/post-lingr msg)))
