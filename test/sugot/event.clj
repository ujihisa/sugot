(ns sugot.event
  (:require [sugot.lib :as l]))

(defn cancelled? [f event]
  (let [flag (ref false)]
    (with-redefs [l/set-cancelled
                  (fn [e]
                    (when (= event e)
                      (dosync
                        (ref-set flag true))))]
      (f event)
      @flag)))
