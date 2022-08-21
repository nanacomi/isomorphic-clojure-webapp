(ns isomorphic-clojure-webapp.ui.pages.start
  (:require [rum.core :refer [defc use-state use-effect! use-ref]]
            [cljs-http.client :refer [get post]]
            [cljs.core.async :refer [go <!]]))


(defn add-task
  [task]
  (post "http://localhost:3000/tasks"
        {:json-params {:label task}}))


(defc input-task
  [input-ref on-add]
  [:form {:action "" :method "post"}
   [:input {:type "text"
            :style {:margin-right 10}
            :ref input-ref}]
   [:button {:type "button"
             :on-click on-add}
    "add"]])


(defc task-list
  [tasks]
  [:ul
   (map (fn [task] 
          [:li {:key (:tasks/id task)}
           (:tasks/label task)]) 
        tasks)])


(defc ui
  []
  (let [[tasks set-tasks!] (use-state nil)
        input-task-ref (use-ref nil)
        fetch-all (fn [] 
                      (go (let [response (<! (get "http://localhost:3000/tasks"))] 
                            (set-tasks! (:body response))))
                      #())
        on-add (fn [] 
                 (go (<! (add-task (.. input-task-ref -current -value))) 
                     (fetch-all)))
        _ (use-effect! fetch-all [])]
    [:div
     [:h1 "TODO LIST"]
     (input-task input-task-ref on-add) 
     (task-list tasks)]))