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
    (let [new-label (get (clojure.walk/keywordize-keys (get req :body-params)) :label)]
      ;; (jdbc/execute! (db-spec db) [(format "insert into tasks(\"label\") values(\"%s\")" new-label)])
      (jdbc/execute! (db-spec db) (sql/format {:insert-into :tasks,
                                               :columns [:label],
                                               :values [[new-label]]}))
      {:status 200
       :headers {"content-type" "application/json"}
       :body "ok"})))

(defmethod ig/init-key ::update [_ {:keys [db]}]
  (fn [req]
    (let [id (get-in req [:path-params :id])
          label (get (clojure.walk/keywordize-keys (get req :body-params)) :label)]
      (jdbc/execute! (db-spec db) (sql/format {:update :tasks,
                                               :set {:label label},
                                               :where [:= :id id]}))
      {:status 200
       :headers {"content-type" "application/json"}
       :body "ok"})))

(defmethod ig/init-key ::delete [_ {:keys [db]}]
  (fn [req]
    (let [id (get-in req [:path-params :id])]
      (jdbc/execute! (db-spec db) (sql/format {:delete-from :tasks
                                               :where [:= :id id]}))
      {:status 200
       :headers {"content-type" "application/json"}
       :body "ok"})))
