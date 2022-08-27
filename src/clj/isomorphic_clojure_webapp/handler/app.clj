(ns isomorphic-clojure-webapp.handler.app
  (:require [clojure.walk :refer [keywordize-keys]]
            [integrant.core :as ig]
            [rum.core :refer [render-html]]
            [next.jdbc :as jdbc]
            [honeysql.format :as sql]))

(defn db-spec
  [db]
  (:datasource (:spec db)))

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
    (let [tasks (jdbc/execute! (db-spec db) ["select * from tasks where id = ?" (get-in req [:path-params :id])])] 
      {:status 200
       :headers {"content-type" "application/json"}
       :body (first tasks)})))

(defmethod ig/init-key ::add [_ {:keys [db]}]
  (fn [req]
    (let [new-label (-> req :body-params keywordize-keys :label)]
      (jdbc/execute! (db-spec db) (sql/format {:insert-into :tasks,
                                               :columns [:label],
                                               :values [[new-label]]}))
      {:status 200
       :headers {"content-type" "application/json"}})))

(defmethod ig/init-key ::update [_ {:keys [db]}]
  (fn [req]
    (let [id (get-in req [:path-params :id])
          label (:label (keywordize-keys (get req :body-params)))]
      (jdbc/execute! (db-spec db) (sql/format {:update :tasks,
                                               :set {:label label},
                                               :where [:= :id id]}))
      {:status 200
       :headers {"content-type" "application/json"}})))

(defmethod ig/init-key ::delete [_ {:keys [db]}]
  (fn [req]
    (let [id (get-in req [:path-params :id])]
      (jdbc/execute! (db-spec db) (sql/format {:delete-from :tasks
                                               :where [:= :id id]}))
      {:status 200
       :headers {"content-type" "application/json"}})))
