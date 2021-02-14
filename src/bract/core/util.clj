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
    [java.io FileNotFoundException PrintStream PrintWriter StringWriter]
    [java.util UUID]
    [java.util.concurrent ThreadLocalRandom]
    [clojure.lang IDeref IFn Named]))


(defn expected
  "Throw illegal input exception citing _expectation_ and what was _found_ did not match. Optionally accept a predicate
  fn to test _found_ before throwing the exception."
  ([expectation found]
    (throw (IllegalArgumentException.
             (format "Expected %s, but found (%s) %s" expectation (class found) (pr-str found)))))
  ([pred expectation found]
    (when-not (pred found)
      (expected expectation found))))


(defn err-println
  "Same as `clojure.core/println` for `*err*`."
  [x & more]
  (binding [*out* *err*]
    (apply println x more)))


(defn err-print-banner
  "Print a banner to `*err*`."
  [x & more]
  (err-println "\n**********")
  (err-println "**")
  (apply err-println "**" x more)
  (err-println "**")
  (err-println "**"))


(defn shorten-name
  "Shorten a stringable name, e.g. `foo.bar.baz.qux/fred` to `f.b.baz.qux/fred`, leaving only the last two tokens
  intact."
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
  "Given a redefinable var e.g. `(defonce a-var nil)` having logical false value, set it to `true` and evaluate the
  body."
  [a-var & body]
  `(let [var# ~a-var
         old# (volatile! true)]
     (expected var? "a redefinable var e.g. (defonce a-var nil)" var#)
     (alter-var-root var# (fn [old-val#] (or old-val# (do (vreset! old# old-val#) true))))
     (when-not @old#
       ~@body)))


(defn clean-uuid
  "Generate or convert UUID into a sanitized, lower-case form."
  (^String []
   (clean-uuid (.toString (java.util.UUID/randomUUID))))
  (^String [^String uuid]
   (if (nil? uuid)
     nil
     (let [n (.length uuid)
           ^StringBuilder b (StringBuilder. n)]
       (loop [i 0]
         (if (>= i n)
           (.toString b)
           (let [c (.charAt uuid i)]
             (when (Character/isLetterOrDigit c) ; ignore non-letter and non-numeric
               ;; make lower-case before adding
               (.append b (Character/toLowerCase c)))
             (recur (unchecked-inc i)))))))))


(defn stack-trace-str
  "Given a throwable (generally an exception) return the stack trace string as it would be printed on a console."
  ^String
  [^Throwable e]
  (let [^StringWriter sw (StringWriter.)]
    (.printStackTrace e (PrintWriter. sw))
    (.toString sw)))


(defn now-millis
  "Return current epochal time in milliseconds."
  (^long []
    (System/currentTimeMillis))
  (^long [^long start-millis]
    (unchecked-subtract (System/currentTimeMillis) start-millis)))


(defn sleep-millis
  "Sleep for specified number of milliseconds."
  [^long millis]
  (try
    (Thread/sleep millis)
    (catch InterruptedException e
      (.interrupt (Thread/currentThread)))))


(defn alive-millis
  "Return a function `(fn [])` that when invoked, records the current time as when was the application last alive. You
  may deref the same function as `(deref f)` to find out the time the application was last alive. Optimized for
  concurrent updates; eventually consistent."
  ([]
    (alive-millis (.availableProcessors ^Runtime (Runtime/getRuntime))))
  ([^long n]
    (let [trackers (-> n
                     (repeatedly #(volatile! 0))  ; initialize with all zeros
                     vec)]
      (reify
        IFn    (invoke [_] (let [idx (.nextLong (ThreadLocalRandom/current) n)]
                             (vreset! (get trackers idx) (now-millis))))
        IDeref (deref  [_] (->> trackers
                             (map deref)
                             (apply max)))))))


(defn health-status
  "Given a collection of health status maps (with keys `:status` and `:impact`) of zero or more components, derive
  overall health status and return status map `{:status status :components components}` based on the following rules:

  0. Health status `:critical` > `:degraded` > `:healthy` (high to low)
  1. Higher old-status always overrides lower new-status.
  2. Same old-status and new-status are considered unchanged.
  3. A higher new-status is interpreted as follows:

     |Old status|New status|Impact :direct|Impact :indirect|Impact :noimpact|
     |----------|----------|--------------|----------------|----------------|
     | degraded | critical |   critical   |    degraded    |    degraded    |
     | healthy  | critical |   critical   |    degraded    |    healthy     |
     | healthy  | degraded |   degraded   |    degraded    |    healthy     |

  Example of returned status:

  ```edn
  {:status :degraded  ; derived from components - :critical, :degraded, :healthy (default), :unknown
   :components [{:id     :mysql
                 :status :degraded
                 :impact :hard    ; impact on overall health - :hard (default), :soft, :none/nil/false
                 :breaker :half-open
                 :retry-in \"14000ms\"}
                {:id     :cache
                 :status :critical
                 :impact :soft}
                {:id     :disk
                 :status :healthy
                 :impact :none
                 :free-gb 39.42}]}
  ```"
  [components]
  (let [critical  2
        degraded  1
        healthy   0
        n->status [:healthy :degraded :critical]
        status->n {:healthy  0
                   :degraded 1
                   :critical 2}
        status-up (fn ^long [^long old-status ^long new-status impact]
                    (if (>= old-status new-status)
                      old-status
                      (case impact
                        nil   old-status  ; falsey is the same as :none
                        false old-status  ; falsey is the same as :none
                        :none old-status
                        :soft degraded
                        new-status)))]
    {:status (->> components
               (reduce (fn [^long old-status {:keys [status impact] :as health}]
                         (if-let [^long new-status (status->n status)]
                           (status-up old-status new-status impact)
                           old-status))
                 healthy)
               n->status)
     :components components}))


(defn set-default-uncaught-exception-handler
  "Set specified function `(fn [thread throwable])` as default uncaught exception handler."
  [f]
  (cond
    (nil? f) (Thread/setDefaultUncaughtExceptionHandler nil)
    (fn? f)  (Thread/setDefaultUncaughtExceptionHandler
               (reify Thread$UncaughtExceptionHandler
                 (uncaughtException [_ thread ex] (f thread ex))))
    :else    (expected "function or nil" f)))


(defn pst-when-uncaught-handler
  "When uncaught exception handler is configured, print the stack trace."
  ([^Throwable e]
    (when (or (Thread/getDefaultUncaughtExceptionHandler)
            (.getUncaughtExceptionHandler ^Thread (Thread/currentThread)))
      (.printStackTrace e)))
  ([^Throwable e out]
    (when (or (Thread/getDefaultUncaughtExceptionHandler)
            (.getUncaughtExceptionHandler ^Thread (Thread/currentThread)))
      (cond
        (instance? PrintStream out) (.printStackTrace e ^PrintStream out)
        (instance? PrintWriter out) (.printStackTrace e ^PrintWriter out)
        :otherwise (do
                     (err-println "Invalid argument: expected java.io.PrintStream or java.io.PrintWriter but found"
                       (pr-str (class out)) out)
                     (expected "java.io.PrintStream or java.io.PrintWriter instance" out))))))


(defn invoke
  "Invoke first argument as a function on the remaining arguments."
  [f & args]
  (apply f args))


;; conversion


(defn as-vec
  "Turn argument into a vector if it is a collection, else wrap the argument into a single-item vector."
  [x]
  (if (coll? x)
    (vec x)
    [x]))


(defn as-str
  "Turn anything into string."
  [x]
  (cond
    (instance? Named x) (let [right (name x)]
                          (if-let [left (namespace x)]
                            (str left \/ right)
                            right))
    (string? x)         x
    :otherwise          (str x)))


(let [;; conversion constants
        ms-to-s 1000
        ms-to-m (* 1000 60)
        ms-to-h (* 1000 60 60)
        ms-to-d (* 1000 60 60 24)]
  (defn millis->str
    "Convert milliseconds to human readable string."
    [^long millis]
    (cond
      (> millis ms-to-d) (str (quot millis ms-to-d) "d " (millis->str (rem millis ms-to-d)))
      (> millis ms-to-h) (str (quot millis ms-to-h) "h " (millis->str (rem millis ms-to-h)))
      (> millis ms-to-m) (str (quot millis ms-to-m) "m " (millis->str (rem millis ms-to-m)))
      (> millis ms-to-s) (str (quot millis ms-to-s) "s " (millis->str (rem millis ms-to-s)))
      :otherwise         (str millis "ms"))))


(let [;; conversion constants
        b-to-kb 1024
        b-to-mb (* 1024 1024)
        b-to-gb (* 1024 1024 1024)]
  (defn nbytes->str
    "Convert bytes-count to human readable string."
    [^long n]
    (cond
      (> n b-to-gb) (format "%.2f GBytes" (double (/ n b-to-gb)))
      (> n b-to-mb) (format "%.2f MBytes" (double (/ n b-to-mb)))
      (> n b-to-kb) (format "%.2f KBytes" (double (/ n b-to-kb)))
      :otherwise (str n " Bytes"))))


(defmacro let-var
  "Given a binding vector where right-hand side is fully-qualified var name symbol and body of code, resolve the vars
  and evaluate the body of code in the binding context."
  [bindings & body]
  (expected vector? "a binding vector" bindings)
  (expected (comp even? count) "even number of binding forms" bindings)
  (if (empty? bindings)
    `(do ~@body)
    (let [[left right] bindings]
      `(let [right# (symbol ~right)
             ns# (symbol (namespace right#))]
         (try (require ns#)
           (catch FileNotFoundException e#
             (throw (ex-info (format "Error loading namespace %s when resolving var %s" ns# right#) {}))))
         (if-let [~left (find-var right#)]
           (let-var [~@(drop 2 bindings)]
             ~@body)
           (throw (ex-info (format "Cannot find fn '%s' in classpath." right#) {})))))))


(defn nop
  "Do nothing, return `nil`."
  [& args])


(defmacro thrown->val
  "Execute `body` of code in a `try` block, returning `value` when an exception of type `klass` is caught.
  There may be one of more klass/value pairs, laid out as `catch` expressions in the same order as specified."
  [[klass value & more] & body]
  (let [catches (->> (partition 2 more)
                  (cons [klass value])
                  (reduce (fn [exprs [k v]]
                            (conj exprs `(catch ~k ex#
                                           ~v)))
                    []))]
    `(try
       ~@body
       ~@catches)))


(defmacro after
  "Create a function `(fn [arg]) -> arg` that returns the supplied argument after evaluating body of code."
  [& body]
  `(fn [result#]
     ~@body
     result#))


(defmacro doafter
  "Evaluate supplied expression and return it after evaluating body of code."
  [expr & body]
  `(let [result# ~expr]
     ~@body
     result#))
