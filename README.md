This repo is mainly just an exercise for me to learn the [Om library](https://github.com/swannodette/om). It's a quick attempt to implement an autocompleter input widget
that contains a small subset of the functionality described by this post:
http://swannodette.github.io/2013/08/17/comparative/

The widget gets invoked as follows:
```clojure
(ns om-ac-ex.app
  (:require [om-ac-ex.widget :as widget])
...
(om/build widget/ac-input app
  {:state {:sel-ch sel-ch
           :ac-fn #(search 10 utils/cc-vars %)}})
```
`:ac-fn` specifies a function that takes a single argument (the value of the input field) and returns a vector of possible completions. The original autocompletion example I linked to above used an asynchronous process to populate the list of possible completions, but in my example I just update the list with whatever is returned from calling `:ac-fn`. Eventually, I would like to make this asynchronous to support fetching data from a remote server. For the purposes of this example I've just hard-coded the list of vars in clojure.core to use as the completion pool.
Another feature missing is support for mouse events; my example is keyboard only for now.
While going through this exercise I found myself wanting to use a swap!-like call on the component state, so I wrote this utility function, based on om.core/set-state!:
```clojure
(defn swap-state!
  "Takes a pure owning component and sets the state of the component to be:
   (apply f current-state args)."
  ([owner f]
     (om/allow-reads
      (let [props  (.-props owner)
            state  (.-state owner)
            cursor (aget props "__om_cursor")
            path   (om/-path cursor)
            pstate (or (aget state "__om_pending_state")
                       (aget state "__om_state"))]
        (aset state "__om_pending_state" (f pstate))
        ;; invalidate path to component
        (if (empty? path)
          (swap! (om/-state cursor) clone)
          (swap! (om/-state cursor) update-in path clone)))))
  ([owner f x]
     (swap-state! owner #(f % x)))
  ([owner f x y]
     (swap-state! owner #(f % x y)))
  ([owner f x y & args]
     (swap-state! owner #(apply f % x y args))))
```