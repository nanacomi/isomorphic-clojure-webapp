(ns isomorphic-clojure-webapp.handler.app
  (:require [integrant.core :as ig]
            [rum.core :refer [render-html]]
            [next.jdbc :as jdbc]
            [honeysql.format :as sql]))

(def db-spec (fn [db] (:datasource (:spec db))))

(defmethod ig/init-key ::index [_ _]
  (fn [req]
    (prn req)
    {:status 200
     :headers {"content-type" "text/html"}
     :body (render-html [:html
                         [:body
                          [:#app]
                          [:script {:src "/js/main.js"}]]])}))

(defmethod ig/init-key ::all [_ {:keys [db]}]
  (fn [req]
    (let [tasks (jdbc/execute! (db-spec db) (sql/format {:select [:*] :from [:tasks]}))]
      {:status 200
       :headers {"content-type" "application/json"}
       :body tasks})))

(defmethod ig/init-key ::fetch [_ {:keys [db]}]
  (fn [req]
    (let [tasks (jdbc/execute! (db-spec db) [(format "select * from tasks where id = %s" (get-in req [:path-params :id]))])] 
      {:status 200
       :headers {"content-type" "application/json"}
       :body (first tasks)})))

(defmethod ig/init-key ::add [_ {:keys [db]}]
  (fn [req]
    {:status 200
       :headers {"content-type" "application/json"}
       :body "ok"}))
