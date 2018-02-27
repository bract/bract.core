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
    [clojure.edn       :as edn]
    [clojure.java.io   :as io]
    [keypin.core       :as keypin]
    [keypin.type       :as kptype]
    [keypin.util       :as kputil]
    [bract.core.echo   :as echo]
    [bract.core.impl   :as impl]
    [bract.core.keydef :as kdef]
    [bract.core.type   :as type]
    [bract.core.util   :as util])
  (:import
    [java.net InetAddress UnknownHostException]
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
    (let [launcher (-> (kdef/ctx-config context)
                     kdef/cfg-launcher)]
      (echo/echo "Launcher name:" launcher)
      (launcher context))
    (do
      (echo/echo "Launch not enabled, skipping launch.")
      context)))


(defn invoke-deinit
  "Given context with :bract.core/deinit key and corresponding collection of (fn []) de-init fns for the app, invoke
  them in a sequence. Return context with empty deinit vector."
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
    (assoc context (key kdef/ctx-deinit) [])))


(defn invoke-stopper
  "Given context with :bract.core/stopper key and corresponding (fn []) stopper fn for the app, invoke it."
  [context]
  (let [f (kdef/ctx-stopper context)]
    (f))
  context)


(defn add-shutdown-hook
  "Given context with :bract.core/*shutdown-flag and :bract.core/shutdown-hooks keys related to app shutdown, and
  config key \"bract.core.drain.timeout\", add an inducer as a shutdown hook. Specified inducer (invoke-deinit by
  default) may be a function or a fully-qualified function name."
  ([context]
    (add-shutdown-hook context invoke-deinit))
  ([context inducer]
    (let [flag    (kdef/*ctx-shutdown-flag context)  ; volatile of boolean
          timeout (-> (kdef/ctx-config context)
                    kdef/cfg-drain-timeout
                    kptype/millis)                   ; timeout in millis
          thread  (Thread. (fn []
                             (echo/echo "JVM received a TERMINATE request, reached shutdown-hook")
                             ;; set the flag
                             (when flag
                               (vswap! flag (fn [fval]
                                              (echo/echo (if fval
                                                           "Shutdown flag is already set to true, leaving as is"
                                                           "Shutdown flag was false, now set to true"))
                                              true)))
                             ;; wait for timeout
                             (let [last-alive-millis (long @(kdef/ctx-alive-tstamp context))
                                   until-time-millis (if (pos? last-alive-millis)
                                                       (unchecked-add last-alive-millis ^long timeout)
                                                       (unchecked-add (util/now-millis) ^long timeout))]
                               (while (< (util/now-millis) until-time-millis)
                                 (let [nap-millis (unchecked-subtract until-time-millis (util/now-millis))]
                                   (when (pos? nap-millis)
                                     (echo/echof "Waiting for current workload to drain, time remaing: %d ms"
                                       nap-millis)
                                     (util/sleep-millis (min 500 nap-millis))))))
                             ;; invoke shutdown-hook inducer
                             (echo/echo "Workload draining timed out, executing shutdown-hook inducer now")
                             (apply-inducer context inducer)))]
      (.addShutdownHook ^Runtime (Runtime/getRuntime) thread)
      (update context (key kdef/ctx-shutdown-hooks) conj thread))))


(defn set-default-exception-handler
  "Set specified function (STDERR printer by default) as the default uncaught-exception handler for all JVM threads."
  ([context]
    (set-default-exception-handler
      context (fn [^Thread thread ^Throwable ex]
                (util/err-println
                  (format "Uncaught exception in thread ID: %d, thread name: %s - %s"
                    (.getId thread) (.getName thread) (util/stack-trace-str ex))))))
  ([context exception-handler]
    (-> (type/ifunc exception-handler)
      util/set-default-uncaught-exception-handler)
    context))


;; ----- inducers that inject config -----


(defn discover-hostname
  "Discover hostname and add to config if absent.
  Options:
  :config-key - configuration key to update discovered hostname at, default: \"discovered.hostname\""
  ([context]
    (discover-hostname context {}))
  ([context {:keys [config-key]
             :or {config-key "discovered.hostname"}}]
    (kdef/discover-config context config-key
      (fn [key-path]
        (try
          (let [^InetAddress localhost (InetAddress/getLocalHost)]
            (assoc-in context key-path (.getHostName localhost)))
          (catch UnknownHostException e
            (echo/echof "Cannot determine hostname (stack trace below), not adding config key '%s'"
              (pr-str config-key))
            (.printStackTrace e System/err)
            context))))))


(defn discover-project-edn-version
  "Discover application version from project.edn file containing :version key, and add to config if absent.
  Options:
  :config-key  - configuration key to update discovered version at, default: \"discovered.app.version\"
  :project-edn - resource path to the project EDN file, default: \"project.edn\" (in classpath)"
  ([context]
    (discover-project-edn-version context {}))
  ([context {:keys [config-key
                    project-edn]
             :or {config-key  "discovered.app.version"
                  project-edn "project.edn"}}]
    (kdef/discover-config context config-key
      (fn [key-path]
        (if-let [project-edn-resource (io/resource project-edn)]
          (try
            (let [proj-map (-> project-edn-resource
                             slurp
                             edn/read-string)]
              (if-let [version (:version proj-map)]
                (assoc-in context key-path version)
                (do
                  (echo/echof
                    "Cannot find key :version in the config read from classpath file '%s', not adding config key '%s'"
                    (pr-str project-edn)
                    (pr-str config-key))
                  context)))
            (catch Exception e
              (echo/echof "Error reading file '%s' in classpath as EDN (stack trace below), not adding config key '%s'"
                (pr-str project-edn)
                (pr-str config-key))
              (.printStackTrace e System/err)
              context))
          (do
            (echo/echof (str "Cannot find the file '%s' in classpath, not adding config key '%s'. "
                          "This may help: https://github.com/kumarshantanu/lein-project-edn")
              (pr-str project-edn)
              (pr-str config-key))
            context))))))
