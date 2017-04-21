;   Copyright (c) Shantanu Kumar. All rights reserved.
;   The use and distribution terms for this software are covered by the
;   Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
;   which can be found in the file LICENSE at the root of this distribution.
;   By using this software in any fashion, you are agreeing to be bound by
;   the terms of this license.
;   You must not remove this notice, or any other, from this software.


(ns bract.core.inducer
  "The inducer fns exposed by Bract-core"
  (:require
    [bract.core.config :as config]
    [bract.core.echo   :as echo]
    [bract.core.util   :as util]))


(defn context-hook!
  "Given context with config, read the fully qualified context-hook fn name and invoke it as (fn [context])."
  [context]
  (let [config (config/ctx-config context)]
    (-> (config/cfg-context-hook config)
      (apply [context]))
    context))


(defn config-hook!
  "Given context with config, read the fully qualified config-hook fn name and invoke it as (fn [config])."
  [context]
  (let [config (config/ctx-config context)]
    (-> (config/cfg-config-hook config)
      (apply [config]))
    context))


(defn export-as-sysprops
  "Given context with config under the key :bract.core/config, read the value of config key \"bract.core.exports\"
  as a vector of string config keys and export the key-value pairs for those config keys as system properties."
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


(defn invoke-launcher
  "Given context with config under the key :bract.core/config, read the value of config key \"bract.core.launcher\"
  as a fully qualified launcher fn name and invoke it when the context key :bract.core/launch? has the value true."
  [context]
  (if (config/ctx-launch? context)
    (-> (config/ctx-config context)
      config/cfg-launcher
      (apply [context]))
    context))
