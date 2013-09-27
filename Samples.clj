(ns Samples)

;; Special Forms von Clojure
; def
(def f (fn [x] x))
(def config {:width 1280, 
             :height 640 
             :background-color :black 
             :foreground-color :white)

; if then else
(if  (= 1 0) 
  (println "Then Fall")
  (println "Else Fall"))

(if  (= 1 1) 
  (println "Then Fall")
  (println "Else Fall"))

; do Blockanweisung
(do
  (println "Erste Zeile")
  (println "Zweite Zeile")
  (println "Dritte Zeile")
  (println "Vierte Zeile"))

; let Binding
(let [a 1,
      b 2]
  (+ a b))

(let [msg "Der Artikel kostet"
      preis 12.45
      waehrung "Euro"]
  (str msg " " preis " " waehrung))

(let [x 1
      y x]
  y)

; Quoute
(1 2 3)         ; nicht erlaubt
'(1 2 3)        ; mit Quote ja, hier Reader Macro
(quote (1 2 3)) ; mit Quote ja, hier volle Schreibweise

; Funktionen
(def f (fn [x] x)) ; gibt den wert selber zurück.identität

; Rekursion loop recur

(def factorial
  (fn [n]
    (loop [cnt n, 
           acc 1]
       (if (zero? cnt)            
         acc          
         (recur (dec cnt) (* acc cnt))))))

; Exception
  (throw (Exception. "Test"))

; Try Catch finnaly

(try     
  (/ 1 0)     
  (catch Exception e (str "caught exception: " (.get_Message e))))

      
;; Ref sample 1
(def creator (ref "Anders Hejlsberg"))
(def language (ref "C#"))

(deref creator)
(deref language)
@creator
@language

(dosync
  (ref-set creator "Rich Hickey")
  (ref-set language "Clojure"))

(defn set-language [c l]
(dosync
  (ref-set creator c)
  (ref-set language l)))

(defn print-language []
  (str "Language " @language " is created by " @creator))

(defn change-string [s]
  (.ToUpper s))

(dosync 
  (alter creator change-string))

; Alternative to previous
(dosync 
  (alter creator #(.ToUpper %)))

;; Ref Sample 2
(def ref1 (ref 1))
(def ref2 (ref 1))

(defn lesen []
  (println "Thread1: Starte lesen")
  (dotimes [i 6]
    (System.Threading.Thread/Sleep 1000)
    (dosync
      (println "Ref1=" @ref1 " Ref2=" @ref2))))

(defn schreiben []
  (println "Thread2: Starte schreiben")
  (dosync
    (alter ref1 inc)
    (System.Threading.Thread/Sleep 3000)
    (alter ref2 inc))
  (println "Fertig schreiben"))

(do
  (future (lesen))
  (future (schreiben)))

;; Ref Sample 3
(def v (ref []))

(defn change-1 [value]
  (dosync
    (println "Changer1=" value)
    (System.Threading.Thread/Sleep 100)
    (alter v conj value)))

(defn change-2 [value]
  (dosync
    (println "Changer2=" value)
    (System.Threading.Thread/Sleep 150)
    (alter v conj value)))

(defn changer1 []
  (doseq [entry [1 2 3 4 5]]
    (change-1 entry)))

(defn changer2 []
  (doseq [entry ["a" "b" "c" "d" "e"]]
    (change-2 entry)))

(do
  (future (changer1))
  (future (changer2)))

;Ref Sample 4
; Ein wenig Spaß mit Bank Konten

; einfacher Account Record
(defrecord Account [nr name ammount])

; zwei Accounts
(def account1 (ref (Account. 1 "Thomas" 2000.0)))
(def account2 (ref (Account. 2 "Paul" 1000.0)))

; eine leere Liste für Logging Events
(def account-logs (ref ()))

; Funktion setzt die Loglist wieder zurück
(defn clear-account-logs []
  (dosync
    (ref-set account-logs ())))

(defn- base-transfer
  "Transferiert ammount von from nach to."
  [from to ammount]
  (alter from update-in [:ammount] - ammount)
  (alter to update-in [:ammount] + ammount))

(defn- base-transfer-log
  "Protokoliert einen Transfer"
  [from to ammount]
  (alter account-logs conj (str System.DateTime/Now " " ammount " " (:name @ from) "->" (:name @ to))))

(defn- create-safe-transfer [transfer-fn log-fn]
  (fn [from to ammount]
    (dosync
      (transfer-fn from to ammount)
      (log-fn from to ammount))))

; konstruiert eine Transfer Metode
(def transfer (create-safe-transfer base-transfer base-transfer-log))

; Hilfs Funktionen um das System auf Herz und Nieren zu testen
(defn many-transfers-sleep
  "Führt einen Transfer times mal aus.
   Zwischen jeden Transfer wird sleep Milisekunden gewartet"
  [from to ammount times sleep]
  (dotimes [x times]
    (System.Threading.Thread/Sleep sleep)
    (transfer from to ammount)))

(use 'clojure.pprint)

(defn status [& accounts]
  "Hilfsfunktion um die Werte auszuprinten"
  (let [accounts (apply vector accounts)]
    (pprint (count @account-logs))
    (pprint accounts)))

; transfer 100 dinger 10 mal und schläft 1000 milisekunden zwischen jeden transfer
(many-transfers-sleep account1 account2 100 10 1000)

(do
  (clear-account-logs)
  (future (many-transfers-sleep account1 account2 25 200 50))
  (future (many-transfers-sleep account2 account1 31 200 60))
  (future (many-transfers-sleep account1 account2 17 200 70))
  (future (many-transfers-sleep account1 account2 8 200 80))
  (future (many-transfers-sleep account2 account1 19 200 90)))

(do
  (clear-account-logs)
  (future (many-transfers-sleep account1 account2 25 50000 0))
  (future (many-transfers-sleep account2 account1 31 50000 0))
  (future (many-transfers-sleep account1 account2 17 50000 0))
  (future (many-transfers-sleep account1 account2 8 50000 0))
  (future (many-transfers-sleep account2 account1 19 50000 0)))

; Atom Sample 1
; Erzeugen
(def s (atom "Hallo"))

; Auslesen
(deref s)
@s

; Atom zurücksetzen
(reset! s "Bonjour")

; Atom ändern
(swap! s #(.ToUpper %))	

; Atom Sample 2
(defn memoize-1 [f]
  (let [mem (atom {})]
    (fn [& args]
      (if-let [e (find @mem args)]
        (val e)
        (let [ret (apply f args)]
          (swap! mem assoc args ret)
          ret)))))

(defn fib [n]
  (if (<= n 1)
    n
    (+ (fib (dec n)) (fib (- n 2)))))

(time (fib 30))

(def fib (memoize-1 fib))
 
(time (fib 30))

; Agent Sample 1
(def agt (agent "Hallo"))

; Auslesen
(deref agt)
@agt

; Ändern
(send agt #(.ToUpper %))
(send-off agt #(.ToLower %))

; Agent Sample 2
(use 'clojure.pprint)
(def agt2 (agent {:state "Created"}))

(defn modify-agent [a k v]      
  (System.Threading.Thread/Sleep 2000)
  (assoc a k v :state v))

@agt2

(do
  (send agt2 modify-agent :step-1 "Erster Schritt")
  (send agt2 modify-agent :step-2 "Zweiter Schritt")
  (send agt2 modify-agent :step-3 "Dritter Schritt")
  (send agt2 modify-agent :step-4 "Vierter Schritt")
  (send agt2 modify-agent :step-5 "Fünfter Schritt")
  (send agt2 modify-agent :step-6 "Sechster Schritt")
  (send agt2 modify-agent :step-7 "Siebter Schritt")
  (send agt2 modify-agent :step-8 "Achter Schritt")
  (send agt2 modify-agent :step-9 "Neunter Schritt")
  (send agt2 modify-agent :step-10 "Zehnter Schritt"))

; Warten, vielleicht für immer
(do
  (await agt2)
  (pprint @agt2))

; Warten für 5 Sekunden
(do
  (await-for 5000 agt2)
  (pprint @agt2))


; Sequence Library
; Erzeugen einer Sequnce
(range 20)
(range 5 20)
;(range)
(def n (range))
(take 20 n)
(take 20 (range))

(repeat 20 "A")
(repeat 20 [1 2 3])
;(repeat "forever")

(def r (repeat "forever"))
(take 20 r)
(take 20 (repeat "forever"))

(take 20 (cycle ["a" 1 "b" 2]))

(take 20 (iterate inc 1))
(take 20 (iterate #(/ % 2) 1))
(defn halbe [x]
  (/ x 2))
(take 20 (iterate halbe 1))

; repeatedly ist für funktionen mit Seiten Effekt
; #(rand-int 11) gibt immer andere Werte zurück
(take 5 (repeatedly #(rand-int 11)))

; Sequence auslesen
(first [ 1 2 3 4 5 6])
(rest [ 1 2 3 4 5 6]) ; (rest []) => ()
(next [ 1 2 3 4 5 6]) ; (next []) => nil
(last [ 1 2 3 4 5 6])
(butlast [ 1 2 3 4 5 6])
(second [ 1 2 3 4 5 6])

(nth ["a" "b" "c" "d" "e" "f"] 2)
(nth ["a" "b" "c" "d" "e" "f"] 0)
(nth ["a" "b" "c" "d" "e" "f"] 10000) ; gibt es nicht, also Error
(count ["a" "b" "c" "d" "e" "f"])


(first [["a" 1] ["b" 2] ["c" 3]])
(ffirst [["a" 1] ["b" 2] ["c" 3]]) ;a 
(nfirst [["a" 1] ["b" 2] ["c" 3]])  ;1

; Sequencen verändern
(def numbers (range 10))

(defn mal2 [x]
  (* x 2))

; drei mal der selbe effekt
(map mal2 numbers)
(map (fn [x] (* 2 x)) numbers)
(map #(* 2 %) numbers)

;map mit mehren collections. Stopt wenn die erste zu Ende ist
(defn my-plus [s1 s2]
  (+ s1 s2))

(map my-plus numbers (repeat 1000))
(map #(+ % %2) numbers (repeat 1000))
(map + numbers (repeat 1000))

; Beispiel Collection
(def order [
             {:produkt "Uhr"               :kunde "Andrea" :menge 3  :einzelpreis 80.70 }
             {:produkt "Schuhe"            :kunde "Andrea" :menge 20 :einzelpreis 45.50 }
             {:produkt "Ring"              :kunde "Andrea" :menge 1  :einzelpreis 200.00 }
             {:produkt "Fachzeitschrift"   :kunde "Thomas" :menge 2  :einzelpreis 10.00 }
             {:produkt "Frauenzeitschrift" :kunde "Andrea" :menge 5  :einzelpreis 1.50 }
             {:produkt "Schuhe"            :kunde "Thomas" :menge 1  :einzelpreis 120.00 }
             {:produkt "Tuperdosen"        :kunde "Andrea" :menge 25 :einzelpreis 2.50 }
             {:produkt "Handy"             :kunde "Andrea" :menge 1  :einzelpreis 450.00 }
             {:produkt "Handy"             :kunde "Thomas" :menge 1  :einzelpreis 655.00 }
             {:produkt "Drucker"           :kunde "Thomas" :menge 1  :einzelpreis 100.00 }
            ])
(count order)

; Ein Element der Map extrahieren
(defn get-produkt [m]
  (get m :produkt)
  ;(:produkt m)
  )
(map get-produkt order)
(map #(get % :produkt) order)
(map :produkt order)

; ein Element zu der Map hinzufügen und das über die ganze collection
; in der REPL Einzelschrit herleiten
(map #(assoc % :gesammtpreis (* (:einzelpreis %) (:menge %))) order)

; Sequence filtern
(filter even? [1 2 3 4 5])
(filter odd? [1 2 3 4 5])
(filter (complement odd?) [1 2 3 4 5])

(filter #(= (:kunde %) "Andrea") order)
(filter #(= (:produkt %) "Handy") order)
(filter #(> (:einzelpreis %) 200) order)

; Sequence sortieren
(sort [2 4 1 5 3 6])
(sort-by count ["Mary" "hat" "ein" "kleines" "Lamm"])
(sort-by first ["Mary" "hat" "ein" "kleines" "Lamm"])
(sort-by :kunde order)

; Sequencen grupieren
(group-by identity [1 2 3 2 1])
(group-by first ["Fischers" "Fritz" "fischt" "frische" "Fische"])
(group-by :kunde order)

; Reduce
; mit reduce kann man alles machen
(reduce + [1 2 3 4 5])
(reduce + 100 [1 2 3 4 5])
(reduce + [])

(defn my-plus [accumulator, listvalue]          
  (println "A=" accumulator " V=" listvalue)          
  (+ accumulator listvalue))

(reduce my-plus [1 2 3 4 5])
(reduce my-plus 100 [1 2 3 4 5])
(reduce my-plus [1])

(reduce * [1 2 3 4 5])
(reduce * 10 [1 2 3 4 5])
(reduce * 0 [1 2 3 4 5])

(defn prime-accumulator [primes number]           
  (if (some zero? (map #(mod number %) primes))             
    primes ; then            
    (conj primes number) ; else
    )) 

(reduce prime-accumulator [2] (range 3 1000))


;; Testing Framework
(use 'clojure.test)
(is (= 4 (+ 2 2)) "Zwei und Zwei sollte Vier ergeben")
(is (= 4 "4") "Vier ist Vier????")

(deftest parse-hallo-test
  (let [s "HALLO"]
    (is (= (.ToUpper "hallo") s) "Should be HALLO")
    (is (= (.ToUpper "Hallo") s) "Should be HALLO")
    (is (= (.ToUpper "hAllo") s) "Should be HALLO")
    (is (= (.ToUpper "haLlo") s) "Should be HALLO")
    (is (= (.ToUpper "halLo") s) "Should be HALLO")
    (is (= (.ToUpper "hallO") s) "Should be HALLO")
    (is (= (.ToUpper "HAllo") s) "Should be HALLO")
    (is (= (.ToUpper "halLO") s) "Should be HALLO")    
    ))

;; Ausführen von Tests
(run-tests 'Samples)
(run-tests 'user)

; Mocking
(defn generate-rand []
  (take 5 (repeatedly #(rand-int 100))))

(defn limit-to [limit]
  (map #(if (> % limit) limit %) (generate-rand)))

; funktioniert nicht, da Werte Zufallsmäßig erzeugt werden
(comment
  (deftest limit-to-test
    (let [res '(50 50 33 35 50)]
      (is (= (limit-to 50) res) "Sollte die richtige Sequence sein")))
  )

(deftest limit-to-test-mock-generate-rand
  (with-redefs [generate-rand (fn [] (seq [100 98 33 35 67]))]
    (let [res '(50 50 33 35 50)]    
      (is (= (limit-to 50) res) "Sollte die richtige Sequence sein"))))

(deftest limit-to-test-mock-rnd-int-1
  (with-redefs [rand-int (fn [val] 100)]
    (let [res '(50 50 50 50 50)]    
      (is (= (limit-to 50) res) "Sollte die richtige Sequence sein"))))

(deftest limit-to-test-mock-rnd-int-2
  (with-redefs [rand-int (fn [val] 0)]
    (let [res '(0 0 0 0 0)]    
      (is (= (limit-to 50) res) "Sollte die richtige Sequence sein"))))

(defn create-rand-int-simulator [& xs]    
  (let [cnt (atom 0)]  
    (fn [x] 
      (let [val (nth xs (mod @cnt (count xs)))]      
        (swap! cnt inc)
        val))))

(deftest limit-to-test-mock-rnd-int-3
  (with-redefs [rand-int (create-rand-int-simulator 45 55)]
    (let [res '(45 50 45 50 45)]    
      (is (= (limit-to 50) res) "Sollte die richtige Sequence sein"))))

;; Visual Studio Integration
; Project Templates zeigen
; File Templates zeigen
; REPL Zeigen
; Context Menü Solution Explorer
; Context Menü File Explorer

; defn macro
(defn mul [x y] (* x y))                    ; Erzeugt eine Funktion
(macroexpand '(defn mul [x y] (* x y)))     ; Expandiert das macro
(def mul (clojure.core/fn ([x y] (* x y)))) ; der eigentliche Code der evaluiert wird

; time macro
(time (reduce + (range 10000000)))          ; stoppt die Zeit, gibt aber das Ergebnis des reduce zurück
; user => "Elapsed time: 4078 msecs"
; user => 49999995000000

(macroexpand '(time (reduce + (range 10000000))))
(let* [start (. clojure.lang.RT (StartStopwatch)) 
       ret (reduce + (range 10000000))] 
  (prn (str "Elapsed time: " (. clojure.lang.RT StopStopwatch) " msecs")) 
  ret)

; ->> thread last macro
(reduce + (map #(* 10 %) (filter even? (range 10))))
(->> (range 10)
     (filter even? ,,,)
     (map #(* 10 %) ,,,)
     (reduce +) ,,,)

(macroexpand-all '(->> (range 10)
     (filter even? ,,,)
     (map #(* 10 %) ,,,)
     (reduce +) ,,,))

; -> thread first macro
(first (.Replace (.ToUpper "a b c d") "A" "X"))
(-> "a b c d"          
  .ToUpper ,,, 
  (.Replace ,,, "A" "X") 
  first ,,,)

(macroexpand-all '(-> "a b c d"                                
                    .ToUpper ,,, 
                    (.Replace ,,, "A" "X") 
                    first ,,,))


;; Debug Makros
; druckt einzelen Werte oder Anweisungen aus
; die Klammern müßen jedesmal rausgefummelt werden
(defmacro dbg [x] 
  `(let [x# ~x] (println "dbg:" '~x "=" x#) x#))

(+ (* 2 4) (* 5 6))
(dbg (+ (* 2 4) (* 5 6)))
(dbg (+ (dbg (* 2 4)) (dbg (* 5 6))))

(let [a 3,
      b 4]
  (Math/Sqrt (+ (* a a) (* b b))))

(let [a 3,
      b 4]
  (dbg (Math/Sqrt (+ (* a (dbg a)) (* (dbg b) b)))))

(use 'clojure.walk)
(macroexpand-all 
  '(let [a 3,      
         b 4]  
     (dbg (Math/Sqrt (+ (* a (dbg a)) (* (dbg b) b))))))

; druckt einzelne Anweisungen aus, aber keine Werte
; einfacher zu entfernen
(defmacro dbg-prn  
  "Debugging form that prints out results"  
  [& more]  
  `(let [start# ~more]     
     (print '~more "==>" start# "\n")     
     start#))

(+ (* 2 4) (* 5 6))
(dbg-prn + (dbg-prn * 2 4) (dbg-prn * 5 6))




; mehrere collection zusammenführen
(concat  [1 2 3] ["a" "b" "c" "d" "e"] [:key1 :key2 :key3])






















































































