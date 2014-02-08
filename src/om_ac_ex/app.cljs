(ns om-ac-ex.app
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [cljs.core.async :refer [<! chan]]
            [om-ac-ex.utils :as utils]
            [om-ac-ex.widget :as widget]))

(enable-console-print!)
(def app-state (atom {:var "No selection"}))

(defn search [n data s]
  "Returns a vector of n strings from data that contain substring s.
   Strings beginning with s are sorted ahead of others."
  (vec (take n
             (concat
              (filter #(-> %  (.indexOf s) (= 0)) data)
              (filter #(-> %  (.indexOf s) (> 0)) data)))))

(defn main [app owner]
  (reify
    om/IWillMount
    (will-mount [_]
      (let [sel-ch (chan)]
        (utils/swap-state! owner assoc :sel-ch sel-ch)
        (go (while true
              (let [v  (<! sel-ch)]
                (om/transact! app #(assoc % :var v)))))))
    om/IRenderState
    (render-state [_ {:keys [sel-ch]}]
      (dom/div #js {:className "main"}
        (om/build widget/ac-input app
          {:state {:sel-ch sel-ch
                   :ac-fn #(search 10 utils/cc-vars %)}})
        (dom/span nil (str "Selection: " (:var app)))))))

(om/root app-state main (.getElementById js/document "app"))
