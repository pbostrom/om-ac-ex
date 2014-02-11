(ns om-ac-ex.widget
  (:require-macros [cljs.core.match.macros :refer [match]])
  (:require [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [cljs.core.async :refer [put!]]
            [cljs.core.match]
            [om-ac-ex.utils :as utils]))

(defn map-hotkey [e]
  (when (and (.-ctrlKey e) (.-shiftKey e))
   (cond
    (= (.-keyCode e) 88) :eval-tlf
    (= (.-keyCode e) 90) :load-buf)))

(defn display [show]
  (if show
    #js {}
    #js {:display "none"}))

(defn highlight [i sel-i]
  (if (== i sel-i)
    #js {:backgroundColor "#b6d6fd"}
    #js {}))

(def ENTER 13)
(def UP_ARROW 38)
(def DOWN_ARROW 40)
(def TAB 9)
(def ESC 27)

(def CTRL-KEYS #{UP_ARROW DOWN_ARROW ENTER TAB ESC})

(defn key->keyword [code]
  (condp = code
    UP_ARROW   :previous
    DOWN_ARROW :next
    ENTER      :select
    TAB        :select
    ESC        :exit))

(defn handle-ctrl-key [{:keys [sel-idx results] :as state} keycode]
  (let [kw (key->keyword keycode)
        list-size (count results)]
    (match [sel-idx kw]
      [::none :next    ] (assoc state :sel-idx 0)
      [::none :previous] (assoc state :sel-idx (dec list-size))
      [_      :next    ] (assoc state :sel-idx (mod (inc sel-idx) list-size))
      [_      :previous] (assoc state :sel-idx (mod (dec sel-idx) list-size))
      [_      :exit    ] (assoc state :sel-idx ::none :show-list false)
      [::none :select  ] (assoc state :show-list false)
      [_      :select  ] (assoc state :show-list false :val (results sel-idx)))))

(defn ac-input [app owner]
  (reify
    om/IInitState
    (init-state [x]
      {:results []
       :sel-idx ::none
       :val ""
       :md ::none
       :show-list false})
    om/IRenderState
    (render-state [this {:keys [results sel-idx show-list val sel-ch ac-fn] :as state}]
      (dom/div #js {:className "ac-comp"}
        (dom/span #js {:className "ac-box"}
          (dom/input #js {:type "text"
                          :onChange (fn [e]
                                      (let [val (str (.. e -target -value))]
                                        (utils/swap-state! owner assoc
                                          :show-list true
                                          :sel-idx ::none
                                          :val val
                                          :results
                                          (ac-fn val))))
                          :onKeyDown (fn [e]
                                       (when-let [k (CTRL-KEYS (.-keyCode e))]
                                         (.preventDefault e)
                                         (utils/swap-state! owner handle-ctrl-key k)
                                         (when (= (key->keyword k) :select)
                                           (put! sel-ch (om/get-state owner :val)))))
                          :onBlur #(om/set-state! owner :show-list true)
                          :value val})
          (apply dom/ul #js {:className "ac-menu"
                             :style (display show-list)}
            (map-indexed
             (fn [i v]
               (dom/li #js {:style (highlight i sel-idx)
                            :onMouseDown #(om/set-state! owner :md i)
                            :onMouseUp (fn [_]
                                         (if (= (om/get-state owner :md) i)
                                           (do
                                             (utils/swap-state! owner
                                               (fn [s]
                                                 (-> s
                                                     (handle-ctrl-key ENTER)
                                                     (assoc :md ::none))))
                                             (put! sel-ch (om/get-state owner :val)))
                                           (om/set-state! owner :md ::none)))
                            :onMouseLeave #(om/set-state! owner :sel-idx ::none)
                            :onMouseEnter #(om/set-state! owner :sel-idx i)} v)) results)))))))
