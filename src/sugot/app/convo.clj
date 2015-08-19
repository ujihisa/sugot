(ns sugot.app.convo
  (:import [org.bukkit Bukkit])
  (:require [clojure.string :as s]
            [sugot.lib :as l]))

(defn- replacefirst-go
  ^{:doc "remove `to` from the beginning part of `s-all`, and
         return [`to` rest] or just [nil rest]"
    :test (fn []
            (assert (= ["べ" "nri"]
                       (replacefirst-go "be" "べ" "benri"))))}
  [from to s-all]
  (if (.startsWith s-all from)
    [to (.substring s-all (count from))]
    [nil s-all]))

(def romaji-table
  [["ka" "か"] ["ki" "き"] ["ku" "く"] ["ke" "け"] ["ko" "こ"]
   ["sa" "さ"] ["shi" "し"] ["su" "す"] ["se" "せ"] ["so" "そ"]
   ;["a" "あ"] ["i" "い"] ["u" "う"] ["e" "え"] ["o" "お"]
   ["n" "ん"]])

(defn- consume1
  ^{:test (fn []
            (assert (= ["べ" "nri"]
                       (consume1 romaji-table "benri"))))}
  [table s]
  (loop [table table]
    (if (empty? table)
      nil
      (let [[from to] (first table)
            [left right] (replacefirst-go from to s)]
        (if left
          [left right]
          (recur (rest table)))))))

(defn- english->hiragana [english-str]
  (loop [memo "" s english-str]
    (if (empty? s)
      memo
      (if-let [[left right] (consume1 romaji-table s)]
        (recur (str memo left) right)
        (recur (str memo (first s)) (.substring s 1))))))

(defn AsyncPlayerChatEvent [event p]
  (let [message (-> event .getMessage english->hiragana)
        fmt (-> event .getFormat)]
    (.setMessage event message)
    (l/post-lingr (format fmt (:name p) message))))
