(ns sugot.app.convo-test
  (:require [midje.sweet :refer :all]
            [sugot.app.convo :refer :all]
            [sugot.lib :as l]
            [sugot.mocks :as mocks]))

(facts replacefirst-go-test
  (fact "a word"
    (@#'sugot.app.convo/replacefirst-go "n" "ん" "benri")
    => [nil "benri"]
    (@#'sugot.app.convo/replacefirst-go "be" "べ" "benri")
    => ["べ" "nri"]))

(defprotocol SugotAsyncPlayerChatEvent
  (getMessage [this])
  (getFormat [this])
  (setMessage [this msg]))

(facts AsyncPlayerChatEvent-test
  (fact "english->hiragana"
    1 => 1)

  (fact "notifies to lingr, after converting"
    (with-redefs [l/post-lingr (fn [msg] {:msg msg})]
      (AsyncPlayerChatEvent
        (reify
          mocks/Player
          (getPlayer [this] (mocks/player "dummy-player"))
          SugotAsyncPlayerChatEvent
          (getMessage [this] "a")
          (getFormat [this] "<%s> %s")
          (setMessage [this msg])))
      => {:msg "<dummy-player> あ"})))
