(ns DatentypenundDatenstrukturen)

(comment
  ; Datentypen
  (class 1)
  (class 3.14)
  (class 1.25M)
  (class "Hello")
  (class 'foo)
  (class :firstname)
  (class true)
  (class nil)
  (class 1/3)
  (class #"[A-Z]")

  ;Datenstrukturen List
  (list 1 2 3 4)
  (def v 1)

  ;Datenstrukturen Vektor
  [1 2 3 4]
  ["Thomas" "Paul" "Andreas"]

  ;Datenstrukturen Map
  {:firstname "Thomas", :lastname "Schulte"}
  {"Postleitzahl" 58642, "Hausnummer" 12}

  ;Datenstrukturen Set
  #{"C" "C++" "C#" "Clojure" "VB.NET"}
  #{:red :green :blue}

  ; Mixen von Datenstrukturen und typen
  [1 2 "drei" :vier 25/5]

  {:firstname "Thomas"
   :lastname "Schulte"
   :adress {:street "Auf dem Loh 12"
            :zipcode 58642
            :city "Iserlohn"}
   }



  )