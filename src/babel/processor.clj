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

(defn stringify
  [vector-of-keywords]
  (if (= (count vector-of-keywords) 1) (name (first vector-of-keywords)) (name (second vector-of-keywords))))

(defn spec-message
  "Takes ex-info data of a spec error, returns a modified message as a string"
  [ex-data]
  (let [{[{:keys [path val via in]}] :clojure.spec.alpha/problems fn-name :clojure.spec.alpha/fn args :clojure.spec.alpha/args} ex-data
        arg-number (first in)
        [print-type print-val] (d/type-and-val val)] ; note that we convert the value back to a string
        ;; The message below is a stub for now
    (str "In function " fn-name " " print-val " " print-val " the argument " print-val " at position " arg-number " was expected to be " (stringify path)
          " but is " print-type "instead.\n")))

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
