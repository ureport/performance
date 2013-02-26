(ns anonymizer.sql
  (:use [clojure.tools.cli :only [cli]])
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
    (println (format "  UPDATE: row: %d"  row))
    (prn "    before: " before)
    (prn "    after:  " (map-from-rs rs))
))

(defn next-row [rs]
  (let [success (.next rs)]
    ;;(println (format "  NEXT-SUCCESS: %s" success))
    success))

(defn loop-rows [rs, current-row, batch-size, func]       
  (loop [rs* rs
         row current-row] 
    ;;(prn "LOOP: row " row ", max " (+ current-row batch-size))
    (if (and  (< row (+ current-row batch-size))
              (next-row rs*))
      (do
        (func rs* row)                
        (recur rs* (+ row 1)))
      row)))


(defn anonymise [start-at batch-size max-rows] 
  (println (format "anonymise start-at %d, batch-size %d, max-rows %d" start-at batch-size max-rows))
  (sql/with-connection database-url
    (with-open [stmt (.createStatement (sql/connection) ResultSet/TYPE_SCROLL_SENSITIVE ResultSet/CONCUR_UPDATABLE)]
      (.setFetchSize stmt batch-size)
      (with-open [rs (.executeQuery stmt "SELECT id, name, birthdate FROM rapidsms_contact order by id LIMIT 3")]        
        (if (> start-at 1)
          (if (not (.absolute rs (- start-at 1)))
            (throw (Throwable. "You have asked to start beyond the end of the resultset!"))))

        (loop [rs* rs
               row start-at]
          (let [row* (sql/transaction
                      (loop-rows rs* row batch-size update-row))] 
            ;;(prn "--> row* " row* ", row " row)
            (if (> row* row)
              (recur rs* row*)
               row*)))        
        ))))

;;(anonymise 1 3 3)

(defn -main [& args]
  (let [start-at (Integer/parseInt (first args))
        batch-size (Integer/parseInt (second args))
        limit (Integer/parseInt (last args))]
    (prn "start-at" start-at ", batch-size " batch-size ", limit " limit)
    (let [updated-count (anonymise start-at batch-size limit)]
      (prn "Got upto row: " updated-count))))





;; (def conn (DriverManager/getConnection database-url))

;; (.setAutoCommit conn true)

;; (def stmt (.createStatement conn ResultSet/TYPE_SCROLL_SENSITIVE ResultSet/CONCUR_UPDATABLE))

;; (def rs (.executeQuery stmt "SELECT id, name, birthdate FROM rapidsms_contact order by id LIMIT 3"))

;; (next-row rs)
;;(.absolute rs 3)

;; (defn print-row [rs row]
;;   (prn row " : "(.getString rs "name")))


;; (loop-rows rs, 1,  2, print-row)

;; (def rs-meta (.getMetaData rs))

;; (.getColumnTypeName rs-meta 3) ;; timestampz
;; (.getColumnTypeName rs-meta 2) ;; varchar 

;; (.absolute rs 10)
;; (.getString rs "name")
;; (.getString rs "birthdate")

;; (.updateString rs "name" (md5 (.getString rs "name")))
;; (.updateTimestamp rs "birthdate" (Timestamp. (System/currentTimeMillis)))
;; (.updateRow rs)

;; (.close rs)
;; (.close stmt)
;; (.close conn)
