(ns utilities.spec_generator)
   ;(:require [corefns.assert_handling :refer :all]))


;;the only part of this hash map we still use is :has-type
;;the other parts used to be useful when I was also doing the re-defining, and may yet be useful
(def type-data {
                :arg   {:check nil,                   :has-type "arg",      :argument "arg",  :arg-vec ["arg"]},
                :coll  {:check "check-if-seqable?",   :has-type "seqable",  :argument "coll", :arg-vec ["coll"]},
                :n     {:check "check-if-number?",    :has-type "number",   :argument "n",    :arg-vec ["n"]},
                :colls {:check "check-if-seqables?",  :has-type "seqable",  :argument "args", :arg-vec ["&" "args"]},
                :str   {:check "check-if-string?",    :has-type "string",   :argument "str",  :arg-vec ["str"]},
                :strs  {:check "check-if-strings?",   :has-type "string",   :argument "strs", :arg-vec ["&" "strs"]},
                :f     {:check "check-if-function?",  :has-type "function", :argument "f",    :arg-vec ["f"]},
                :fs    {:check "check-if-functions?", :has-type "function", :argument "fs",   :arg-vec ["&" "fs"]}
                ;:args  {:check nil,                   :has-type "arg",      :argument "args", :arg-vec ["&" "args"]}})
                :args  {:check nil,                   :has-type "arg",      :argument "args", :arg-vec ["&" "args"]}})

;;plans to use this for the output
(def re-type-data {
                :arg   {:has-type ":a arg"},
                :coll  {:has-type ":a (s/nilable coll?)"},
                :n     {:has-type ":a number?"},
                :colls {:has-type ":a (s/nilable coll?)"},
                :str   {:has-type ":a string?"},
                :strs  {:has-type ":a string?"},
                :f     {:has-type ":a ifn?"},
                :fs    {:has-type ":a ifn?"},
                :args  {:has-type ":a (s/* args)"},
                nil {:has-type ":a something"}})

(def re-type-replace '{
                :arg   ":a arg"
                :coll  ":a (s/nilable coll?)"
                :n     ":a number?"
                :colls ":a (s/nilable coll?)"
                :str   ":a string?"
                :strs  ":a string?"
                :f     ":a ifn?"
                :fs    ":a ifn?"
                :args  ":a (s/* args)"
                nil ":a something"})

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
                     (zipmap [:coll :c :c1 :c2 :c3 :c4 :c5] (repeat :coll)),
                     (zipmap [:maps] (repeat :maps-or-vectors)), ;added
                     (zipmap [:n :number :step :start :end :size] (repeat :n)),
                     (zipmap [:arg :key :val :argument :x :y :test] (repeat :arg)), ; key is added
                     ;(zipmap ["(s/cat :a any?)" :key :val :argument :x :y] (repeat :arg)), ; key is added
                     (zipmap [:f :function :pred] (repeat :f)),
                     (zipmap [:fs :functions :preds] (repeat :fs)),
                     (zipmap [:colls :cs] (repeat :colls)),
                     (zipmap [:string :str :s] (repeat :str)),
                     (zipmap [:strs :strings :ss] (repeat :strs)),
                     (zipmap [:more :args :vals :arguments :xs :ys] (repeat :args))))

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
(defn last-arg [arglists] (keyword (str (name (first (reverse (first (reverse (sort-by count arglists)))))))))

;;checks the :type-data of each argument, returning true if they are all the same
;;args -> boolean
(defn same-type? [& args] ;need to look into why this always returns true but the function doesn't if you run it without the defn
	(apply = (map #(:has-type (type-data %)) args))) ;this returns true for absolutely everything
 ;(apply not= nil (first (map #(:has-type (type-data %)) args))))

(defn same-type2? [& args] ;need to look into why this always returns true but the function doesn't if you run it without the defn
	(map #(replace re-type-replace %) args)) ;this returns true for absolutely everything
 ;(apply not= nil (first (map #(:has-type (type-data %)) args))))

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
          (empty? rem-args) (append-last-arg (vec (filter #(<= (count %) (last-false-index diffs)) f-arglists)) last-arg)
          :else (recur (drop-while empty? (map rest rem-args)) (into diffs [true]))))));(apply map same-type? (conj rem-args [last-arg]))))))))
          ;:else (recur (drop-while empty? (map rest rem-args)) (into diffs (apply map same-type? (conj rem-args [last-arg]))))))))
          ;reduces all the sequences in the sequence by one (rest) and removes those that are empty
          ;the diffs [true] actually works because its what that function did

;;runs chomp-arglist if the & is present, else just translates to our representation
;;arglists -> arglists
(defn chomp-if-necessary [arglists]
	(if (chompable? arglists) ;checks for & in the arglist essentially
		(chomp-arglists arglists) ;look at chomp-arglists
		(arglists->argtypes arglists))) ;changes an arglist to an argtype, like making "x" into :arg or "more" into :args

;; outputs a string of generated data for redefining functions with preconditions
;; function -> string
#_(defn pre-re-defn [fvar]
;;   (println "pre-re-defn ing: " (:name (meta fvar)))
  (let [fmeta (meta fvar)]
    (str "(re-defn #'" (:ns fmeta) "/" (:name fmeta) " "
      (apply str (vec (interpose " " (map vec (chomp-if-necessary (map #(map name %) (:arglists fmeta))))))) ")")))

#_(defn pre-re-defn [fvar]
;;   (println "pre-re-defn ing: " (:name (meta fvar)))
  (let [fmeta (meta fvar)]
    (str "(s/fdef " (:ns fmeta) "/" (:name fmeta) " :args (s/or :a (s/cat "
      ;" " between each variable(s)
      (apply str (vec (interpose ") :a (s/cat " (map vec (chomp-if-necessary (map #(map name %) (:arglists fmeta))))))) ")))")))

(defn pre-re-defn [fvar]
;;   (println "pre-re-defn ing: " (:name (meta fvar)))
  (let [fmeta (meta fvar)]
    (clojure.string/replace
      (clojure.string/replace
        (clojure.string/replace
          (str "(s/fdef " (:ns fmeta) "/" (:name fmeta) " \n  :args (s/or :a (s/cat "
            (apply str (vec (interpose ") \n              :a (s/cat " (map vec (apply same-type2? (map vec (chomp-if-necessary (map #(map name %) (:arglists fmeta))))))))) ")))\n"
            "(stest/instrument `" (:ns fmeta) "/" (:name fmeta) ")\n") #"\[\"" "") #"\"\]" "") #"\"" "")))


(defn println-recur [all-vars]
  (when
    (not (empty? all-vars))
    (try
      (println (pre-re-defn (first all-vars)))
      (catch java.lang.ClassCastException e))
    (println-recur (rest all-vars))))
;; (println-recur (vals (ns-publics 'clojure.core)))

(defn println-recur-criminals [all-vars]
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
