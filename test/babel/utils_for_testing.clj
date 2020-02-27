(ns babel.utils-for-testing)

;#########################################
;### Utils for generating test patterns ##
;#########################################

(defn make-pattern
  "Takes a string and returns a compiled regex pattern for containing
  that string as a literal. Dot matches newlines."
  [s]
  (re-pattern (str "(?s)" (java.util.regex.Pattern/quote s) "(.*)")))
