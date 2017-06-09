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
    [bract.core.config  :as config]
    [bract.core.echo    :as echo]
    [bract.core.inducer :as inducer]
    [bract.core.util    :as util])
  (:import
    [bract.core Echo]))


(def default-root-context {(key config/ctx-config-files) ["config/config.dev.edn"]
                           (key config/ctx-launch?)      false})


(def default-root-inducers [inducer/set-verbosity
                            inducer/read-config
                            inducer/run-config-inducers])


(defn init
  "Initialize app in DEV mode."
  []
  (try
    (inducer/set-verbosity default-root-context)
    (echo/with-latency-capture "Initializing app in DEV mode"
      (inducer/induce inducer/apply-inducer default-root-context default-root-inducers))
    (catch Throwable e
      (.printStackTrace e)
      (echo/abort (.getMessage e)))))


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
  "Rebind var bract.core.dev/record-context! to the given context."
  [context]
  (alter-var-root #'app-context (constantly context))
  context)


(defn deinit
  "De-initialize application. Throw error if app-context is not initialized."
  []
  (ensure-init)
  (util/expected map? "app-context to be initialized as map using inducer bract.core.dev/record-context!" app-context)
  (let [f (config/ctx-deinit app-context)]
    (echo/with-latency-capture "De-initializing application"
      (f))))


(defn start
  "Launch application. Throw error if app-context is not initialized."
  []
  (ensure-init)
  (util/expected map? "app-context to be initialized as map using inducer bract.core.dev/record-context!" app-context)
  (echo/with-latency-capture "Launching application"
    (-> (config/ctx-config app-context)
      config/cfg-launcher
      (apply [(assoc app-context
                (key config/ctx-launch?) true)])
      record-context!)))


(defn stop
  "Stop the started application."
  []
  (ensure-init)
  (let [stopper (config/ctx-stopper app-context)]
    (echo/with-latency-capture "Stopping the started application"
      (stopper))))
