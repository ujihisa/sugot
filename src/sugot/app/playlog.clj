(ns sugot.app.playlog
  "Notifications mostly to lingr.
  This is not specific for notifications inside gameplay"
  (:require [clojure.string :as s]
            [sugot.lib :as l]))

(defn PlayerLoginEvent [event p]
  (l/post-lingr (format "[LOGIN] %s logged in." (:name p))))

(defn PlayerQuitEvent [event p]
  (l/post-lingr (format "[LOGOUT] %s logged out." (:name p))))

(defn PlayerBedEnterEvent [event p]
  (l/broadcast-and-post-lingr (format "[BED] %s went to bed." (:name p))))
