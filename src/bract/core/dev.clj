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
    [bract.core.keydef  :as kdef]
    [bract.core.echo    :as echo]
    [bract.core.inducer :as inducer]
    [bract.core.util    :as util])
  (:import
    [bract.core Echo]))


;; ----- overrides -----


(defn verbose
  "Set verbose mode to specified status (unless environment variable APP_VERBOSE is set):
  true  - enable verbose mode
  false - disable verbose mode
  nil   - clear verbose mode override"
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


(defn config
  "Set config files to specified argument (unless environment variable APP_CONFIG is set):
  string - set config files as override
  nil    - clear config file override"
  ([]
    (when-let [config-file (System/getenv "APP_CONFIG")]
      (util/err-println (format "Environment variable APP_CONFIG='%s' overrides config file" config-file)))
    (System/getProperty "app.config"))
  ([config]
    (if-let [config-file (System/getenv "APP_CONFIG")]
      (util/err-println (format "Override failed due to environment variable APP_CONFIG='%s'" config-file))
      (cond
        (nil? config)    (System/clearProperty "app.config")
        (string? config) (System/setProperty "app.config" config)
        :otherwise       (throw (ex-info (str "Expected argument to be string or nil but found " (pr-str config))
                                  {}))))))


;; ----- default -----


(def default-root-context {(key kdef/ctx-context-file) "bract-context.dev.edn"
                           (key kdef/ctx-config-files) ["config/config.dev.edn"]
                           (key kdef/ctx-launch?)      false})


(def default-root-inducers [inducer/set-verbosity
                            inducer/read-context
                            inducer/run-context-inducers
                            inducer/read-config
                            inducer/run-config-inducers])


(defn init
  "Initialize app in DEV mode."
  []
  (inducer/set-verbosity default-root-context)
  (echo/with-latency-capture "Initializing app in DEV mode"
    (inducer/induce inducer/apply-inducer default-root-context default-root-inducers)))


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
  "Ensure that app-context is initialized."
  []
  (when (string? app-context)
    (init))
  (when (string? app-context)
    (throw (ex-info "Failed to ensure initialization. Add `bract.core.dev/record-context!` to your inducer list."
             {}))))


(defn record-context!
  "Rebind var bract.core.dev/app-context to the given context."
  [context]
  (alter-var-root #'app-context (constantly context))
  context)


(defn deinit
  "De-initialize application. Throw error if app-context is not initialized."
  []
  (ensure-init)
  (util/expected map? "app-context to be initialized as map using inducer bract.core.dev/record-context!" app-context)
  (echo/with-latency-capture "De-initializing application"
    (inducer/invoke-deinit app-context)))


(defn start
  "Launch application. Throw error if app-context is not initialized."
  []
  (ensure-init)
  (util/expected map? "app-context to be initialized as map using inducer bract.core.dev/record-context!" app-context)
  (echo/with-latency-capture "Launching application"
    (-> app-context
      (assoc (key kdef/ctx-launch?) true)
      inducer/invoke-launcher
      record-context!)))


(defn stop
  "Stop the started application."
  []
  (ensure-init)
  (util/expected map? "app-context to be initialized as map using inducer bract.core.dev/record-context!" app-context)
  (echo/with-latency-capture "Stopping the started application"
    (inducer/invoke-stopper app-context)))
