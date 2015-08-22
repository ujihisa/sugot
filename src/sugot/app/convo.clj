(ns sugot.app.convo
  (:require [clojure.string :as s]
            [sugot.lib :as l])
  (:import [org.bukkit Bukkit]))

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

; TODO use this as a word, not as a letter
(def special-word-table
  [["benri" "便利"] ["dropper" "泥(・ω・)ﾉ■ ｯﾊﾟ"] ["hopper" "穂(・ω・)ﾉ■ ｯﾊﾟ"]
   ["thx" "格別のご愛顧を賜り心よりお礼申し上げます"]
   ["yw" "いえいえ、こちらこそこれもひとえに皆々様のご支援・ご協力あってのことと心より感謝しております"]
   ["skesan" "スケさん"] ["!r" "利便性" "tkm" "匠"]])

(def english-words
  "To keep them as words untranslated"
  (for [word ["go" "raa" "zombie" "home" "page"]]
    [(re-pattern (str #"\b" word #"\b"))]))

(def romaji-table
  [["ka" "か"] ["ki" "き"] ["ku" "く"] ["ke" "け"] ["ko" "こ"]
   ["ta" "た"] ["chi" "ち"] ["tsu" "つ"] ["te" "て"] ["to" "と"]
   ["sa" "さ"] ["shi" "し"] ["su" "す"] ["se" "せ"] ["so" "そ"]
   ["na" "な"] ["ni" "に"] ["nu" "ぬ"] ["ne" "ね"] ["no" "の"]
   ["ha" "は"] ["hi" "ひ"] ["fu" "ふ"] ["he" "へ"] ["ho" "ほ"]
   ["ma" "ま"] ["mi" "み"] ["mu" "む"] ["me" "め"] ["mo" "も"]
   ["ya" "や"] ["yu" "ゆ"] ["yo" "よ"]
   ["ra" "ら"] ["ri" "り"] ["ru" "る"] ["re" "れ"] ["ro" "ろ"]
   ["wa" "わ"] ["wo" "を"] ["n" "ん"]
   ["ga" "が"] ["gi" "ぎ"] ["gu" "ぐ"] ["ge" "げ"] ["go" "ご"]
   ["za" "ざ"] ["ji" "じ"] ["zu" "ず"] ["ze" "ぜ"] ["zo" "ぞ"]
   ["da" "だ"] ["di" "ぢ"] ["du" "づ"] ["de" "で"] ["do" "ど"]
   ["ba" "ば"] ["bi" "び"] ["bu" "ぶ"] ["be" "べ"] ["bo" "ぼ"]
   ["a" "あ"] ["i" "い"] ["u" "う"] ["e" "え"] ["o" "お"]])

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
