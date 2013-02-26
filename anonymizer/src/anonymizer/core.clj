(ns anonymizer.core
  (:require [clojure.java.jdbc :as sql])
  (:gen-class)
  (import java.sql.ResultSet))

;; http://asymmetrical-view.com/2010/10/14/clojure-and-large-result-sets.html

(def database-url "postgresql://localhost:5432/ureport_perf")

;; (sql/with-connection database-url
;;          (sql/create-table :testing [:data :text]))

;;  (sql/with-connection database-url
;;     (sql/with-query-results results ["select * from rapidsms_contact LIMIT 20"]
;;       (doall results)))


(def default-fetch-size 50)
 
(defn with-query-results-cursor [[sql & params :as sql-params] func]
  (sql/transaction
   (with-open [stmt (.prepareStatement (sql/connection) sql)]
     (doseq [[index value] (map vector (iterate inc 1) params)]
       (.setObject stmt index value))
     (.setFetchSize stmt default-fetch-size)
     (with-open [rset (.executeQuery stmt)]
       (func (resultset-seq rset))))))
 
  ;; example usage:

(defn all-rows [table]
  (sql/with-connection database-url
    (with-query-results-cursor [(format "SELECT * FROM %s" table)]
      (fn [rs]
        (doseq [rec rs]
          (println (format "%s\t: %s" (:id rec) (:name rec))))))))



(defn updateable-cursor [[sql & params :as sql-params] func]
  (sql/transaction
   (with-open [stmt (.prepareStatement (sql/connection) sql ResultSet/TYPE_SCROLL_SENSITIVE ResultSet/CONCUR_UPDATABLE)]
     (doseq [[index value] (map vector (iterate inc 1) params)]
       (.setObject stmt index value))
     (.setFetchSize stmt default-fetch-size)
     (with-open [rset (.executeQuery stmt)]
       (func rset)))))

(defn update-all-rows [table]
  (sql/with-connection database-url
    (with-query-results-cursor [(format "SELECT * FROM %s LIMIT 10" table)]
      (fn [rs]        
        (doseq [record rs]
               (println (:name record)))))))
 

(defn -main [& args]
  (all-rows "rapidsms_contact"))