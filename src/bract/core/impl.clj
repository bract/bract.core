;   Copyright (c) Shantanu Kumar. All rights reserved.
;   The use and distribution terms for this software are covered by the
;   Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
;   which can be found in the file LICENSE at the root of this distribution.
;   By using this software in any fashion, you are agreeing to be bound by
;   the terms of this license.
;   You must not remove this notice, or any other, from this software.


(ns bract.core.impl
  (:require
    [keypin.util :as kputil]
    [bract.core.echo :as echo]
    [bract.core.type :as type]
    [bract.core.util :as util])
  (:import
    [java.util List Map]
    [clojure.lang AFn Symbol Keyword Var]))


(extend-protocol type/Inducer
  AFn
  (ifunc
    ([this] this)
    ([this config-key] this))
  (iname [this] (str this))
  (iargs [this] [])
  String
  (ifunc
    ([this] (throw (UnsupportedOperationException. (str "Cannot determine inducer without the key: " this))))
    ([this config-key] (do
                         (echo/echo (format "Looking up inducer `%s`" this))
                         (kputil/str->var->deref config-key this))))
  (iname [this] this)
  (iargs [this] [])
  Symbol
  (ifunc
    ([this] (throw (UnsupportedOperationException. (str "Cannot determine inducer without the key: " this))))
    ([this config-key] (do
                         (echo/echo (format "Looking up inducer `%s`" this))
                         (kputil/str->var->deref config-key this))))
  (iname [this] (name this))
  (iargs [this] [] [])
  List
  (ifunc
    ([this] (do
             (util/expected seq "non-empty collection" this)
             (type/ifunc (first this))))
    ([this config-key] (do
                         (util/expected seq "non-empty collection" this)
                         (type/ifunc (first this) config-key))))
  (iname [this] (type/iname (first this)))
  (iargs [this] (vec (rest this)))
  Map
  (ifunc
    ([this] (do
              (util/expected #(contains? % :inducer) "map with :inducer key" this)
              (type/ifunc (get this :inducer))))
    ([this config-key] (do
                         (util/expected #(contains? % :inducer) "map with :inducer key" this)
                         (type/ifunc (get this :inducer) config-key))))
  (iname [this] (or (:name this) (type/iname (get this :inducer))))
  (iargs [this] (vec (get this :args)))
  Var
  (ifunc
    ([this] this)
    ([this config-key] this))
  (iname [this] (str (.-ns ^Var this) \/ (.-sym ^Var this)))
  (iargs [this] []))
