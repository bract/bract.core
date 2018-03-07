;   Copyright (c) Shantanu Kumar. All rights reserved.
;   The use and distribution terms for this software are covered by the
;   Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
;   which can be found in the file LICENSE at the root of this distribution.
;   By using this software in any fashion, you are agreeing to be bound by
;   the terms of this license.
;   You must not remove this notice, or any other, from this software.


(ns bract.core.main
  "Provided entry-point for Bract CLI applications."
  (:require
    [bract.core.keydef  :as core-kdef]
    [bract.core.inducer :as core-inducer]
    [bract.core.util    :as core-util])
  (:gen-class))


(def root-inducers
  "Root inducers for bract.core module."
  [core-inducer/set-verbosity        ; set default verbosity
   core-inducer/read-context         ; read context file if present
   core-inducer/set-verbosity        ; set user-preferred verbosity
   core-inducer/read-config          ; read config file(s) if specified
   core-inducer/run-context-inducers ; run context inducers
   ])


(defn delegate-main
  "Delegate the calling of `main` fn."
  [context root-inducers]
  (try
    (when-let [exit-code (-> context
                           (core-inducer/induce root-inducers)
                           core-kdef/ctx-app-exit-code)]
      (System/exit (int exit-code)))
    (catch Throwable e
      (core-util/pst-when-uncaught-handler e)
      (throw e))))


(defn -main
  "Java main() method entry point - upon AOT compiling this namespace and using it as main class."
  [& args]
  (delegate-main
    {(key core-kdef/ctx-cli-args) (vec args)}
    root-inducers))
