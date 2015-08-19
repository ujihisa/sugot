(ns sugot.lib-test
  (:require [clojure.test :refer :all]
            [sugot.lib :refer :all]
            [clj-http.client]))

(deftest post-lingr-sync-test
  (testing "clj-http.client/post should be called"
    (with-redefs [sugot.lib/bot-verifier "dummy-bot-verifier"
                  clj-http.client/post (fn [& args] args)]
      (is (= ["http://lingr.com/api/room/say" {:form-params {:room "mcujm", :bot 'sugoicraft, :text "hello", :bot_verifier "dummy-bot-verifier"}}]
             (post-lingr-sync "hello"))))))

(deftest sec-test
  (testing "with int"
    (is (= 20 (sec 1)))
    (is (= 60 (sec 3))))
  (testing "with float"
    (is (= 20 (sec 1.001)))
    (is (= 22 (sec 1.1)))))

(deftest later-fn-test
  ; TODO how to test this?
  (testing "nanido"
    (is (= 1 1))))
