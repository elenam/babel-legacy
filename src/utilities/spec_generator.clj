(ns utilities.spec_generator
   (:require [clojure.string :as str]))

;;the only part of this hash map we still use is :has-type
;;the other parts used to be useful when I was also doing the re-defining, and may yet be useful
(def type-data {
                :arg   {:check nil,                   :has-type "arg",      :argument "arg",  :arg-vec ["arg"]},
                :coll  {:check "check-if-seqable?",   :has-type "seqable",  :argument "coll", :arg-vec ["coll"]},
                :n     {:check "check-if-number?",    :has-type "number",   :argument "n",    :arg-vec ["n"]},
                :r     {:check "check-if-ratio?",     :has-type "ratio",    :argument "r",    :arg-vec ["r"]},
                :colls {:check "check-if-seqables?",  :has-type "seqable",  :argument "args", :arg-vec ["&" "args"]},
                :str   {:check "check-if-string?",    :has-type "string",   :argument "str",  :arg-vec ["str"]},
                :strs  {:check "check-if-strings?",   :has-type "string",   :argument "strs", :arg-vec ["&" "strs"]},
                :f     {:check "check-if-function?",  :has-type "function", :argument "f",    :arg-vec ["f"]},
                :fs    {:check "check-if-functions?", :has-type "function", :argument "fs",   :arg-vec ["&" "fs"]}
                :args  {:check nil,                   :has-type "arg",      :argument "args", :arg-vec ["&" "args"]}})

(def re-type-replace
  "This hashmap used for replacing the keywords with strings."
  '{
    :arg   ":a any?"
    :coll  ":a (s/nilable coll?)"
    :n     ":a number?"
    :colls ":a (s/nilable coll?)"
    :str   ":a string?"
    :strs  ":a string?"
    :f     ":a ifn?"
    :fs    ":a ifn?"
    :args  ":a (s/* args)"
    :colls* ":a (s/* (s/nilable coll?))"
    :strs*  ":a (s/* string?)"
    :fs*    ":a (s/* function?)"
    :args*  ":a (s/* any?)"
    :r ":a ratio?"
    :arg? ":a (s/? any?)"
    :coll? ":a (s/? (s/nilable coll?))"
    :n? ":a (s/? number?)"
    :str? ":a (s/? string?)"
    :f? ":a (s/? ifn?)"
    :r? ":a (s/? ratio?)"
    :nil? ":a (s/? something)"
    :colls+ ":a (s/+ (s/nilable coll?))"
    :strs+  ":a (s/+ string?)"
    :fs+    ":a (s/+ function?)"
    :args+  ":a (s/+ any?)"
    nil ":a something"})

(def type-replace
  "This hashmap used for replacing the keywords with strings."
  '{
    :arg   :arg
    :coll  :coll
    :n     :n
    :colls :colls
    :str   :str
    :strs  :strs
    :f     :f
    :fs    :fs
    :args  [:coll :args]
    :r :r
    nil nil})

(def type-multi
  "This hashmap used for checking that certain keys exist."
  '{
    [:coll :colls] :colls
    [:str :strs]  :strs
    [:f :fs]    :fs
    [:arg :args]  :args})

(def type-single
  "This hashmap used for checking that certain keys exist and replacing them."
  '{
    [:arg]   :arg?
    [:coll]  :coll?
    [:n]     :n?
    [:str]   :str?
    [:f]     :f?
    [:r]     :r?
    [nil] :nil?})

(def type-single-no-vec
  "This hashmap used for checking that certain keys exist and replacing them."
  '{
    :arg   :arg?
    :coll  :coll?
    :n     :n?
    :str   :str?
    :f     :f?
    :r     :r?
    nil :nil?})

(def type-multi-replace
  "This hashmap used for checking that certain keys exist and replacing them."
  '{
    :colls :colls*
    :strs  :strs*
    :fs    :fs*
    :args  :args*})

(def type-multi-replace+
  "This hashmap used for replacing keys."
  '{
    :colls :colls+
    :strs  :strs+
    :fs    :fs+
    :args  :args+})

(def arg-type (merge
                     (zipmap [:seq] (repeat :seq)),
                     (zipmap [:map] (repeat :map-or-vector)),
                     (zipmap [:coll :c :c1 :c2 :c3 :c4 :c5 :m :ks] (repeat :coll)),
                     (zipmap [:maps] (repeat :maps-or-vectors)),
                     (zipmap [:n :number :step :start :end :size] (repeat :n)),
                     (zipmap [:r] (repeat :r)),
                     (zipmap [:arg :key :val :argument :x :y :test :not-found :init-val-or-seq :size-or-seq :body] (repeat :arg)),
                     (zipmap [:f :function :pred] (repeat :f)),
                     (zipmap [:fs :functions :preds] (repeat :fs)),
                     (zipmap [:colls :cs] (repeat :colls)),
                     (zipmap [:string :str :s] (repeat :str)),
                     (zipmap [:strs :strings :ss] (repeat :strs)),
                     (zipmap [:more :args :vals :arguments :xs :ys :forms :filters :keyvals] (repeat :args))))

(defn arglists->argtypes
  "arglists->argtypes takes a list of lists of arguments as strings and
   changes the arguments into keys corresponding to arg-type."
  [arglists]
  (map (fn [x] (map #(arg-type (keyword %)) x)) arglists))

(defn last-arglist
  "last-arglist takes a list of lists and gets the longest list in the
   list of lists."
  [arglists]
  (first (reverse (sort-by count arglists))))

(defn chompable?
  "chompable? checks if there are no empty lists or lists with & in them in a
   list of lists."
  [arglists]
  (or (not-any? #(< 1 (count %)) arglists)
    (= "&" (name (first (rest (reverse (last-arglist arglists))))))))

(defn remove-and
  "remove-and removes the second to last element in the longest coll in a
   coll of colls."
  [arglists]
  (let [sorted-arglists (reverse (sort-by count arglists))
	       f-arglists (first sorted-arglists)
	       r-arglists (reverse (rest sorted-arglists))]
     (->> (conj r-arglists (into (->> (drop-last 2 f-arglists)
                                      vec)
                                 (take-last 1 f-arglists)))
          (sort-by count))))

(defn last-arg
  "last-arg returns the last element of the longest coll in a coll of colls."
  [arglists]
  (first (reverse (first (reverse (sort-by count arglists))))))

(defn second-to-last-arg
  "second-to-last-arg returns the second to last element of the longest coll
   in a coll of colls."
  [arglists]
  (first (rest (reverse (first (reverse (sort-by count arglists)))))))

(defn second-arglist
  "second-arglist returns the second coll in a coll of colls."
  [arglists]
  (first (rest arglists)))

(defn argtypes->moretypes
  "argtypes->moretypes takes a coll of colls of keywords and changes the
   keywords depending on what it is and the position it is in within
   the coll of colls."
  [arglists]
  (cond (and (empty? (first arglists))
             (contains? type-multi [(second-to-last-arg arglists) (last-arg arglists)]))
           (->> arglists
                last-arg
                (get type-multi-replace)
                (conj [])
                seq
                (conj [])
                seq)
        (and (empty? (first arglists))
             (= (count arglists) 2)
             (= (count (second-arglist arglists)) 1)
             (contains? type-single (vec (second-arglist arglists))))
          (->> arglists
               second-arglist
               vec
               (get type-single)
               (conj [])
               seq
               (conj [])
               seq)
        (and (= (count arglists) 1)
             (= (count (first arglists)) 2)
             (contains? type-multi (vec (first arglists))))
          (->> arglists
               last-arg
               (get type-multi-replace+)
               (conj [])
               seq
               (conj [])
               seq)
        (and (empty? (first arglists))
             (= (count arglists) 2)
             (= (count (second-arglist arglists)) 1)
             (contains? type-multi-replace (last-arg arglists)))
          (->> arglists
               last-arg
               (get type-multi-replace)
               (conj [])
               seq
               (conj [])
               seq)
        (contains? type-multi-replace (last-arg arglists))
          (->> (conj (->> arglists
                          (map seq)
                          seq
                          reverse
                          rest)
                     (->> (conj (->> arglists
                                     (map seq)
                                     seq
                                     reverse
                                     first
                                     reverse
                                     rest)
                                (->> arglists
                                     last-arg
                                     (get type-multi-replace)))
                          reverse))
               reverse)
        (and (empty? (first arglists))
             (>= (count arglists) 2))
           (conj (->> arglists
                      rest
                      rest
                      reverse)
                 (->> (conj (->> arglists
                                 rest
                                 first
                                 rest
                                 reverse)
                            (->> arglists
                                 rest
                                 first
                                 first
                                 (get type-single-no-vec)))
                      reverse))
        :else arglists))

(defn same-type2?
 "same-type2? replaces keywords with corresponding strings in a coll."
 [& args]
	(map #(replace re-type-replace %) args))

(defn chomp-if-necessary
 "chomp-if-necessary checks if a coll of colls is chompable. If it is
  chompable it will go through remove-and and argtypes->moretypes. If it is
  not chompable it will only go through arglists->argtypes."
 [arglists]
	(if (chompable? arglists)
  (argtypes->moretypes (arglists->argtypes (remove-and arglists)))
		(arglists->argtypes arglists)))

(defn spec-length
   "This function takes an argument n and changes it to a corresponding string."
   [n & [arg]]
   (if arg
    (cond
      (not= nil (re-matches #":(\S*)s\*" (str arg))) (case n
                                                       1 "::b-length-zero-or-greater"
                                                       2 "::b-length-greater-zero"
                                                       3 "::b-length-greater-one"
                                                       4 "::b-length-greater-two"
                                                       5 "::b-length-greater-three"
                                                       6 "::b-length-greater-four"
                                                       n)
      (not= nil (re-matches #":(\S*)s+" (str arg))) (case n
                                                       1 "::b-length-greater-zero"
                                                       2 "::b-length-greater-one"
                                                       3 "::b-length-greater-two"
                                                       4 "::b-length-greater-three"
                                                       5 "::b-length-greater-four"
                                                       6 "::b-length-greater-five"
                                                       n)
      :else n)
    (case n
      1 "::b-length-one"
      2 "::b-length-two"
      3 "::b-length-three"
      4 "::b-length-four"
      5 "::b-length-five"
      (cond
        (= n [1 1]) "::b-length-one"
        (= n [2 2]) "::b-length-one"
        (or (= n [2 1]) (= n [1 2])) "::b-length-one-to-two"
        (or (= n [3 1]) (= n [1 3])) "::b-length-one-to-three"
        (or (= n [3 2]) (= n [2 3])) "::b-length-two-to-three"
        (or (= n [4 3]) (= n [3 4])) "::b-length-three-to-four"
        (or (= n [4 1]) (= n [1 4])) "::b-length-one-to-four"
        (or (= n [4 2]) (= n [2 4])) "::b-length-two-to-four"
        (or (= n [5 3]) (= n [3 5])) "::b-length-three-to-five"
        (= n :args ) "::b-length-greater-zero"
        (not= nil (re-matches #":(\S*)s\*" (str n))) "::b-length-greater-zero"
        (not= nil (re-matches #":(\S*)s+" (str n))) "::b-length-greater-one"
        (not= nil (re-matches #":(\S*)\?" (str n))) "::b-length-zero-or-one"
        :else  n))))

(defn args-and-range
  "This function helps keep the length of replace count down
  it sends :args and a minimum and maximum to spec-length."
  [arglist x]
  (if (or (= :args (first (first arglist)))
        (not= nil (re-matches #":(\S*)s(.*)" (str (first (first arglist)))))
        (not= nil (re-matches #":(\S*)\?" (str (first (first arglist))))))
    (spec-length (first (first arglist)))
    (spec-length (vec (conj nil (apply min x) (apply max x))))))

(defn replace-count
  "This function takes a vector, creates a vector with the count of the
  vectors in the vector and outputs the corresponding string."
  [arglist]
  (let [x (map count arglist)]
    (if (and (= 1 (count x))
          (nil? (re-matches #":(\S*)s(.*)" (str (last-arg arglist))))
          (not= :args (first (first arglist)))
          (nil? (re-matches #":(\S*)s(.*)" (str (first (first arglist)))))
          (nil? (re-matches #":(\S*)?" (str (first (first arglist))))))
      (spec-length (first x))
      (if (not= nil (re-matches #":(\S*)s(.*)" (str (last-arg arglist))))
        (spec-length (count (first (reverse arglist))) (last-arg arglist))
        (args-and-range arglist x)))))

(defn pre-re-defn
  "takes a function, applies chomp-if-necessary on its arglist and
   turns it into a string that outputs in a form that can be used
   as a sort of intermediate step."
  [fvar]
  (let [fmeta (meta fvar)]
    (format "(re-defn #' %s/%s %s)\n"
      (:ns fmeta)
      (:name fmeta)
      (->> (:arglists fmeta)
           (map #(map name %))
           (chomp-if-necessary)
           (map vec)
           (interpose "")
           (vec)
           (apply str)))))

(defn pre-re-defn-spec
  "takes a function, applies chomp-if-necessary on its arglist and
   turns it into a string that outputs in the form of a spec."
  [fvar]
  (let [fmeta (meta fvar)]
    (-> (format "(s/fdef %s/%s :args (s/or :a (s/cat %s)))\n(stest/instrument `%s/%s)\n"
          (:ns fmeta)
          (:name fmeta)
          (->> (:arglists fmeta)
               (map #(map name %))
               (chomp-if-necessary)
               (map vec)
               (apply same-type2?)
               (map vec)
               (interpose ") \n  :a (s/cat ")
               (vec)
               (apply str))
          (:ns fmeta)
          (:name fmeta))
        (clojure.string/replace #"\[\"" "")
        (clojure.string/replace #"\"\]" "")
        (clojure.string/replace #"\"" ""))))

(defn pre-re-defn-spec-babel
  "takes a function, applies chomp-if-necessary on its arglist and
   turns it into a string that outputs in the form of a babel spec."
  [fvar]
  (let [fmeta (meta fvar)]
    (-> (format "(s/fdef %s/%s :args (s/and %s (s/or :a (s/cat %s))))\n(stest/instrument `%s/%s)\n"
          (:ns fmeta)
          (:name fmeta)
          (->> (:arglists fmeta)
               (map #(map name %))
               (chomp-if-necessary)
               (map vec)
               (replace-count))
          (->> (:arglists fmeta)
               (map #(map name %))
               (chomp-if-necessary)
               (map vec)
               (apply same-type2?)
               (map vec)
               (interpose ") \n  :a (s/cat ")
               (vec)
               (apply str))
          (:ns fmeta)
          (:name fmeta))
        (clojure.string/replace #"\[\"" "")
        (clojure.string/replace #"\"\]" "")
        (clojure.string/replace #"\"" ""))))

(defn apply-persist
 "apply-persist checks if \"Persistent\" is anywhere in a
  sequence."
 [& vars]
 (map #(clojure.string/includes? % "Persistent") vars))

(defn apply-types
 "apply-persist checks if \"Persistent\" is anywhere in a
  sequence."
 [x vars]
 (map #(= % x) vars))

(defn check-types
  "check-types takes a function and returns true if the function
   has a Persistent in the arglists if apply-persist is passed as x
   or the type corresponds to whatever is passed to x."
  [vars x]
  (let [y (cond->> vars
            true meta
            true :arglists
            (= x "apply-persist") (map #(map type %))
            true (map #(map str %))
            true (map vec)
            true vec)
        z (if (= x "apply-persist")
             (map #(apply-persist %) y)
             (map #(apply-types x %) y))]
           (boolean? (some #(= (seq [true]) %) z))))

(defn println-recur
  "This function shows everything required for a defn, to work with this
  there can be an intermediate step."
  ([all-vars]
   (when
     (not (empty? all-vars))
     (try
       (println (pre-re-defn (first all-vars)))
       (catch java.lang.ClassCastException e))
     (println-recur (rest all-vars))))
   ([all-vars x]
    (loop [rem-args all-vars
           result []]
      (cond
         (empty? rem-args) (println result)
         (check-types (first rem-args) x)
           (recur (rest rem-args)
                  (conj result (str (first rem-args) "\n")))
         :else (recur (rest rem-args) result)))))

(defn println-recur-spec
  "This function generates basic specs for a library."
  [all-vars]
  (when
    (not (empty? all-vars))
    (try
      (println (pre-re-defn-spec (first all-vars)))
      (catch java.lang.ClassCastException e))
    (println-recur-spec (rest all-vars))))

(defn println-recur-spec-babel
  "This function generates specs that include length used in
  babel only."
  [all-vars]
  (when
    (not (empty? all-vars))
    (try
      (println (pre-re-defn-spec-babel (first all-vars)))
      (catch java.lang.ClassCastException e))
    (println-recur-spec-babel (rest all-vars))))

(defn println-recur-vector
  "This function generates specs that include length used in
  babel only."
  [all-vars]
    (loop [rem-args all-vars
           normal "Normal Cases: \n"
           macros "Macros and Special Cases: \n"
           exceptions "Exceptions: \n"]
      (cond
        (empty? rem-args) [normal macros exceptions]
        (check-types (first rem-args) "apply-persist")
          (recur (rest rem-args)
            normal
            macros
            (str exceptions (first rem-args) "\n"))
        :else (if (or ((meta (first rem-args)) :macro)
                      ((meta (first rem-args)) :special-form))
                (recur (rest rem-args)
                  normal
                  (str macros (pre-re-defn (first rem-args)) "\n")
                  exceptions)
                (recur (rest rem-args)
                  (str normal (pre-re-defn (first rem-args)) "\n")
                  macros
                  exceptions)))))

(defn println-recur-spec-babel-vector
  "This function generates specs that include length used in
  babel only and puts them into a file h.txt."
  [all-vars]
    (loop [rem-args all-vars
           normal "Normal Cases: \n"
           macros "Macros and Special Cases: \n"
           exceptions "Exceptions: \n"]
      (cond
        (empty? rem-args) (spit "h.txt" (apply str [normal macros exceptions]))
        (check-types (first rem-args) "apply-persist")
          (recur (rest rem-args)
            normal
            macros
            (str exceptions (first rem-args) "\n"))
        :else (if (or ((meta (first rem-args)) :macro)
                      ((meta (first rem-args)) :special-form))
                (recur (rest rem-args)
                  normal
                  (str macros (pre-re-defn-spec-babel (first rem-args)) "\n")
                  exceptions)
                (recur (rest rem-args)
                  (str normal (pre-re-defn-spec-babel (first rem-args)) "\n")
                  macros
                  exceptions)))))

(defn println-recur-criminals
  "This function shows everything that could not be run in pre-re-defn."
  [all-vars]
  (when
    (not (empty? all-vars))
    (try
      (pre-re-defn (first all-vars))
      (catch java.lang.ClassCastException e
          (println (first all-vars))))
    (println-recur-criminals (rest all-vars))))
