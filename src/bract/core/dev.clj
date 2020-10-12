;   Copyright (c) Shantanu Kumar. All rights reserved.
;   The use and distribution terms for this software are covered by the
;   Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
;   which can be found in the file LICENSE at the root of this distribution.
;   By using this software in any fashion, you are agreeing to be bound by
;   the terms of this license.
;   You must not remove this notice, or any other, from this software.


(ns bract.core.dev
  "Development and test support."
  (:require
    [clojure.java.javadoc :refer [javadoc]]
    [clojure.pprint       :refer [pprint]]
    [clojure.repl         :refer :all]
    [clojure.string     :as string]
    [bract.core.keydef  :as kdef]
    [bract.core.echo    :as echo]
    [bract.core.inducer :as inducer]
    [bract.core.main    :as main]
    [bract.core.util    :as util])
  (:import
    [bract.core Echo]))


;; ----- overrides -----


(defn context-file
  "Set context file to specified argument (unless environment variable `APP_CONTEXT` is set):

  | Value | Effect                       |
  |-------|------------------------------|
  |string | set context file as override |
  |`nil`  | clear context file override  |"
  ([]
    (when-let [context-file (System/getenv "APP_CONTEXT")]
      (util/err-println (format "Environment variable APP_CONTEXT='%s' overrides context file" context-file)))
    (System/getProperty "app.context"))
  ([context-filename]
    (if-let [env-context-file (System/getenv "APP_CONTEXT")]
      (util/err-println (format "Override failed due to environment variable APP_CONTEXT='%s'" env-context-file))
      (cond
        (nil? context-filename)    (do
                                     (System/clearProperty "app.context")
                                     nil)
        (string? context-filename) (do
                                     (System/setProperty "app.context" context-filename)
                                     context-filename)
        :otherwise                  (-> "Expected argument to be string or nil but found "
                                      (str (pr-str context-filename))
                                      (ex-info {:context-filename context-filename})
                                      throw)))))


(defn verbose
  "Set verbose mode to specified status (unless environment variable `APP_VERBOSE` is set):

  | Value | Effect                      |
  |-------|-----------------------------|
  |`true` | enable verbose mode         |
  |`false`| disable verbose mode        |
  |`nil`  | clear verbose mode override |"
  ([]
    (when-let [verbose (System/getenv "APP_VERBOSE")]
      (util/err-println (format "Environment variable APP_VERBOSE='%s' overrides verbosity" verbose)))
    (System/getProperty "app.verbose"))
  ([status?]
    (if-let [verbose (System/getenv "APP_VERBOSE")]
      (util/err-println (format "Override failed due to environment variable APP_VERBOSE='%s'" verbose))
      (case status?
        nil   (System/clearProperty "app.verbose")
        true  (do (System/setProperty "app.verbose" "true")  (Echo/setVerbose true))
        false (do (System/setProperty "app.verbose" "false") (Echo/setVerbose false))
        (throw (ex-info (str "Expected argument to be true, false or nil but found " (pr-str status?)) {}))))))


(defn config-files
  "Set config files to specified argument (unless environment variable `APP_CONFIG` is set):

  | Value    | Effect                       |
  |----------|------------------------------|
  |collection| set config files as override |
  |string    | set config files as override |
  |`nil`     | clear config file override   |"
  ([]
    (when-let [config-filenames (System/getenv "APP_CONFIG")]
      (util/err-println (format "Environment variable APP_CONFIG='%s' overrides config file" config-filenames)))
    (System/getProperty "app.config"))
  ([config-filenames]
    (if-let [env-config-filenames (System/getenv "APP_CONFIG")]
      (util/err-println (format "Override failed due to environment variable APP_CONFIG='%s'" env-config-filenames))
      (cond
        (nil? config-filenames)    (do
                                     (System/clearProperty "app.config")
                                     nil)
        (and (coll? config-filenames)
          (every? string?
            config-filenames))     (let [filenames (string/join ", " config-filenames)]
                                     (System/setProperty "app.config" filenames)
                                     filenames)
        (string? config-filenames) (do
                                     (System/setProperty "app.config" config-filenames)
                                     config-filenames)
        :otherwise                 (-> "Expected argument to be collection of string, string or nil but found "
                                     (str (pr-str config-filenames))
                                     (ex-info {:config-filenames config-filenames})
                                     throw)))))


;; ----- initial context -----


(def root-context {(key kdef/ctx-context-file) "bract-context.dev.edn"
                   (key kdef/ctx-config-files) ["config/config.dev.edn"]
                   (key kdef/ctx-launch?)      false})


(defonce ^:redef seed-context {})


(defn initial-context
  "Resolve and return the initial context to trigger the application in DEV mode."
  []
  (merge root-context seed-context))


;; ----- REPL helpers -----


(defn init
  "Initialize app in DEV mode."
  []
  (try
    (let [init-context (initial-context)]
      (inducer/set-verbosity init-context)
      (echo/with-latency-capture "Initializing app in DEV mode"
        (inducer/induce inducer/apply-inducer init-context main/root-inducers)))
    (catch Throwable e
      (util/pst-when-uncaught-handler e)
      (throw e))))


(defonce ^:redef init-gate nil)


(defn init-once!
  "Given a var e.g. (defonce a-var nil) having logical false value, set it to `true` and initialize app in DEV mode."
  ([]
    (init-once! #'init-gate))
  ([a-var]
  (util/exec-once! a-var
    (init))))


(defonce ^:redef app-context (format "Var %s/app-context not initialized" *ns*))


(defn ensure-init
  "Ensure that [[app-context]] is initialized."
  []
  (when (string? app-context)
    (init))
  (when (string? app-context)
    (throw (ex-info "Failed to ensure initialization. Add `bract.core.dev/record-context!` to your inducer list."
             {}))))


(defn record-context!
  "Rebind var [[app-context]] to the given context."
  [context]
  (alter-var-root #'app-context (constantly context))
  context)


(defn deinit
  "De-initialize application. Throw error if [[app-context]] is not initialized."
  []
  (ensure-init)
  (util/expected map? "app-context to be initialized as map using inducer bract.core.dev/record-context!" app-context)
  (echo/with-latency-capture "De-initializing application"
    (-> app-context
      inducer/invoke-deinit
      record-context!))
  nil)


(defn start
  "Launch application. Throw error if [[app-context]]` is not initialized."
  []
  (ensure-init)
  (util/expected map? "app-context to be initialized as map using inducer bract.core.dev/record-context!" app-context)
  (echo/with-latency-capture "Launching application"
    (-> app-context
      (assoc (key kdef/ctx-launch?) true)
      inducer/invoke-launchers
      record-context!))
  nil)


(defn stop
  "Stop the started application."
  []
  (ensure-init)
  (util/expected map? "app-context to be initialized as map using inducer bract.core.dev/record-context!" app-context)
  (echo/with-latency-capture "Stopping the started application"
    (inducer/invoke-stopper app-context))
  nil)
