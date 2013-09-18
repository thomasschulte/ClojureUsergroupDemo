(ns Interop)

(comment
  ;; Member access
;(.instanceMember instance args*)
  (. ToUpper "Hallo")
  (. Replace "Hallo" "a" "e")
  (. Replace "Hallo" \a \e)
  (. Length "Hallo")

;(.instanceMember Classname args*)
;(Classname/staticMethod args*)
  (String/Compare "foo" "bar")
  (String/Format "Value={0}" 1)

;Classname/staticField
  String/Empty
  Math/PI

;;Dot special form
  (. "Hallo" ToUpper)
  (. "Hallo" Length)
  (. "Hallo" (Replace "a" "e"))
  (. "Hallo" Replace "a" "e")

;;Instantiation
  (Uri. "http://www.google.de")
  (new Uri "http://www.google.de")

;; Assignment 
  (System.Reflection.Assembly/LoadWithPartialName "System.Data")
  (import ' System.Data.DataTable)
  (def dt (DataTable.))
  (. set_CaseSensitive dt true)
  (. get_CaseSensitive dt)
  (. get_Columns dt)
  (. Add (. get_Columns dt) "First")
  (. . dt Columns (Add "Second"))
  (def cs (seq (. . dt Columns)))

  (System.Reflection.Assembly/LoadWithPartialName "Microsoft.VisualBasic")
  (import ' Microsoft.VisualBasic.DateAndTime)
  DateAndTime/Now
  )