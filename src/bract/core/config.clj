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
    [keypin ConfigIO PropertyConfigIO]))


(keypin/defkey  ; context keys
  ctx-config        [:bract.core/config  map?         "Application config"]
  ctx-deinit        [:bract.core/deinit  fn?          "De-initialization function (fn []) for the app"
                     {:default #(echo/echo "Application de-init is not configured, skipping de-initialization.")}]
  ctx-launch?       [:bract.core/launch? kputil/bool? "Whether invoke launcher fn" {:default false}]
  ctx-stopper       [:bract.core/stopper fn?          "Function (fn []) to stop the started application"
                     {:default #(echo/echo "Application stopper is not configured, skipping stop.")}])


(keypin/defkey  ; config keys
  cfg-inducer-names ["bract.core.inducers"     vector? "Vector of fully qualified inducer fn names"
                     {:parser kputil/any->edn}]
  cfg-context-hook  ["bract.core.context-hook" fn?     "Fully qualified context hook fn name"
                     {:parser kputil/str->var->deref}]
  cfg-config-hook   ["bract.core.config-hook"  fn?     "Fully qualified config hook fn name"
                     {:parser kputil/str->var->deref}]
  cfg-exports       ["bract.core.exports"      vector? "Vector of config keys to export as system properties"
                     {:parser kputil/any->edn}]
  cfg-launcher      ["bract.core.launcher"     fn?     "Fully qualified launcher fn name"
                     {:parser kputil/str->var->deref}])


(defn apply-inducer-by-name
  "Given a context and a fully qualified inducer fn name, load the fn and apply it to the context returning an updated
  context."
  ([context inducer-name]
    (apply-inducer-by-name "inducer" (key cfg-inducer-names) context inducer-name))
  ([inducer-type config-key context inducer-name]
    (echo/echo (format "Looking up %s `%s`" inducer-type inducer-name))
    (let [f (kputil/str->var->deref config-key inducer-name)]
      (echo/with-latency-capture (format "Executing  %s `%s`" inducer-type inducer-name)
        (echo/with-inducer-name inducer-name
          (f context))))))


(defn run-app
  "Given the parsed config and launch flag run the app."
  [config launch?]
  (echo/echo "Applying Bract inducers")
  (-> {}
    (assoc (key ctx-config) config)
    (assoc (key ctx-launch?) launch?)
    (util/induce apply-inducer-by-name (cfg-inducer-names config))))


(defn print-config
  "Print the given config using the format determined from the supplied config file names."
  [config config-filenames]
  (let [^ConfigIO configIO (if (some (comp #(.endsWith ^String % ".edn")
                                       string/trim
                                       string/lower-case)
                                 config-filenames)
                             keypin/edn-file-io
                             PropertyConfigIO/INSTANCE)]
    (.writeConfig configIO *out* config true)))


(defn resolve-config-filenames
  "Given config filenames as a comma-separated string (potentially nil) and default filename (potentially nil) return a
  vector of config filenames."
  [config-filenames-str default-filename-str]
  (when-let [config-filename-str (or (-> config-filenames-str (echo/->echo "Specified config filename(s)"))
                                   (-> (System/getenv "APP_CONFIG") (echo/->echo "Env var APP_CONFIG"))
                                   (-> default-filename-str (echo/->echo "Fallback config filename")))]
    (as-> config-filename-str $
      (string/split $ #",")
      (mapv string/trim $))))


(defn resolve-config
  "Given a collection of config filenames, read and resolve config as a map and return it."
  [config-filenames]
  (-> config-filenames
    keypin/read-config
    kputil/clojurize-data))
