(ns babel.processor
 (:require [errors.messageobj :as m-obj]
           [errors.prettify-exception :as p-exc]
           [errors.dictionaries :as d]))

;;an atom that record original error response
(def recorder (atom {:msg [] :detail []}))

(defn reset-recorder
  "This function reset the recorder atom"
  []
  (reset! recorder {:msg [] :detail []}))

(defn update-recorder-msg
  "takes an unfixed error message, and put it into the recorder"
  [inp-message]
  (swap! recorder update-in [:msg] conj inp-message))
  ;(swap! recorder assoc :msg inp-message))

(defn update-recorder-detail
  "takes error message details, and put them into the recorder"
  [inp-message]
  (swap! recorder update-in [:detail] conj inp-message))

;
; (defn get-error
;   [session-number]
;   ((comp #(% #'clojure.core/*e)
;          deref
;          #(get % session-number)
;          deref
;          deref
;         #(get % 'sessions))
;    (ns-interns `nrepl.middleware.session)))

(defn process-message
  "takes a Java Throwable object, and returns the adjusted message as a string."
  [err]
  (let [errmap (Throwable->map err)
        throwvia (:via errmap)
        viacount (count throwvia)
        errclass (str (:type (first throwvia)))
        errdata (:data errmap)]
    (if (and (= "clojure.lang.ExceptionInfo" errclass) (= viacount 1))
        (p-exc/process-spec-errors (str (.getMessage err)) errdata true)
        (if (= "clojure.lang.Compiler$CompilerException" errclass)
          (p-exc/process-macro-errors err errclass (ex-data err))
          (if (and (= "clojure.lang.ExceptionInfo" errclass) (> viacount 1))
            (str
              (->> throwvia
                   reverse
                   first
                   :message
                   (str (:type (first (reverse throwvia))) " ")
                   p-exc/process-errors
                   :msg-info-obj
                   m-obj/get-all-text)
              (p-exc/process-stacktrace err))
            (str
              (->> err
                   .getMessage
                   (str errclass " ")
                   p-exc/process-errors
                   :msg-info-obj
                   m-obj/get-all-text)
              (p-exc/process-stacktrace err)))))))

(def spec-ref {:number "a number", :collection "a sequence", :string "a string", :coll "a collection",
                :map-arg "a two-element-vector", :function "a function", :ratio "a ratio", :future "a future", :key "a key", :map-or-vector "a map-or-vector",
                :regex "a regular expression", :num-non-zero "a number that's not zero", :arg-one "not wrong"})


(defn stringify
  [vector-of-keywords]
  (if (= (count vector-of-keywords) 1) (name (spec-ref (first vector-of-keywords))) (name (spec-ref (second vector-of-keywords)))))

(defn spec-message
  "Takes ex-info data of a spec error, returns a modified message as a string"
  [ex-data]
  (let [{[{:keys [path pred val via in]}] :clojure.spec.alpha/problems fn-full-name :clojure.spec.alpha/fn args-val :clojure.spec.alpha/args} ex-data
        arg-number (first in)
        [print-type print-val] (d/type-and-val val)
        fn-name (d/get-function-name (str fn-full-name))
        function-args-val (apply str (interpose " " (map d/anonymous? (map #(second (d/type-and-val %)) args-val))))
        ]
    (if (re-matches #"corefns\.corefns/b-length(.*)" (str pred))
        (str "wrong length") ; a stub
        (str "The " (d/arg-str arg-number) " of " "("fn-name" "function-args-val")" " was expected to be " (stringify path)
             " but is " print-type print-val " instead.\n"))))

; (defn modify-errors [inp-message]
;   (if (contains? inp-message :err)
;       (assoc inp-message :err
;              (let [err (get-error (:session inp-message))
;                    processed-error (if err (process-message err) "No detail can be found")]
;                   (do
;                     (update-recorder-detail processed-error)
;                     (update-recorder-msg (:session inp-message));(str err))
;                     ;(inp-message :err))))
;                     processed-error)))
;
;       inp-message))

(println "babel.processor loaded")
