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
  (fact "notifies to lingr, after converting"
    (with-redefs [l/post-lingr (fn [msg] {:msg msg})]
      (AsyncPlayerChatEvent
        (reify
          mocks/IgetPlayer
          (getPlayer [this] (mocks/player "dummy-player"))
          SugotAsyncPlayerChatEvent
          (getMessage [this] "a")
          (getFormat [this] "<%s> %s")
          (setMessage [this msg])))
      => {:msg "<dummy-player> あ"})))

(facts japanese?-test
  (fact "A word contains alphabet is not japanese"
    (@#'sugot.app.convo/japanese? "ab") => false
    (@#'sugot.app.convo/japanese? "AB") => false
    (@#'sugot.app.convo/japanese? "aB") => false
    (@#'sugot.app.convo/japanese? "Ab") => false)
  (fact "A word only contains hiragana is japanese"
    (@#'sugot.app.convo/japanese? "あa") => false
    (@#'sugot.app.convo/japanese? "aあ") => false
    (@#'sugot.app.convo/japanese? "あ") => true
    (@#'sugot.app.convo/japanese? "あいうえお") => true))

(facts raw->japanese-test
  (fact "not romaji str should not be japanese"
    (@#'sugot.app.convo/raw->japanese "emacs") => nil
    (@#'sugot.app.convo/raw->japanese "vim") => nil)
  (fact "romaji str which contains captal letter should not be japanese"
    (@#'sugot.app.convo/raw->japanese "Benri") => nil
    (@#'sugot.app.convo/raw->japanese "sugokU") => nil
    (@#'sugot.app.convo/raw->japanese "kANARi") => nil)
  (fact "romaji str should be japanese"
    (@#'sugot.app.convo/raw->japanese "benri") => "べんり"
    (@#'sugot.app.convo/raw->japanese "sugoku") => "すごく"
    (@#'sugot.app.convo/raw->japanese "kanari") => "かなり"))

(facts translate-test
  (fact "Each word should be benri"
    (@#'sugot.app.convo/translate "You should be here") => "You should べ へれ"
    (@#'sugot.app.convo/translate "sugoku kanari") => "すごく かなり"
    (@#'sugot.app.convo/translate "I am the Minecraft") => "I am the Minecraft"))
