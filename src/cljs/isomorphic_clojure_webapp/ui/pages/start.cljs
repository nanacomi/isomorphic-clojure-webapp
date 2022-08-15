(ns isomorphic-clojure-webapp.ui.pages.start
  (:require [rum.core :refer [defc use-effect! use-state]]
            [cljs-http.client :refer [get]]
            [cljs.core.async :refer [go <!]]))


(defc ui
  []
  (let [[tasks set-tasks!] (use-state nil)
        _ (use-effect!
           (fn []
             (go (let [response (<! (get "http://localhost:3000/tasks"))]
                   (set-tasks! (:body response))))
             #())
           [])
        ]
    [:div
     [:h1 "TODO LIST"]
     [:ul
      (for [task tasks]
        [:li {:key (:tasks/id task)} (:tasks/label task)])]]))
