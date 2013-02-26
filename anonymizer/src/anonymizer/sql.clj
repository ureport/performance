(ns anonymizer.sql
  (:require [clojure.java.jdbc :as sql])
  (:gen-class)
  (import (java.sql DriverManager Connection ResultSet Timestamp)))

;;http://jdbc.postgresql.org/documentation/head/query.html
;;http://www.postgresql.org/docs/8.1/static/
(def database-url "jdbc:postgresql://localhost:5432/ureport_perf")


(defn map-from-rs [rs]
  {:name (.getString rs "name")
   :birthdate (.getString rs "birthdate")})

;; timestamptz and varchar
(defn debug-meta [rs]
        (let [rs-meta (.getMetaData rs)]
           (prn (.getColumnTypeName rs-meta 3)) 
           (prn (.getColumnTypeName rs-meta 2))))


(defn prepare-update-cursor-stmt [sql] 
  (sql/prepare-statement (sql/connection) sql 
                         :result-type :scroll-insensitive
                         :concurrency :updatable
                         :fetch-size 20
                         :max-rows 20))

 
;;http://docs.oracle.com/javase/1.4.2/docs/api/java/sql/ResultSet.html

(defn md5
  "Generate a md5 checksum for the given string"
  [token]
  (let [hash-bytes
         (doto (java.security.MessageDigest/getInstance "MD5")
               (.reset)
               (.update (.getBytes token)))]
       (.toString
         (new java.math.BigInteger 1 (.digest hash-bytes)) ; Positive and the size of the number
         16))) ; Use base16 i.e. hex




(defn update-row [rs row]
  (let [before (map-from-rs rs)]
    (.updateString rs "name" (md5 (.getString rs "name")))
    (.updateTimestamp rs "birthdate" (Timestamp. (System/currentTimeMillis)))
    (.updateRow rs)
    ;;(prn "row: " row)
    (prn (format "[%d] " row) "before: " before)
    (prn (format "[%d] " row) "after:  " (map-from-rs rs))
))


(defn loop-rows [rs, current-row, batch-size, func]
  (loop [rs* rs
         row current-row]  
    (if (and (< row (+ current-row batch-size))
             (.next rs*))
      (do 
        (func rs row)
        (recur rs* (+ row 1)))
      row)))


(defn anonymise [batch-size max-rows]
  (sql/with-connection database-url
    (with-open [stmt (.createStatement (sql/connection) ResultSet/TYPE_SCROLL_SENSITIVE ResultSet/CONCUR_UPDATABLE)]
      (.setFetchSize stmt 50)
      (with-open [rs (.executeQuery stmt (format "SELECT id, name, birthdate FROM rapidsms_contact order by id LIMIT %d" max-rows))]
        (loop [rs* rs
               row 1]
          (let [row* (sql/transaction
                      (loop-rows rs* row batch-size update-row))]            
            (if (> row* row)
              (recur rs* row*)
              (- row* 1))))        
        ))))

;;(anonymise 20 )

(defn -main [& args]
  (prn "batch-size " (first args) ", limit " (second args)))