(ns sugot.app.staging-test
  (:require [clojure.test :refer :all]
            [sugot.app.staging :refer :all]
            [sugot.lib :as l])
  (:import [sugot.models P Loc]))

(deftest PlayerLoginEvent-test
  (testing "notifies to lingr"
    (with-redefs [l/post-lingr (fn [msg] {:msg msg})]
      (is (= {:msg "[LOGIN] dummy-player logged in."}
             (PlayerLoginEvent nil (P. "dummy-player" nil nil)))))))

(deftest PlayerQuitEvent-test
  (testing "notifies to lingr"
    (with-redefs [l/post-lingr (fn [msg] {:msg msg})]
      (is (= {:msg "[LOGOUT] dummy-player logged out."}
             (PlayerQuitEvent nil (P. "dummy-player" nil nil)))))))

(deftest PlayerBedEnterEvent-test
  (testing "notifies both to lingr and server"
    (with-redefs [l/broadcast-and-post-lingr (fn [msg] {:post-lingr msg})]
      ; TODO test if braodcast is also called
      (is (= {:post-lingr "[BED] dummy-player went to bed."}
             (PlayerBedEnterEvent nil (P. "dummy-player" nil nil)))))))

; (defn fixture [f]
;   ; before
;   (f)
;   ; after
;   )
; 
; (use-fixtures :each fixture)
