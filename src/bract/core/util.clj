;   Copyright (c) Shantanu Kumar. All rights reserved.
;   The use and distribution terms for this software are covered by the
;   Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
;   which can be found in the file LICENSE at the root of this distribution.
;   By using this software in any fashion, you are agreeing to be bound by
;   the terms of this license.
;   You must not remove this notice, or any other, from this software.


(ns bract.core.util
  "Standalone utility functions."
  (:require
    [clojure.string :as string])
  (:import
    [java.util UUID]))


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


(defn shorten-name
  "Shorten a stringable name, e.g. foo.bar.baz.qux/fred to f.b.baz.qux/fred, leaving only the last two tokens intact."
  ([any-name]
    (shorten-name #"\." any-name))
  ([split-regex any-name]
    (let [tokens (string/split (str any-name) split-regex)
          tcount (count tokens)]
      (string/join "."
        (if (> tcount 2)
          (concat (as-> (- tcount 2) $
                    (take $ tokens)
                    (map first $))
            (take-last 2 tokens))
          tokens)))))


(defmacro exec-once!
  "Given a redefinable var e.g. (defonce a-var nil) having logical false value, set it to `true` and evaluate the body."
  [a-var & body]
  `(let [var# ~a-var
         old# (volatile! true)]
     (expected var? "a redefinable var e.g. (defonce a-var nil)" var#)
     (alter-var-root var# (fn [old-val#] (or old-val# (do (vreset! old# old-val#) true))))
     (when-not @old#
       ~@body)))


(defn as-vec
  "Turn argument into a vector if it is a collection, else wrap the argument into a single-item vector."
  [x]
  (if (coll? x)
    (vec x)
    [x]))


(defn uuid-str
  "Return a random UUID string."
  ^String
  []
  (.toString (UUID/randomUUID)))
