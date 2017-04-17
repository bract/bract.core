;   Copyright (c) Shantanu Kumar. All rights reserved.
;   The use and distribution terms for this software are covered by the
;   Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
;   which can be found in the file LICENSE at the root of this distribution.
;   By using this software in any fashion, you are agreeing to be bound by
;   the terms of this license.
;   You must not remove this notice, or any other, from this software.


(ns bract.core.config
  (:require
    [clojure.string :as string]
    [keypin.core :as keypin]
    [keypin.util :as kputil]
    [bract.core.echo :as echo]
    [bract.core.util :as util])
  (:import
    [keypin ConfigIO PropertyConfigIO]))


(keypin/defkey
  ;; seed keys
  ctx-config        [:bract.core/config       map?         "Application config"]
  ctx-launch?       [:bract.core/launch?      kputil/bool? "Whether invoke launcher fn" {:default false}]
  ;; config keys
  cfg-workers       ["bract.core.workers"     vector?      "Vector of fully qualified worker fn names"
                     {:parser kputil/any->edn}]
  cfg-exports       ["bract.core.exports"     vector?      "Vector of config keys to export as system properties"]
  cfg-launcher      ["bract.core.launcher"    fn?          "Fully qualified launcher fn name"
                     {:parser kputil/str->var->deref}])


(defn roll
  "Given a collection of arity-1 worker fns and optional seed, roll the seed through each worker fn, which returns an
  updated seed."
  ([workers]
    (roll {} workers))
  ([seed workers]
    (echo/echo "Executing Bract workers")
    (reduce (fn [context each-name]
              (echo/echo (format "Looking up worker '%s'" each-name))
              (let [f (kputil/str->var->deref (key cfg-workers) each-name)]
                (echo/echo (format "Executing  worker '%s'" each-name))
                (f context)))
      seed workers)))


(defn run-app
  [config launch?]
  (-> {}
    (assoc (key ctx-config) config)
    (assoc (key ctx-launch?) launch?)
    (roll (cfg-workers config))))


(defn print-config
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
