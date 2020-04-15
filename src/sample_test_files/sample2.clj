(ns sample-test-files.sample2
   (:require [corefns.corefns]))

;; #####################################################
;; ###########n Tests for stacktraces ##################
;; #### DO NOT MOVE: it would change line numbers ######
;; #####################################################

(defn f [x y] (map x y))

(defn g [x y] (if (empty? x) (f x y) (f y x)))
