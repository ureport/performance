(ns anonymizer.sql
  (:use [clojure.tools.cli :only [cli]])
  (:require [clojure.java.jdbc :as sql])
  (:gen-class)
  (import (java.sql DriverManager Connection ResultSet Timestamp)))

;;http://jdbc.postgresql.org/documentation/head/query.html
;;http://www.postgresql.org/docs/8.1/static/
(def local-database-url "jdbc:postgresql://localhost:5432/ureport_perf")

(defn random-number [length]
  (let [s (str (rand))
        c (count s)]
    (subs s 2 (+ length 2))))

(defn pick-random-char [str]
  (get str (int (rand (count str)))))

(defn random-phone-number []  
  (str (apply str (repeatedly 3 (partial pick-random-char "+-"))) (random-number 10)))


(defn map-from-rs [rs]
  {:name (.getString rs "name")
   :birthdate (.getString rs "birthdate")})

(defn map-from-rs-connection [rs]
  {:identity (.getString rs "identity")})


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




(defn update-row-contact [rs row]
  (let [before (map-from-rs rs)]
    (.updateString rs "name" (md5 (.getString rs "name")))
    (.updateTimestamp rs "birthdate" (Timestamp. (System/currentTimeMillis)))
    (.updateRow rs)
    (println (format "UPDATE ROW: [%d]"  row))
    (println (format "  before: %s" before))
    (println (format "  after : %s" (map-from-rs rs)))
))

(defn update-row-connection [rs row]
  (let [before (map-from-rs-connection rs)]
    (.updateString rs "identity" (random-phone-number))
    (.updateRow rs)
    (println (format "UPDATE ROW: [%d]"  row))
    (println (format "  before: %s" before))
    (println (format "  after : %s" (map-from-rs-connection rs)))
))


(defn next-row [rs]
  (let [success (.next rs)]
    success))

(defn loop-rows [rs, current-row, batch-size, func]       
  (loop [rs* rs
         row current-row] 
    (if (and  (< row (+ current-row batch-size))
              (next-row rs*))
      (do
        (func rs* row)                
        (recur rs* (+ row 1)))
      row)))

;; (format "SELECT id, name, birthdate FROM rapidsms_contact order by id LIMIT %d" max-rows)
(defn anonymise [database-url start-at batch-size max-rows update-func] 
  (println (format "anonymise start-at %d, batch-size %d, max-rows %d" start-at batch-size max-rows))
  (sql/with-connection database-url
    (with-open [stmt (.createStatement (sql/connection) ResultSet/TYPE_SCROLL_SENSITIVE ResultSet/CONCUR_UPDATABLE)]
      (.setFetchSize stmt batch-size)
      (with-open [rs (.executeQuery stmt (format "SELECT id, identity FROM rapidsms_connection order by id LIMIT %d" max-rows))]        
        (if (> start-at 1)
          (if (not (.absolute rs (- start-at 1)))
            (throw (Throwable. "You have asked to start beyond the end of the resultset!"))))

        (loop [rs* rs
               row start-at]
          (let [row* (sql/transaction
                      (loop-rows rs* row batch-size update-func))]             
            (if (> row* row)
              (recur rs* row*)
               row*)))        
        ))))


;; rapidsms_connection - identity
;;(anonymise 1 3 3)
(defn get-arg [args index]
  (get (apply vector args) index))

(defn -main [& args]
  (let [db-url (get-arg args 0)
        start-at (Integer/parseInt (get-arg args 1))
        batch-size (Integer/parseInt (get-arg args 2))
        limit (Integer/parseInt (get-arg args 3))]
    (prn "db-url" db-url "start-at" start-at ", batch-size " batch-size ", limit " limit)
    (let [updated-count (anonymise db-url start-at batch-size limit update-row-connection)]
      (println (format "Next Row to anonymise: [%d]" updated-count)))
))





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


