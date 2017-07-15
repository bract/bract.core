;   Copyright (c) Shantanu Kumar. All rights reserved.
;   The use and distribution terms for this software are covered by the
;   Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
;   which can be found in the file LICENSE at the root of this distribution.
;   By using this software in any fashion, you are agreeing to be bound by
;   the terms of this license.
;   You must not remove this notice, or any other, from this software.


(ns bract.core.inducer
  "The inducer fns exposed by Bract-core."
  (:require
    [clojure.java.io :as io]
    [keypin.core     :as keypin]
    [keypin.util     :as kputil]
    [bract.core.echo :as echo]
    [bract.core.impl :as impl]
    [bract.core.kdef :as kdef]
    [bract.core.type :as type]
    [bract.core.util :as util])
  (:import
    [bract.core Echo]))


;; ----- utility for applying inducers -----


(defn apply-inducer
  "Given a context and inducer-spec, apply the inducer to the context (and args if any) returning updated context."
  ([context inducer]
    (apply-inducer "inducer" context inducer))
  ([inducer-type context inducer]
    (let [f (type/ifunc inducer)
          n (type/iname inducer)
          a (type/iargs inducer)]
      (echo/with-latency-capture (format "Executing %s `%s`" inducer-type n)
        (echo/with-inducer-name n
          (apply f context a))))))


(defn induce
  "Given a reducing function `(fn [context inducer-spec]) -> context` and a collection of inducer-specs, roll the seed
  context through each inducer successively, returning updated context. The chain may be broken by an inducer returning
  a reduced context, i.e. `(reduced context)`."
  ([context coll]
    (induce apply-inducer context coll))
  ([f context coll]
    (reduce (fn [context inducer-candidate]
              (if (kdef/ctx-exit? context)
                (reduced context)
                (f context inducer-candidate)))
      context coll)))


;; ----- inducers -----


(defn set-verbosity
  "Set Bract verbosity flag and return context."
  [context]
  (let [pre-verbose?  (Echo/isVerbose)
        post-verbose? (kdef/ctx-verbose? context)]
    (Echo/setVerbose post-verbose?)
    (when (and (not pre-verbose?) post-verbose?)
      (echo/echo
        "Verbose mode enabled - override with env var APP_VERBOSE or system property app.verbose: value true/false")))
  context)


(defn read-context
  "Use context filename (when specified) in the context under key :bract.core/context-file to read from and merge into
  the context."
  [context]
  (if-let [context-file (kdef/ctx-context-file context)]
    (if (io/resource context-file)
      (kdef/resolve-context context context-file)
      (do
        (echo/echof "Context file '%s' not found in classpath" context-file)
        context))
    (do
      (echo/echo "No context file is defined under the key" (key kdef/ctx-context-file))
      context)))


(defn read-config
  "Use config filenames in the context under key :bract.core/config-files to read and resolve config, and populate the
  context with it under the key :bract.core/config."
  [context]
  (let [config-files (kdef/ctx-config-files context)]
    (if (seq config-files)
      (->> config-files
        (kdef/resolve-config context)
        (assoc context (key kdef/ctx-config)))
      context)))


(defn run-context-inducers
  "Run the inducers specified in the context."
  ([context]
    (impl/with-lookup-key (key kdef/ctx-inducers)
      (->> (kdef/ctx-inducers context)
        (induce context))))
  ([context lookup-key]
    (impl/with-lookup-key lookup-key
      (as-> (keypin/make-key lookup-key vector? "Vector of inducer fns or their fully qualified names" {}) <>
        (<> context)
        (induce context <>)))))


(defn run-config-inducers
  "Run the inducers specified in the application config."
  ([context]
    (impl/with-lookup-key (key kdef/cfg-inducers)
      (->> (kdef/ctx-config context)
        kdef/cfg-inducers
        (induce context))))
  ([context lookup-key]
    (impl/with-lookup-key lookup-key
      (->> (kdef/ctx-config context)
        ((keypin/make-key lookup-key vector? "Vector of inducer fns or their fully qualified names" {}))
        (induce context)))))


(defn context-hook
  "Given context with config, invoke the context-hook fn with context as argument."
  [context function]
  (let [f (type/ifunc function)]
    (util/expected fn? (format "%s to be a function" function) f)
    (f context)
    context))


(defn config-hook
  "Given context with config, invoke the config-hook fn with config as argument."
  [context function]
  (let [config (kdef/ctx-config context)
        f (type/ifunc function)]
    (util/expected fn? (format "%s to be a function" function) f)
    (f config)
    context))


(defn export-as-sysprops
  "Given context with config, read the value of config key \"bract.core.exports\" as a vector of string config keys and
  export the key-value pairs for those config keys as system properties."
  [context]
  (let [config (kdef/ctx-config context)
        exlist (-> (kdef/cfg-exports config)
                 (echo/->echo "Exporting as system properties"))]
    (doseq [each exlist]
      (util/expected string? "export property name as string" each)
      (when-not (contains? config each)
        (util/expected (format "export property name '%s' to exist in config" each) config))
      (util/expected string? (format "value for export property name '%s' as string" each) (get config each))
      (System/setProperty each (get config each)))
    context))


(defn unexport-sysprops
  "Given context with config, read the value of config key \"bract.core.exports\" as a vector of string config keys and
  remove them from system properties."
  [context]
  (let [config (kdef/ctx-config context)
        exlist (-> (kdef/cfg-exports config)
                 (echo/->echo "Un-exporting (removing) system properties"))]
    (doseq [each exlist]
      (util/expected string? "export property name as string" each)
      (when-not (contains? config each)
        (util/expected (format "export property name '%s' to exist in config" each) config))
      (util/expected string? (format "value for export property name '%s' as string" each) (get config each))
      (System/clearProperty each))
    context))


(defn invoke-launcher
  "Given context with config, read the value of config key \"bract.core.launcher\" as a fully qualified launcher fn
  name and invoke it as (fn [context]) when the context key :bract.core/launch? has the value true."
  [context]
  (if (kdef/ctx-launch? context)
    (-> (kdef/ctx-config context)
      kdef/cfg-launcher
      (apply [context]))
    (do
      (echo/echo "Launch not enabled, skipping launch.")
      context)))


(defn invoke-deinit
  "Given context with :bract.core/deinit key and corresponding collection of (fn []) de-init fns for the app, invoke
  them in a sequence."
  ([context]
    (invoke-deinit context true))
  ([context ignore-errors?]
    (let [coll (kdef/ctx-deinit context)]
      (if (seq coll)
        (doseq [f coll]
          (try
            (f)
            (catch Exception e
              (echo/echof "Application de-init error (%s): %s"
                (if ignore-errors? "ignored" "not ignored") (util/stack-trace-str e))
              (when-not ignore-errors?
                (throw e)))))
        (echo/echo "Application de-init is not configured, skipping de-initialization.")))
    context))


(defn invoke-stopper
  "Given context with :bract.core/stopper key and corresponding (fn []) stopper fn for the app, invoke it."
  [context]
  (let [f (kdef/ctx-stopper context)]
    (f))
  context)
