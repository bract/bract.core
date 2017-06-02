;   Copyright (c) Shantanu Kumar. All rights reserved.
;   The use and distribution terms for this software are covered by the
;   Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
;   which can be found in the file LICENSE at the root of this distribution.
;   By using this software in any fashion, you are agreeing to be bound by
;   the terms of this license.
;   You must not remove this notice, or any other, from this software.


(ns bract.core.config
  "Context and configuration keys used by bract.core and functinos associated with config."
  (:require
    [clojure.string :as string]
    [keypin.core :as keypin]
    [keypin.util :as kputil]
    [bract.core.echo :as echo]
    [bract.core.util :as util])
  (:import
    [java.io OutputStream Writer]
    [java.util Map]
    [keypin ConfigIO PropertyConfigIO]))


(keypin/defkey  ; context keys
  ctx-verbose?      [:bract.core/verbose? kputil/bool? "Verbose initialization?" {:parser  kputil/any->bool
                                                                                  :default true
                                                                                  :envvar  "APP_VERBOSE"
                                                                                  :sysprop "app.verbose"}]
  ctx-config-files  [:bract.core/config-files  vector? "Config file names"       {:parser  kputil/any->vec
                                                                                  :default []
                                                                                  :envvar  "APP_CONFIG"
                                                                                  :sysprop "app.config"}]
  ctx-cli-args      [:bract.core/cli-args      coll?   "Collection of CLI arguments"]
  ctx-config        [:bract.core/config        map?    "Application config"]
  ctx-deinit        [:bract.core/deinit        fn?     "De-initialization function (fn []) for the app"
                     {:default #(echo/echo "Application de-init is not configured, skipping de-initialization.")}]
  ctx-launch?       [:bract.core/launch?  kputil/bool? "Whether invoke launcher fn" {:default false}]
  ctx-stopper       [:bract.core/stopper       fn?     "Function (fn []) to stop the started application"
                     {:default #(echo/echo "Application stopper is not configured, skipping stop.")}])


(keypin/defkey  ; config keys
  cfg-inducers      ["bract.core.inducers"     vector? "Vector of fully qualified inducer fn names"
                     {:parser kputil/any->edn}]
  cfg-context-hook  ["bract.core.context-hook" fn?     "Fully qualified context hook fn name"
                     {:parser kputil/str->var->deref}]
  cfg-config-hook   ["bract.core.config-hook"  fn?     "Fully qualified config hook fn name"
                     {:parser kputil/str->var->deref}]
  cfg-exports       ["bract.core.exports"      vector? "Vector of config keys to export as system properties"
                     {:parser kputil/any->edn}]
  cfg-launcher      ["bract.core.launcher"     fn?     "Fully qualified launcher fn name"
                     {:parser kputil/str->var->deref}])

;; ----- utility fns -----


(defn apply-inducer-by-name
  "Given a context and a fully qualified inducer fn name, load the fn and apply it to the context returning an updated
  context."
  ([context inducer-name]
    (apply-inducer-by-name "inducer" (key cfg-inducers) context inducer-name))
  ([inducer-type config-key context inducer-name]
    (echo/echo (format "Looking up %s `%s`" inducer-type inducer-name))
    (let [f (kputil/str->var->deref config-key inducer-name)]
      (echo/with-latency-capture (format "Executing %s `%s`" inducer-type inducer-name)
        (echo/with-inducer-name inducer-name
          (f context))))))


(defn apply-inducer
  "Given a context and inducer (either a fn or inducer-name), apply it to the context."
  ([context inducer-or-name]
    (apply-inducer context "inducer" inducer-or-name))
  ([context inducer-type inducer-or-name]
    (if (ifn? inducer-or-name)
      (echo/with-latency-capture (format "Executing %s `%s`" inducer-type inducer-or-name)
        (echo/with-inducer-name inducer-or-name
          (inducer-or-name context)))
      (apply-inducer-by-name inducer-or-name))))


(defn print-config
  "Print the given config using the format determined from the supplied config file names."
  [config config-filenames]
  (let [^ConfigIO configIO (if (some (comp #(.endsWith ^String % ".edn")
                                       string/trim
                                       string/lower-case)
                                 config-filenames)
                             keypin/edn-file-io
                             PropertyConfigIO/INSTANCE)]
    (.writeConfig configIO ^Writer *out* ^Map config true)))


(defn resolve-config
  "Given a collection of config filenames, read and resolve config as a map and return it."
  [context config-filenames]
  (let [keypin-opts {:parent-key   "parent.config.filenames"
                     :info-logger  #(echo/echo "[keypin] [info]" %)
                     :error-logger #(echo/echo "[keypin] [error]" %)}]
    (if (contains? context ctx-config)
      (let [pre-config (ctx-config context)]
        (as-> config-filenames <>
          (keypin/read-config <> (assoc keypin-opts
                                   :realize? false)) ; read config, but do not realize (i.e. evaluate variables)
          (kputil/clojurize-data <>)
          (merge pre-config <>)                      ; merge config onto the pre-existing config
          (keypin/realize-config <> keypin-opts)
          (kputil/clojurize-data <>)))
      (-> config-filenames
        (keypin/read-config keypin-opts)
        kputil/clojurize-data))))
