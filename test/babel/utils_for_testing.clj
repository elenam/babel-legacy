(ns babel.utils-for-testing)

;#########################################
;### Utils for generating test patterns ##
;#########################################

(defn- s->pattern
  "Takes a string or a regular expression and returns a
  string that can be then combined with other strings to be
  passed to re-pattern"
  [s]
  (if (string? s)
      (java.util.regex.Pattern/quote s)
      (.pattern s)))

(defn make-pattern
  "Takes a sequence of strings and regexes and returns
  a compiled regex pattern composed of the strings as literals
  and the pattrens of the regexes. Dot matches newlines."
  [& s]
  (re-pattern (str "(?s)"
                   (apply str (map s->pattern s))
                   "(.*)")))
