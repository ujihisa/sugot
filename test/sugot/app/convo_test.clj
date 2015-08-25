(ns sugot.app.convo-test
  (:require [clojure.test :refer :all]
            [sugot.app.convo :refer :all]
            [sugot.lib :as l]
            [sugot.mocks :as mocks]))

(deftest replacefirst-go-test
  (testing "a word"
    (is (= [nil "benri"]
           (@#'sugot.app.convo/replacefirst-go "n" "ん" "benri")))
    (is (= ["べ" "nri"]
           (@#'sugot.app.convo/replacefirst-go "be" "べ" "benri")))))

(defprotocol SugotAsyncPlayerChatEvent
  (getPlayer [this])
  (getMessage [this])
  (getFormat [this])
  (setMessage [this msg]))

(deftest AsyncPlayerChatEvent-test
  (testing "english->hiragana"
    (is (= 1 1)))

  (testing "notifies to lingr, after converting"
    (with-redefs [l/post-lingr (fn [msg] {:msg msg})]
      (is (= {:msg "<dummy-player> あ"}
             (AsyncPlayerChatEvent
               (reify SugotAsyncPlayerChatEvent
                 (getPlayer [this] (mocks/player "dummy-player"))
                 (getMessage [this] "a")
                 (getFormat [this] "<%s> %s")
                 (setMessage [this msg]))
               nil))))))
