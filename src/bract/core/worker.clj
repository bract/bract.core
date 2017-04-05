;   Copyright (c) Shantanu Kumar. All rights reserved.
;   The use and distribution terms for this software are covered by the
;   Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
;   which can be found in the file LICENSE at the root of this distribution.
;   By using this software in any fashion, you are agreeing to be bound by
;   the terms of this license.
;   You must not remove this notice, or any other, from this software.


(ns bract.core.worker
  "The workers exposed by Bract-core"
  (:require
    [bract.core.config :as config]
    [bract.core.echo   :as echo]
    [bract.core.util   :as util]))


(defn export-as-sysprops
  "Given seed context with config under the key :bract.core/config, read the value of config key \"bract.core.exports\"
  as a vector of string config keys and export the key-value pairs for those config keys as system properties."
  [seed]
  (let [config (config/seed-config seed)
        exlist (-> (config/cfg-exports config)
                 (echo/->echo "Exporting as system properties"))]
    (doseq [each exlist]
      (util/expected string? "export property name as string" each)
      (when-not (contains? config each)
        (util/expected (format "export property name '%s' to exist in config" each) config))
      (util/expected string? (format "value for export property name '%s' as string" each) (get config each))
      (System/setProperty each (get config each)))
    seed))


(defn invoke-launcher
  "Given seed context with config under the key :bract.core/config, read the value of config key \"bract.core.launcher\"
  as a fully qualified launcher fn name and invoke it when the context seed key :bract.core/launch? has the value true."
  [seed]
  (if (config/seed-launch? seed)
    (-> (config/seed-config seed)
      config/cfg-launcher
      (apply [seed]))
    seed))
