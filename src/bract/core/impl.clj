;   Copyright (c) Shantanu Kumar. All rights reserved.
;   The use and distribution terms for this software are covered by the
;   Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
;   which can be found in the file LICENSE at the root of this distribution.
;   By using this software in any fashion, you are agreeing to be bound by
;   the terms of this license.
;   You must not remove this notice, or any other, from this software.


(ns bract.core.impl
  (:require
    [clojure.main :refer [demunge]]
    [clojure.pprint  :as pp]
    [clojure.set     :as set]
    [clojure.string  :as string]
    [keypin.util     :as kputil]
    [bract.core.echo :as echo]
    [bract.core.type :as type]
    [bract.core.util :as util])
  (:import
    [java.util List Map]
    [clojure.lang Fn Symbol Keyword Var]))


(def ^:dynamic *lookup-key* "no-key")


(defmacro with-lookup-key
  [lookup-key & body]
  `(binding [*lookup-key* ~lookup-key]
     (echo/echo "Looking up inducer-list at key" ~lookup-key)
     ~@body))


(extend-protocol type/IFunction
  Fn
  (ifunc [this] this)
  (iname [this] (let [fname (str this)]
                  (if-some [tokens (re-matches #"([A-Za-z\.\_]+\$[A-Za-z\.\_]+)@[0-9a-f]+" fname)]
                    (demunge (last tokens))
                    fname)))
  (iargs [this] [])
  String
  (ifunc [this] (do
                  (echo/echo (format "Looking up inducer `%s`" this))
                  (kputil/str->var->deref *lookup-key* this)))
  (iname [this] this)
  (iargs [this] [])
  Symbol
  (ifunc [this] (do
                  (echo/echo (format "Looking up inducer `%s`" this))
                  (kputil/str->var->deref *lookup-key* this)))
  (iname [this] (name this))
  (iargs [this] [] [])
  List
  (ifunc [this] (do
                  (util/expected seq "non-empty collection" this)
                  (type/ifunc (first this))))
  (iname [this] (type/iname (first this)))
  (iargs [this] (vec (rest this)))
  Map
  (ifunc [this] (do
                  (util/expected #(contains? % :inducer) "map with :inducer key" this)
                  (type/ifunc (get this :inducer))))
  (iname [this] (or (:name this) (type/iname (get this :inducer))))
  (iargs [this] (vec (get this :args)))
  Var
  (ifunc [this] this)
  (iname [this] (str (.-ns ^Var this) \/ (.-sym ^Var this)))
  (iargs [this] []))


;; ----- induction reporting -----


(defrecord InducerLog [inducer-level
                       inducer-type
                       invocation
                       keys-added
                       keys-removed
                       keys-updated
                       millis-taken
                       thrown])


(defn stringify
  [v]
  (let [scalar? (some-fn
                  nil?
                  number?
                  kputil/bool?
                  char?
                  string?
                  keyword?
                  symbol?)]
    (cond
      (scalar? v)  (pr-str  v)
      (coll? v)    (let [n (count v)] (format (cond
                                                (map? v)    "{%s}"
                                                (vector? v) "[%s]"
                                                (set? v)    "#{%s}"
                                                :else       "(%s)") (if (zero? n) "" n)))
      (fn? v)      "fn"
      :otherwise   (format "#%s" (.getName ^Class (class v))))))


(defn maxlen
  [v len]
  (let [s (str v)]
    (if (> (count s) len)
      (-> (subs s 0 len)
        (str "+"))
      s)))


(defn map->pairs
  [m]
  (let [ks (keys m)]
    (->> ks
      (map m)
      (map (fn [k v] (format "%s=%s" k (-> (stringify v)
                                         (maxlen 10))))
        ks)
      vec)))


(defn induction-init [level context]
  (->InducerLog
    level
    "initial"
    "--initial-context--"
    (vec (map->pairs context))
    nil
    nil
    0
    nil))


(defn inducer-success [level inducer-type invocation old-context new-context millis-taken]
  (let [old-keyset   (set (keys old-context))
        new-keyset   (set (keys new-context))
        report-vec   (comp vec sort)
        keys-added   (->> (set/difference new-keyset old-keyset)
                       (select-keys new-context)
                       map->pairs
                       report-vec)
        keys-removed (->> (set/difference old-keyset new-keyset)
                       (select-keys old-context)
                       map->pairs
                       report-vec)
        keys-updated (->> (set/intersection old-keyset new-keyset)
                       (filter (fn [k]
                                 (not= (get old-context k) (get new-context k))))
                       (select-keys new-context)
                       map->pairs
                       report-vec)]
    (->InducerLog
      level
      inducer-type
      invocation
      keys-added
      keys-removed
      keys-updated
      millis-taken
      nil)))


(defn inducer-failure [level inducer-type invocation millis-taken thrown]
  (->InducerLog
    level
    inducer-type
    invocation
    nil
    nil
    nil
    millis-taken
    thrown))


(defn row->columnar
  [{:keys [inducer-level
           inducer-type
           invocation
           keys-added
           keys-removed
           keys-updated
           millis-taken
           thrown]
    :as row}]
  (let [vmax (volatile! 0)
        umax (fn [tokens] (vswap! vmax (fn [^long n] (max n (count tokens)))))
        cols (fn [each] (-> (str each)
                          (string/split #"\s")
                          (doto umax)))
        dols (fn [each] (doto each umax))
        cols-inducer-level (cols inducer-level)
        cols-inducer-type  (cols inducer-type)
        cols-invocation    (cols invocation)
        cols-keys-added    (dols keys-added)
        cols-keys-removed  (dols keys-removed)
        cols-keys-updated  (dols keys-updated)
        cols-millis-taken  (cols millis-taken)
        cols-thrown        (cols thrown)
        max-width (fn [tokens] (reduce (fn [^long n each]
                                         (max n (count each)))
                                 0
                                 tokens))
        space-str (fn [n] (->> \space
                            (repeat n)
                            (apply str)))]
    (for [i (range @vmax)]
      (let [iget (fn [tokens] (get tokens i ""))
            ipad (fn [tokens] (let [col-width (max-width tokens)]
                                (-> (iget tokens)
                                  (str (space-str col-width))
                                  (subs 0 col-width))))]
        {:level        (iget cols-inducer-level)
         :inducer-type (iget cols-inducer-type)
         :invocation   (ipad cols-invocation)
         :keys-added   (ipad cols-keys-added)
         :keys-removed (ipad cols-keys-removed)
         :keys-updated (ipad cols-keys-updated)
         :millis       (iget cols-millis-taken)
         :thrown       (iget cols-thrown)}))))


(defn make-report
  [inducer-logs]
  (->> inducer-logs
    (mapcat row->columnar)
    (pp/print-table [:level
                     :inducer-type
                     :invocation
                     :keys-added
                     :keys-removed
                     :keys-updated
                     :millis
                     :thrown])
    with-out-str))
