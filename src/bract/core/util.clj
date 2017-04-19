;   Copyright (c) Shantanu Kumar. All rights reserved.
;   The use and distribution terms for this software are covered by the
;   Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
;   which can be found in the file LICENSE at the root of this distribution.
;   By using this software in any fashion, you are agreeing to be bound by
;   the terms of this license.
;   You must not remove this notice, or any other, from this software.


(ns bract.core.util)


(defn expected
  "Throw illegal input exception citing `expectation` and what was `found` did not match. Optionally accept a predicate
  fn to test `found` before throwing the exception."
  ([expectation found]
    (throw (IllegalArgumentException.
             (format "Expected %s, but found (%s) %s" expectation (class found) (pr-str found)))))
  ([pred expectation found]
    (when-not (pred found)
      (expected expectation found))))


(defn err-println
  "Same as clojure.core/println for *err*."
  [x & more]
  (binding [*out* *err*]
    (apply println x more)))


(defn apply-inducer
  "Given a context and an inducer function `(fn [context]) -> context`, apply the inducer to the context returning
  an updated context."
  [context inducer]
  (inducer context))


(defn induce
  "Given a seed context (optional, {} by default), a reducing function `(fn [context inducer-candidate]) -> context`
  (optional, `bract.core.util/apply-inducer` by default) and a collection of inducer candidates, roll the seed context
  through each inducer function successively, returning an updated context."
  ([coll]
    (induce {} apply-inducer coll))
  ([seed coll]
    (induce seed apply-inducer coll))
  ([seed f coll]
    (reduce (fn [context inducer-candidate] (f context inducer-candidate))
      seed coll)))
