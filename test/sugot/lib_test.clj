(ns sugot.lib-test
  (:require [clojure.test :refer :all]
            [sugot.lib :refer :all]
            [clj-http.client]))

(deftest post-lingr-sync-test
  (testing "clj-http.client/post should be called"
    (with-redefs [sugot.lib/bot-verifier "dummy-bot-verifier"
                  clj-http.client/post (fn [& args] args)]
      (is (= ["http://lingr.com/api/room/say"
              {:form-params {:room "mcujm", :bot 'spifax, :text "hello", :bot_verifier "dummy-bot-verifier"}}]
             (post-lingr-sync "hello"))))))

(deftest post-lingr-test
  (with-redefs [sugot.lib/post-lingr-sync (fn [& expr] nil)]
    (is (future? (post-lingr "hello")))))

(deftest broadcast-test
  ; Nothing to do
  )

(deftest broadcast-and-post-lingr-test
  (with-redefs [broadcast (fn [msg] nil)
                post-lingr (fn [msg] [:called msg])]
    (is (= [:called "hello"]
           (broadcast-and-post-lingr "hello")))))

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

#_ (deftest consume-item-test
  ;TODO
  (is true))
