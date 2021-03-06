(ns sugot.core-test
  (:require [clojure.test :refer :all]
            [sugot.core :refer :all]
            [sugot.app.convo]))

#_ (deftest apps-test
  (testing "List up all app symbols under src/sugot/app dir"
    (is (contains? (apps) 'sugot.app.convo))))

(deftest listeners-test
  (testing "List of all event functions, and returns them as listeners for given app"
    (is (= (listeners 'sugot.app.convo)
           {org.bukkit.event.player.AsyncPlayerChatEvent
            [#'sugot.app.convo/AsyncPlayerChatEvent]}))))
