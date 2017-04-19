;   Copyright (c) Shantanu Kumar. All rights reserved.
;   The use and distribution terms for this software are covered by the
;   Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
;   which can be found in the file LICENSE at the root of this distribution.
;   By using this software in any fashion, you are agreeing to be bound by
;   the terms of this license.
;   You must not remove this notice, or any other, from this software.


(ns bract.core.echo
  (:require
    [clojure.string :as string])
  (:import
    [bract.core Echo]))


(def ^:dynamic *inducer-name* nil)


(defmacro with-inducer-name
  "Given an inducer name and body of code, evaluate the body of code in the context of the specified inducer name so
  that it appears in all echo messages."
  [inducer-name & body]
  `(binding [*inducer-name* ~inducer-name]
     ~@body))


;; ----- echo: diagnostics -----


(defn echo
  "Print all message tokens, returning nil."
  [x & more]
  (->> (cons x more)
    (string/join \space)
    (str (when *module-name* (format "[%s] " *module-name*)))
    Echo/echo))


(defn echof
  [fmt & args]
  (-> (apply format (str fmt) args)
    echo))


(defn ->echo
  "Echo message and value in a -> form, returning the value. First arg is considered the value, rest as message tokens."
  [x & more]
  (->> [(pr-str x)]
    (concat (and (seq more) (concat more [\:])))
    (apply echo))
  x)


(defn ->>echo
  "Echo message and value in a ->> form, returning the value. Last arg is considered the value, rest as message tokens."
  [x & more]
  (let [all (concat [x] more)]
    (apply ->echo (last all) (butlast all))))


(defmacro echo-section
  [description & body]
  `(let [description# (str "(Clojure) " ~description)
         section# (Echo/echoSection description#)]
     (.echoBegin section#)
     (try ~@body
       (finally
         (.echoEnd section#)))))


(defn abort
  [message]
  (Echo/abort message))
