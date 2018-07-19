(ns utilities.spec_generator)
   ;(:require [corefns.assert_handling :refer :all]))


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
                ;:args  {:check nil,                   :has-type "arg",      :argument "args", :arg-vec ["&" "args"]}})
                :args  {:check nil,                   :has-type "arg",      :argument "args", :arg-vec ["&" "args"]}})


(def re-type-replace
  "This hashmap used for replacing the keywords with strings"
  '{
    :arg   ":a any?"
    :coll  ":a (s/nilable coll?)"
    :n     ":a number?"
    :colls ":a (s/nilable coll?)"
    :str   ":a string?"
    :strs  ":a string?"
    :f     ":a ifn?"
    :fs    ":a ifn?"
    ;:args  ":a (s/* args)" ;s+ or s*
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
  "This hashmap used for replacing the keywords with strings"
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
  "This hashmap used for checking that certain keys exist"
  '{
    [:coll :colls] :colls
    [:str :strs]  :strs
    [:f :fs]    :fs
    [:arg :args]  :args})

(def type-single
  "This hashmap used for checking that certain keys exist and replacing them"
  '{
    [:arg]   :arg?
    [:coll]  :coll?
    [:n]     :n?
    [:str]   :str?
    [:f]     :f?
    [:r]     :r?
    [nil] :nil?})

(def type-single-no-vec
  "This hashmap used for checking that certain keys exist and replacing them"
  '{
    :arg   :arg?
    :coll  :coll?
    :n     :n?
    :str   :str?
    :f     :f?
    :r     :r?
    nil :nil?})

(def type-multi-replace
  "This hashmap used for replacing keys"
  '{
    :colls :colls*
    :strs  :strs*
    :fs    :fs*
    :args  :args*})

(def type-multi-replace+
  "This hashmap used for replacing keys"
  '{
    :colls :colls+
    :strs  :strs+
    :fs    :fs+
    :args  :args+})

;;mapping of rich hickey's argument names in doc-strings to a more consistent naming scheme
;; (def arg-type (merge
;;                      (zipmap [:coll :c :c1 :c2 :c3 :c4 :c5] (repeat :coll)),
;;                      (zipmap [:n :number :step :start :end :size] (repeat :n)),
;;                      (zipmap [:arg :val :argument :x :y] (repeat :arg)),
;;                      (zipmap [:f :function :pred] (repeat :f)),
;;                      (zipmap [:fs :functions :preds] (repeat :fs)),
;;                      (zipmap [:colls :cs] (repeat :colls)),
;;                      (zipmap [:string :str :s] (repeat :str)),
;;                      (zipmap [:strs :strings :ss] (repeat :strs)),
;;                      (zipmap [:more :args :vals :arguments :xs :ys] (repeat :args))))
(def arg-type (merge ;this changes the various possible arguments into the various corresponding types, like :x into :arg
                     (zipmap [:seq] (repeat :seq)), ; added
                     (zipmap [:map] (repeat :map-or-vector)), ;added
                     (zipmap [:coll :c :c1 :c2 :c3 :c4 :c5 :m :ks] (repeat :coll)),
                     (zipmap [:maps] (repeat :maps-or-vectors)), ;added
                     (zipmap [:n :number :step :start :end :size] (repeat :n)),
                     (zipmap [:r] (repeat :r)),
                     (zipmap [:arg :key :val :argument :x :y :test :not-found] (repeat :arg)), ; key is added
                     ;(zipmap ["(s/cat :a any?)" :key :val :argument :x :y] (repeat :arg)), ; key is added
                     (zipmap [:f :function :pred] (repeat :f)),
                     (zipmap [:fs :functions :preds] (repeat :fs)),
                     (zipmap [:colls :cs] (repeat :colls)),
                     (zipmap [:string :str :s] (repeat :str)),
                     (zipmap [:strs :strings :ss] (repeat :strs)),
                     (zipmap [:more :args :vals :arguments :xs :ys :forms :filters :keyvals] (repeat :args))))

;; returns the index of the last logically false member of the array
;; coll -> number
(defn last-false-index [coll]
  (loop [remaining coll
         i 1
         j 0]
     (if (empty? remaining) j (recur (rest remaining) (inc i) (if (first remaining) j i)))))

;;does the translating from the names rich hickey gave the arguments, to something consistent
;;list of lists of strings -> list of lists of keys
(defn arglists->argtypes [arglists] (map (fn [x] (map #(arg-type (keyword %)) x)) arglists)) ;changes things like :x to :arg


;;returns the longest collection in a collection of collections
;;arglists -> arglist
(defn last-arglist [arglists] (first (reverse (sort-by count arglists)))) ;gets the last collection in the collection

;;returns trus if an arglists has & ____ as an argument
;;arglists -> boolean
;takes the arglist which has been turned into a sequence of sequences of strings and makes sure each
;that the last sequence in the sequence has no arguments which have a count less than 1
;it also finds the name of the second to last argument and checks if it is &
(defn chompable? [arglists] (or (not-any? #(< 1 (count %)) arglists)  (= "&" (name (first (rest (reverse (last-arglist arglists))))))))

;;removes the second to last element of the longest coll in a coll of colls
;;this is the `&` symbol in our case
;;arglists -> arglists
(defn remove-and [arglists]
  (let [sorted-arglists (reverse (sort-by count arglists))
	f-arglists (first sorted-arglists)
	r-arglists (reverse (rest sorted-arglists))]
	(sort-by count (conj r-arglists (into (vec (drop-last 2 f-arglists)) (take-last 1 f-arglists))))))

;;returns the last element of the longest coll in a coll of colls
;;arglists -> keyword
(defn last-arg [arglists] (first (reverse (first (reverse (sort-by count arglists))))))

(defn second-to-last-arg [arglists] (first (rest (reverse (first (reverse (sort-by count arglists)))))))

(defn second-arglist [arglists] (first (rest arglists)))

(defn argtypes->moretypes [arglists] (cond (and (empty? (first arglists)) (contains? type-multi [(second-to-last-arg arglists) (last-arg arglists)])) (do (println "please check to make sure this is right") (seq [(seq [(get type-multi-replace (last-arg arglists))])]))
                                           (and (empty? (first arglists)) (= (count arglists) 2) (= (count (second-arglist arglists)) 1) (contains? type-single (vec (second-arglist arglists)))) (seq [(seq [(get type-single (vec (second-arglist arglists)))])])
                                           (and (= (count arglists) 1) (= (count (first arglists)) 2) (contains? type-multi (vec (first arglists)))) (seq [(seq [(get type-multi-replace+ (last-arg arglists))])])
                                           (and (empty? (first arglists)) (= (count arglists) 2) (= (count (second-arglist arglists)) 1) (contains? type-multi-replace (last-arg arglists))) (seq [(seq [(get type-multi-replace (last-arg arglists))])])
                                           (contains? type-multi-replace (last-arg arglists)) (reverse (conj (rest (reverse (seq (map seq arglists)))) (reverse (conj (rest (reverse (first (reverse (seq (map seq arglists)))))) (get type-multi-replace (last-arg arglists))))))
                                           (and (empty? (first arglists)) (>= (count arglists) 2)) (conj (reverse (rest (rest arglists))) (reverse (conj (reverse (rest (first (rest arglists)))) (get type-single-no-vec (first (first (rest arglists)))))))
                                           :else arglists))

;;checks the :type-data of each argument, returning true if they are all the same
;;args -> boolean
(defn same-type? [& args] ;need to look into why this always returns true but the function doesn't if you run it without the defn
	(apply = (map #(:has-type (type-data %)) args))) ;this returns true for absolutely everything
 ;(apply not= nil (first (map #(:has-type (type-data %)) args))))

(defn same-type2? [& args] ;need to look into why this always returns true but the function doesn't if you run it without the defn
	(map #(replace re-type-replace %) args)) ;this returns true for absolutely everything
 ;(apply not= nil (first (map #(:has-type (type-data %)) args))))

(defn replace-types [& args]
	(map #(replace type-replace %) args))

;;helper function for chomp-arglists, appends last-arg to the end of the longest coll in arglists
;;arglists -> arglists
(defn append-last-arg [arglists last-arg]
	(conj (vec (rest (reverse (sort-by count arglists)))) (conj (vec (first (reverse (sort-by count arglists)))) last-arg)))

;;removes redundant arguments in arglists
;;arglists -> arglists
(defn chomp-arglists [arglists]
    (let [f-arglists (arglists->argtypes (remove-and arglists)) ;removes & and changing the various args to stuff like :arg
	  last-arg (first (reverse (first (reverse (sort-by count f-arglists))))) ;gets the last argument in the last sequence
          ]
      (loop [rem-args f-arglists
             diffs []]
        (cond
          (empty? rem-args) (append-last-arg (vec (filter #(<= (count %) (last-false-index diffs)) f-arglists)) last-arg) ; this is where the problem occurs with args, due to last-false-index.
          :else (recur (drop-while empty? (map rest rem-args)) (into diffs [true]))))));(apply map same-type? (conj rem-args [last-arg]))))))))
          ;:else (recur (drop-while empty? (map rest rem-args)) (into diffs (apply map same-type? (conj rem-args [last-arg]))))))))
          ;reduces all the sequences in the sequence by one (rest) and removes those that are empty
          ;the diffs [true] actually works because its what that function did

;;runs chomp-arglist if the & is present, else just translates to our representation
;;arglists -> arglists
(defn chomp-if-necessary [arglists]
	(if (chompable? arglists) ;checks for & in the arglist essentially
		;(chomp-arglists arglists) ;look at chomp-arglists
  (argtypes->moretypes (arglists->argtypes (remove-and arglists)))
		(arglists->argtypes arglists))) ;changes an arglist to an argtype, like making "x" into :arg or "more" into :args

;the fix for args may be to remove chomp-arglists and improve arglists->argtypes
;in arglists->arg types if we (remove empty? sequence) and use the remove & function
;we can get all the type data. The main problem with this is + and all other arg
;functions will include all of their parts not includeing the empty sequence
;(unless we decide to make empty sequence translate to s/* for spec and [] for intermediate)
;so things like + will look like (defn #'clojure.core/+ [:n] [:n :n] [:n :n :args]) or with the
;empty sequence (defn #'clojure.core/+ [] [:n] [:n :n] [:n :n :args]). This will change how
;we process specs but shouldn't hurt the intermediate step too much.


(defn spec-length
   "This function takes an argument n and changes it to a corresponding string"
   [n & [arg]]
   (if arg
    (cond
      (not= nil (re-matches #":(\S*)s\*" (str arg))) (case n
                                                       1 "::b-length-zero-or-greater"
                                                       2 "::b-length-greater-zero"
                                                       3 "::b-length-greater-one"
                                                       4 "::b-length-greater-two"
                                                       5 "::b-length-greater-three"
                                                       n)
      (not= nil (re-matches #":(\S*)s+" (str arg))) (case n
                                                       1 "::b-length-greater-zero"
                                                       2 "::b-length-greater-one"
                                                       3 "::b-length-greater-two"
                                                       4 "::b-length-greater-three"
                                                       5 "::b-length-greater-four"
                                                       n)
      :else n)
    (case n
      1 "::b-length-one"
      2 "::b-length-two"
      3 "::b-length-three"
      (cond
        (or (= n [2 1]) (= n [1 2])) "::b-length-one-to-two"
        (or (= n [3 1]) (= n [1 3])) "::b-length-one-to-three"
        (or (= n [3 2]) (= n [2 3])) "::b-length-two-to-three"
        (= n :args ) "::b-length-greater-zero"
        (not= nil (re-matches #":(\S*)s\*" (str n))) "::b-length-greater-zero"
        (not= nil (re-matches #":(\S*)s+" (str n))) "::b-length-greater-one"
        (not= nil (re-matches #":(\S*)\?" (str n))) "::b-length-zero-or-one"
        :else  n))))

(defn args-and-range
  "This function helps keep the length of replace count down
  it sends :args and a minimum and maximum to spec-length"
  [arglist x]
  (if (or (= :args (first (first arglist))) (not= nil (re-matches #":(\S*)s(.*)" (str (first (first arglist))))) (not= nil (re-matches #":(\S*)\?" (str (first (first arglist))))))
    (spec-length (first (first arglist)))
    (spec-length (vec (conj nil (apply min x) (apply max x))))))

(defn replace-count
  "This function takes a vector, creates a vector with the count of the
  vectors in the vector and outputs the corresponding string"
  [arglist]
  (let [x (map count arglist)]
    (if (and (= 1 (count x)) (nil? (re-matches #":(\S*)s(.*)" (str (last-arg arglist)))) (not= :args (first (first arglist))) (nil? (re-matches #":(\S*)s(.*)" (str (first (first arglist))))) (nil? (re-matches #":(\S*)?" (str (first (first arglist))))))
      (spec-length (first x))
      (if (not= nil (re-matches #":(\S*)s(.*)" (str (last-arg arglist))))
        (spec-length (count (first (reverse arglist))) (last-arg arglist))
        (args-and-range arglist x)))))

;; outputs a string of generated data for redefining functions with preconditions
;; function -> string
(defn pre-re-defn [fvar]
;;   (println "pre-re-defn ing: " (:name (meta fvar)))
  (let [fmeta (meta fvar)]
    (str "(re-defn #'" (:ns fmeta) "/" (:name fmeta) " "
      (apply str (vec (interpose " " (map vec (chomp-if-necessary (map #(map name %) (:arglists fmeta))))))) ")")))

(defn pre-re-defn-temp-solution [fvar]
;;   (println "pre-re-defn ing: " (:name (meta fvar)))
  (let [fmeta (meta fvar)] ;eventually I would like to make this take place in the code itself
     (clojure.string/replace
       (clojure.string/replace
        (str "(re-defn #'" (:ns fmeta) "/" (:name fmeta) " "
         (apply str (vec (interpose " " (map vec (apply replace-types (map vec (chomp-if-necessary (map #(map name %) (:arglists fmeta))))))))) ")") #"\[\[" "[") #"\]\]" "]")))

(defn pre-re-defn-spec [fvar]
;;   (println "pre-re-defn ing: " (:name (meta fvar)))
  (let [fmeta (meta fvar)]
    (clojure.string/replace
      (clojure.string/replace
        (clojure.string/replace
          (str "(s/fdef " (:ns fmeta) "/" (:name fmeta) " \n  :args " "(s/or :a (s/cat "
            (apply str (vec (interpose ") \n              :a (s/cat " (map vec (apply same-type2? (map vec (chomp-if-necessary (map #(map name %) (:arglists fmeta))))))))) ")))\n"
            "(stest/instrument `" (:ns fmeta) "/" (:name fmeta) ")\n") #"\[\"" "") #"\"\]" "") #"\"" "")))

(defn pre-re-defn-spec-babel [fvar]
  (let [fmeta (meta fvar)]
    (clojure.string/replace
      (clojure.string/replace
        (clojure.string/replace
          (str "(s/fdef " (:ns fmeta) "/" (:name fmeta) " \n  :args (s/and " (replace-count (map vec (chomp-if-necessary (map #(map name %) (:arglists fmeta))))) " (s/or :a (s/cat "
            (apply str (vec (interpose ") \n  :a (s/cat " (map vec (apply same-type2? (map vec (chomp-if-necessary (map #(map name %) (:arglists fmeta))))))))) "))))\n"
            "(stest/instrument `" (:ns fmeta) "/" (:name fmeta) ")\n") #"\[\"" "") #"\"\]" "") #"\"" "")))


(defn println-recur
  "This function shows everything required for a defn, to work with this
  there can be an intermediate step"
  [all-vars]
  (when
    (not (empty? all-vars))
    (try
      (println (pre-re-defn (first all-vars)))
      (catch java.lang.ClassCastException e))
    (println-recur (rest all-vars))))
;; (println-recur (vals (ns-publics 'clojure.core)))

(defn println-recur-temp
  "This function shows everything required for a defn, to work with this
  there can be an intermediate step"
  [all-vars]
  (when
    (not (empty? all-vars))
    (try
      (println (pre-re-defn-temp-solution (first all-vars)))
      (catch java.lang.ClassCastException e))
    (println-recur-temp (rest all-vars))))
;; (println-recur-temp (vals (ns-publics 'clojure.core)))

(defn println-recur-spec
  "This function generates basic specs for a library"
  [all-vars]
  (when
    (not (empty? all-vars))
    (try
      (println (pre-re-defn-spec (first all-vars)))
      (catch java.lang.ClassCastException e))
    (println-recur-spec (rest all-vars))))
;; (println-recur-spec (vals (ns-publics 'clojure.core)))
;; check for :special-form true
;; send to vectors instead of printing

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
;; (println-recur-spec-babel (vals (ns-publics 'clojure.core)))

(defn println-recur-spec-babel-vector
  "This function generates specs that include length used in
  babel only."
  [all-vars]
    (loop [rem-args all-vars
           normal "Normal Cases: "
           macro "Macros and Special Cases: "]
      (cond
        (empty? rem-args) [(str normal) (str macro)]
        :else (recur (rest rem-args) (if-not (or (get (meta (first rem-args)) :macro) (get (meta (first rem-args)) :special-form))
                       (str normal (pre-re-defn-spec-babel (first rem-args)))
                       (str normal))
                     (if (or (get (meta (first rem-args)) :macro) (get (meta (first rem-args)) :special-form))
                       (str macro (pre-re-defn-spec-babel (first rem-args))) ;this fails for some reason
                       (str macro))))))

(defn println-recur-criminals
  "This function shows everything that could not be run in pre-re-defn"
  [all-vars]
  (when
    (not (empty? all-vars))
    (try
      (pre-re-defn (first all-vars))
      (catch java.lang.ClassCastException e
          (println (first all-vars))))
    (println-recur-criminals (rest all-vars))))
;; (println-recur-criminals (vals (ns-publics 'clojure.core)))


;;probably depricated since spec, but below is slightly buggy code to automate the re defining of functions
;;This should be deleted after I am certain it is no longer useful.
;
;;;adds argument counts to the hashmap being passed around, this is a helper function
;;;for the old way of redefining functions
;;;coll -> coll
;(defn add-counts [coll]
;  (let [cnt-key (fn [k coll] (count (filter #(= % k) coll)))]
;    (loop [coll-keys (keys coll)
;           coll-vals (vals coll)
;           out {}]
;      (if (empty? coll-keys)
;          out
;          (recur (rest coll-keys) (rest coll-vals)
;             (assoc out (first coll-keys) (assoc (first coll-vals) :cnt-str (if (= 1 (cnt-key (first coll-keys) (keys coll))) "" (cnt-key (first coll-keys) coll-keys)))))))))
;
;;; string vector vector -> string
;(defn gen-checks [unqualified-name data]
;  (reduce #(str %1 (str unqualified-name "(check-if-" (:type %2) "? \"" unqualified-name "\" " (:argument %2) (:cnt-str %2))) "" data))
;
;;; string vector -> string
;(defn single-re-defn [fname fnamespace arg-types only]
;  (let [f (symbol fname)
;        unqualified-name  (name f)
;        qualified-name (str  fnamespace "/"  unqualified-name)
;        arg-data (add-counts (clj->ourtypes arg-types))
;	do-apply (if (not (empty? (first arg-types))) (re-matches #".*s$" (first (reverse arg-types))) nil)
;        arg-str (apply str (interpose " " (map #(str (:arg-str %) (:cnt-str %)) arg-data)))
;        checks (gen-checks fname arg-data)
;	]
;        ;(println "arg-vec: " arg-vec)
;    (str "  " (if only "" "(") "[" arg-str "]"
;	(if (= (count checks) 0) "" (str "\n    {:pre [" checks "]}"))
;       "\n" (str "           ("(if do-apply "apply " "") qualified-name " [" arg-str "])")
;       (if only "" ")"))))
;;
;;; takes a function name, and a vector of vectors of arguments
;;; note: arguments DO NOT end in a question mark.
;(defn re-defn [fvar & arglists]
;   (str "(defn " (:name (meta fvar)) "\n  \"" (:doc (meta fvar)) "\""
;       (reduce #(str %1 "\n" (single-re-defn (:name (meta fvar)) (:ns (meta fvar)) %2 (= 1 (count arglists)))) "" arglists) ")"))
