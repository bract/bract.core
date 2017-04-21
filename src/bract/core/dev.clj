;   Copyright (c) Shantanu Kumar. All rights reserved.
;   The use and distribution terms for this software are covered by the
;   Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
;   which can be found in the file LICENSE at the root of this distribution.
;   By using this software in any fashion, you are agreeing to be bound by
;   the terms of this license.
;   You must not remove this notice, or any other, from this software.


(ns bract.core.dev
  (:require
    [bract.core.config :as config]
    [bract.core.echo   :as echo]
    [bract.core.util   :as util])
  (:import
    [bract.core Echo]))


(def default-config-filename "config.dev.edn")


(defn init
  "Initialize app in DEV mode."
  []
  (try
    (Echo/setVerbose true)
    (echo/echo "Initializing app in DEV mode")
    (let [start (System/currentTimeMillis)]
      (let [result (as-> default-config-filename $
                     (config/resolve-config-filenames nil $)
                     (config/resolve-config $)
                     (config/run-app $ false))]
        (echo/echo (format "Initialized app in DEV mode in %dms" (- (System/currentTimeMillis) start)))
        result))
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
