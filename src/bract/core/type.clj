;   Copyright (c) Shantanu Kumar. All rights reserved.
;   The use and distribution terms for this software are covered by the
;   Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
;   which can be found in the file LICENSE at the root of this distribution.
;   By using this software in any fashion, you are agreeing to be bound by
;   the terms of this license.
;   You must not remove this notice, or any other, from this software.


(ns bract.core.type)


(defprotocol IFunction
  (ifunc [this] "Return the function to be invoked")
  (iname [this] "Return the name (or derived name) of the function")
  (iargs [this] "Return the additional arguments to be passed to the function"))


(defrecord Function [func name args]
  IFunction
  (ifunc [this] func)
  (iname [this] name)
  (iargs [this] args))


(defn function?
  [x]
  (satisfies? IFunction x))
