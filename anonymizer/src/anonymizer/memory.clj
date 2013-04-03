(ns anonymizer.memory
  (:gen-class))


(defn gen-string [source n] 
  (apply str (repeatedly n (fn [] (str source)))))


(spit "/Users/jmdb/test.txt" (gen-string "W" 100000))

(defn -main [& args]
  (while 
      true
    (let [contents (slurp "/Users/jmdb/test.txt")]
      (println (count contents)))))
