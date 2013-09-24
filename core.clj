(ns demosamples.core
  (:import [java.lang Character] )
  )

(use '[clojure.string :only (join split replace-first split-lines)])

(defn mix-up-content [sc]
  (loop [old sc
         new ""]
    (let [pick-pos (rand-int (count old))
          c (get old pick-pos)]
      (if (= old "")
          new
          (recur (replace-first old (str c) "") (str new c))))))

(defn word-without-bound [w]
  (apply str (butlast (rest w))))

(defn is-lastchar-special? [w]
  (not (Character/isLetterOrDigit (last w)))
  )

(defn mix-up-word [w]
  (let [cw (if (is-lastchar-special? w) (apply str (butlast w)) w )
        sw (str (first cw)
                (mix-up-content (word-without-bound cw))
                (last (rest cw)))
        rw (if (is-lastchar-special? w) (str sw (last w)) sw)]
    rw))

(defn to-words [s]
  (split s #"\s"))

(defn mix-up-paragraph [s]
  (let [ws (to-words s)
        mws (map mix-up-word ws)]
    (apply str (interpose " " mws))))

(defn mix-up-string [s]
  (let [ps (split-lines s)
        mps (map mix-up-paragraph ps)]
    (apply str (interpose "\r\n" mps))))

(defn shuffle-file-content [in-file out-file]
  (let [s (slurp in-file)]
    (spit out-file  (mix-up-string s))))
