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
    [bract.core.config :as config]
    [bract.core.echo   :as echo]
    [bract.core.util   :as util])
  (:import
    [bract.core Echo]))


(defn set-verbosity
  "Set Bract verbosity flag and return context."
  [context]
  (let [pre-verbose?  (Echo/isVerbose)
        post-verbose? (config/ctx-verbose? context)]
    (Echo/setVerbose post-verbose?)
    (when (and (not pre-verbose?) post-verbose?)
      (echo/echo
        "Verbose mode enabled - override with env var APP_VERBOSE or system property app.verbose: value true/false")))
  context)


(defn read-config
  "Use config filenames in the context to read and resolve config, and populate the context with it."
  [context]
  (let [config-files (config/ctx-config-files context)]
    (if (seq config-files)
      (->> config-files
        (config/resolve-config context)
        (assoc context (key config/ctx-config)))
      context)))


(defn run-context-inducers
  "Run the inducers specified in the context."
  [context]
  (->> (config/ctx-inducers context)
    (util/induce context config/apply-inducer)))


(defn run-config-inducers
  "Run the inducers specified in the application config."
  [context]
  (->> (config/ctx-config context)
    config/cfg-inducers
    (util/induce context config/apply-inducer-by-name)))


(defn context-hook
  "Given context with config, read the fully qualified context-hook fn name and invoke it as (fn [context])."
  [context]
  (let [config (config/ctx-config context)]
    (-> (config/cfg-context-hook config)
      (apply [context]))
    context))


(defn config-hook
  "Given context with config, read the fully qualified config-hook fn name and invoke it as (fn [config])."
  [context]
  (let [config (config/ctx-config context)]
    (-> (config/cfg-config-hook config)
      (apply [config]))
    context))


(defn export-as-sysprops
  "Given context with config, read the value of config key \"bract.core.exports\" as a vector of string config keys and
  export the key-value pairs for those config keys as system properties."
  [context]
  (let [config (config/ctx-config context)
        exlist (-> (config/cfg-exports config)
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
  (let [config (config/ctx-config context)
        exlist (-> (config/cfg-exports config)
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
  (if (config/ctx-launch? context)
    (-> (config/ctx-config context)
      config/cfg-launcher
      (apply [context]))
    (do
      (echo/echo "Launch not enabled, skipping launch.")
      context)))


(defn deinit
  "Given context with :bract.core/deinit key and corresponding (fn []) de-init fn for the app, invoke it."
  [context]
  (let [f (config/ctx-deinit context)]
    (f))
  context)
