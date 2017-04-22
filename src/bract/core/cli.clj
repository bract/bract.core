;   Copyright (c) Shantanu Kumar. All rights reserved.
;   The use and distribution terms for this software are covered by the
;   Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
;   which can be found in the file LICENSE at the root of this distribution.
;   By using this software in any fashion, you are agreeing to be bound by
;   the terms of this license.
;   You must not remove this notice, or any other, from this software.


(ns bract.core.cli
  "Command-line argument parsing and processing support."
  (:require
    [clojure.string    :as string]
    [clojure.tools.cli :as cli]
    [keypin.core       :as keypin]
    [keypin.util       :as kputil]
    [bract.core.config :as config]
    [bract.core.echo   :as echo]
    [bract.core.util   :as util])
  (:import
    [bract.core Echo]))


(def cli-options
  [["-c" "--command COMMAND"         "Command to run: run, printcfg, repl" :default "run"]
   ["-f" "--config-file CONFIG-FILE" "Config file names (comma-separated)"]
   ["-l" "--launch"      "Launch the program" :default false]
   ["-h" "--help"        "Show usage"         :default false]
   ["-v" "--verbose"     "Verbose execution"  :default false]])


(defn process-command
  [command config-filenames]
  (let [config (-> config-filenames
                 keypin/read-config
                 kputil/clojurize-data)]
    (case command
      "run"    (config/run-app config true)
      "dryrun" (config/run-app config false)
      "config" (config/print-config config config-filenames)
      "repl"   (clojure.main/main)
      (echo/abort (format "Invalid command '%s', valid commands are: run, dryrun, config, repl" command)))))


(defn start
  "Parse CLI args and start the application"
  [& args]
  (let [{:keys [options arguments summary errors]} (cli/parse-opts args cli-options)]
    (when (:verbose options)
      (Echo/setVerbose true))
    (cond
      (:help options) (util/err-println summary)
      errors          (util/err-println (string/join \newline errors))
      :otherwise      (if-let [config-filenames (config/resolve-config-filenames (:config-file options) nil)]
                        (process-command (:command options) config-filenames)
                        (do
                          (util/err-println "No config file specified as argument, nor APP_CONFIG env var is defined")
                          (util/err-println summary))))))
