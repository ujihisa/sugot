(ns sugot.app.hardcore-test
  (:require [clojure.test :refer :all]
            [sugot.app.hardcore :refer :all]
            [sugot.lib :as l]
            [sugot.mocks :as mocks])
  (:import [org.bukkit Material]
           [org.bukkit.inventory ItemStack]))

(defn do-nothing [& _])

(deftest PlayerDropItemEvent-test
  (let [event (let [state (atom {:cancel false})]
                (reify
                  mocks/Cancel
                  (isCancelled [this]
                    (:cancel @state))
                  (setCancelled [this bool]
                    (swap! state update-in [:cancel] (constantly bool)))
                  mocks/Player
                  (getPlayer [this] nil)
                  mocks/ItemDrop
                  (getItemDrop [this]
                    (reify
                      mocks/ItemStack
                      (getItemStack [this]
                        (reify
                          mocks/ItemMeta
                          (getItemMeta [this]
                            (reify
                              mocks/DisplayName
                              (getDisplayName [this]
                                "Magic Compass")))))))))]
    (is (with-redefs [l/send-message do-nothing]
          (PlayerDropItemEvent event)
          (true? (.isCancelled event))))))

