;; (def conn (DriverManager/getConnection database-url))

;; (.setAutoCommit conn false)

;; (def stmt (.createStatement conn ResultSet/TYPE_SCROLL_SENSITIVE ResultSet/CONCUR_UPDATABLE))

;; (def rs (.executeQuery stmt "SELECT id, name, birthdate FROM rapidsms_contact order by id LIMIT 10"))


;; (defn print-row [rs row]
;;   (prn row " : "(.getString rs "name")))


;; (loop-rows rs, 4,  3, print-row)

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
