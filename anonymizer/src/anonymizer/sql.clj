(ns anonymizer.sql
  (:require [clojure.java.jdbc :as sql])
  (import (java.sql DriverManager Connection ResultSet Timestamp)))

;;http://jdbc.postgresql.org/documentation/head/query.html
;;http://www.postgresql.org/docs/8.1/static/
(def database-url "jdbc:postgresql://localhost:5432/ureport_perf")


(defn sql-command [conn]
  (str conn))



ResultSet/TYPE_SCROLL_SENSITIVE ResultSet/CONCUR_UPDATABLE
(defn prepare-update-cursor-stmt [sql] 
  (sql/prepare-statement (sql/connection) sql 
                         :result-type :scroll-insensitive
                         :concurrency :updatable
                         :fetch-size 20
                         :max-rows 20))


(sql/with-connection database-url
  (sql/transaction
   (with-open [stmt (prepare-update-cursor-stmt "SELECT * FROM rapidsms_contact")]    
     (prn stmt)
     
     )))

(sql/with-connection database-url
  (with-open [stmt (.createStatement sql/connection)]
    (prn stmt)
             ))

#http://docs.oracle.com/javase/1.4.2/docs/api/java/sql/ResultSet.html

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

(md5 "This is my name")

(def conn (DriverManager/getConnection database-url))

(def stmt (.createStatement conn ResultSet/TYPE_SCROLL_SENSITIVE ResultSet/CONCUR_UPDATABLE))

(def rs (.executeQuery stmt "SELECT id, name, birthdate FROM rapidsms_contact order by id LIMIT 10"))

(def rs-meta (.getMetaData rs))

(.getColumnTypeName rs-meta 3) ;; timestampz
(.getColumnTypeName rs-meta 2) ;; varchar 

(.absolute rs 10)
(.getString rs "name")
(.getString rs "birthdate")

(.updateString rs "name" (md5 (.getString rs "name")))
(.updateTimestamp rs "birthdate" (Timestamp. (System/currentTimeMillis)))
(.updateRow rs)

(.close rs)
(.close stmt)
(.close conn)

(sql/with-connection database-url
  (with-open [stmt (.createStatement (sql/connection) ResultSet/TYPE_SCROLL_SENSITIVE ResultSet/CONCUR_UPDATABLE)]
    (.setFetchSize stmt 50)
    (with-open [rs (.executeQuery stmt "SELECT id, name, birthdate FROM rapidsms_contact order by id")]
      ;;(debug-meta rs) 
      (sql/transaction
       (update-row rs 13))
      )))

(defn update-row [rs location]
  (.absolute rs location)
  (let [before (map-from-rs rs)]
    (.updateString rs "name" (md5 (.getString rs "name")))
    (.updateTimestamp rs "birthdate" (Timestamp. (System/currentTimeMillis)))
    (.updateRow rs)
    (prn "before: " before)
    (prn "after:  " (map-from-rs rs))))

(defn map-from-rs [rs]
  {:name (.getString rs "name")
   :birthdate (.getString rs "birthdate")})

;; timestamptz and varchar
(defn debug-meta [rs]
        (let [rs-meta (.getMetaData rs)]
           (prn (.getColumnTypeName rs-meta 3)) 
           (prn (.getColumnTypeName rs-meta 2))))

