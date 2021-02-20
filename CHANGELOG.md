# bract.core Change Log

## Ideas and TODO

- [Todo] Add Bract's own `defkey` that looks up config, followed by context
- [Todo] Combined context/config lookup for variable substitution
- [Idea] Dev triggers should accept an optional param
  - Init a sub-system
  - De-init a sub-system
  - Launch a sub-system
  - etc.
- [Idea] Support for parameterized (or prefixed/qualified) config
- [Idea] Function to transform every config key, based on `:bract.core/ctx-config-key-xf {:default identity}`
  - Helpful to auto-stringify keys in EDN config files
- [Idea - Dev] Several tasks should accept an optional env key, e.g. `(start :qa)` that looks up env from context
  - Requires env key/alias definition
  - Env key/alias should switch the config file(s)
- Config
  - [Todo] Add a context keydef (fn) to determine whether draining is over, for `add-shutdown-hook` times out
  - [Todo] Make config and context entries accessible to each other for substitutions
  - [Todo] Realize variables as extraction time (late, not early)
  - [Todo - BREAKING CHANGE] Remove exports config keydef `"bract.core.exports"` (in favor of using variable instead)
  - [Todo - BREAKING CHANGE] Have inducers dealing with system properties accept export-key/value arguments
    - `export-as-sysprops`
    - `unexport-sysprops`
  - [Todo - BREAKING CHANGE] Drop inducer `run-context-inducers` in favor of dynamic/late variable substitution
  - [Todo - BREAKING CHANGE] Drop inducer `run-config-inducers` in favor of dynamic/late variable substitution


### [Ideas and Todo] 0.7.0 / 2020-October-??

- [Todo - BREAKING] Make app-context key definition a vector of string
  - String (env var, system property) to be parsed as comma separated tokens
  - [Todo - BREAKING] Context reader should follow the cascading chain
  - [Todo] Make `bract-context.edn` a cascading choice (after `bract-context.run.edn`)
  - [Todo - BREAKING] When neither is found, throw exception
- [Idea] Tagged literal support for context and config
  - Internal reference (self-context reference)
  - Cross reference
- [Idea] Create echo functions for success, failure, latency, open-close fragments, exception stack trace
  - [Idea] Could be replaced by colored printers in DEV mode
- [Idea] Find a way to resolve variables at runtime (dynamic + late)
  - [Todo - BREAKING CHANGE] Remove exports config keydef `"bract.core.exports"` (in favor of using variable instead)
  - [Todo - BREAKING CHANGE] Have inducers dealing with system properties accept export-key/value arguments
    - `export-as-sysprops`
    - `unexport-sysprops`
  - [Todo - BREAKING CHANGE] Drop inducer `run-context-inducers` in favor of dynamic/late variable substitution
  - [Todo - BREAKING CHANGE] Drop inducer `run-config-inducers` in favor of dynamic/late variable substitution
  - [Todo] Add inducers
    - (push-context [context :key])
    - (pull-context [context :key])
    - (pop-context [context :key])
- [Todo - BREAKING CHANGE] Echo overhaul
  - Accommodate structured-format output
  - Tag print fns with `^:redef` so that they can emit coloured, formatted output
- [Todo] Registry of key definitions, so that values can be validated when added to context


## Releases

### 0.6.2-beta6 / 2021-February-21

- Include context values preview in induction report
- Do not set CLI-args in DEV root context
  - To disable CLI processing by default
- Add helper macro `bract.core.inducer/when-context-has-key` for conditional passthrough


### 0.6.2-beta5 / 2021-February-18

- Rethrow exception in `bract.core.util/thrown->val` using `clojure.core/future`
- Include initial context keys in verbose induction summary
- Include default empty CLI-args in DEV root context
  - Support for modules to treat DEV default entry-point on equal footing as main


### 0.6.2-beta4 / 2021-February-14

- Utility macros
  - `bract.core.util/thrown->val`
  - `bract.core.util/after`
  - `bract.core.util/doafter`
- Wrap fn returned by `bract.core.keydef/resolve-event-logger` to ignore event-logging exceptions


### 0.6.2-alpha4 / 2021-February-13

- Verbose-mode `bract.core.inducer/induce` - print an induction report (table)
  - Inducers executed
  - Keys added/removed/updated in app-context
  - Time taken for each inducer
- Add utility fn `bract.core.util/nop` that does nothing
- Metrics event logging mechanism, no-op by default
  - Context key `:bract.core/event-logger`
  - Config keys `"bract.core.eventlog.(enable, allow, block)"`
  - Utility fn `bract.core.keydef/resolve-context`
- Dev mode context key `:bract.core/dev-mode?`
  - Populated as `true` by DEV entry points


### 0.6.2-beta3 / 2021-February-06

- Upgrade Keypin to version `0.8.2` (for new predicate and var metadata)


### 0.6.2-beta2 / 2021-January-28

- Add `bract.core.dev/help` fn for REPL help text


### 0.6.2-beta1 / 2021-January-27

- Demunge function names displayed in verbose/echo mode


### 0.6.2-alpha3 / 2020-October-12

- Improve `bract.core.dev` namespace
  - Make `bract.core.dev` usable for REPL - require/refer useful vars
  - Add `bract.core.dev/-main` for running application in DEV mode
- Documentation
  - Reformat docstring in `bract.core.dev` for environment variables


### 0.6.2-alpha2 / 2020-October-05

- Add inducer `bract.core.inducer/abort` (moved from `gossamer.core.inducer/abort`)
- Documentation
  - Add quickstart 'Greeting' example app


### 0.6.2-alpha1 / 2020-September-30

- Config
  - Upgrade Keypin to version `0.8.1` (for remote and cached config stores)
- Development support
  - Add function `bract.core.dev/initial-context` to resolve DEV mode initial context
  - Make namespace `bract.core.dev-init` safe for loading (e.g. by _cljdoc_)
  - Add `bract.core.util/err-print-banner` for printing banner messages to `*err*`
- Documentation
  - Reformat docstring for _cljdoc_
  - Add _cljdoc_ badge
  - Add documentation page covering context and config keys


### 0.6.1 / 2018-October-10

- Config
  - Upgrade Keypin dependency to version `0.7.6`
    - For `:source` option in key definitions


### 0.6.0 / 2018-May-16

- Config
  - Upgrade Keypin dependency to version `0.7.4`
    - For parser function `keypin.util/str->fn`
- Key definitions
  - [BREAKING CHANGE] Change launcher key from config `"bract.core.launcher"` to context `:bract.core/launchers`
    - Vector of launcher fns
  - [BREAKING CHANGE] Set default context file to `bract-context.edn`
  - Set default value for config key `config-inducers` to `[]`
  - Change launcher parser from `str->var` to `any->fn`
  - Add context utility fn `bract.core.keydef/induce-exit`
  - Change exit-code constraint from 'zero or +ve int' to 'int'
- Inducers
  - Fix issue where vector and map arguments are misinterpreted as functions upon parsing
  - [BREAKING CHANGE] Remove inducer `bract.core.inducer/fallback-config-files`
  - [BREAKING CHANGE] Rename inducer `invoke-launcher` to `invoke-launchers`
  - [BREAKING CHANGE] Remove inducer `prepare-launcher`
  - Output important messages to STDERR when echo is disabled
  - Echo the inducer name and error message on exception in inducer
  - Echo inducer-list key in `run-context-inducers` and `run-config-inducers`
- CLI entrypoint
  - Add `bract.core.main` namespace for CLI entry point
    - Define root inducers
- Development support
  - [BREAKING CHANGE] Drop dev root inducers in favour of `bract.core.main` root inducers
  - [BREAKING CHANGE] Rename `bract.core.dev/default-root-context` to `bract.core.dev/root-context`
  - Add `bract.core.dev/context-file` to reveal or override the context file
  - Refactor `bract.core.dev/config`
    - [BREAKING CHANGE] Rename to `bract.core.dev/config-files`
    - Accept a collection of config filenames as argument
  - Add `bract.core.dev/seed-context` to potentially override the root-context
- Utility
  - Add `let-var` utility macro


### 0.5.1 / 2018-March-05

- Add context key definition `:bract.core/app-exit-code` - app exit code (int, >= 0), default nil
- Add namespace `bract.core.dev-init` to easily initialize test namespaces
- Inducer
  - `discover-hostname` - discover hostname and populate config
  - `discover-project-edn-version` - discover project version and populate config
  - `fallback-config-files` - specify config filenames when unspecified
  - `prepare-launcher` - specify alternate launcher on-the-fly
- Fix issue where resolving new config ignores existing config


### 0.5.0 / 2018-February-18

- Config
  - Upgrade Keypin dependency to version `0.7.2`
    - Symbol/keyword variable substitution in EDN context/config
  - [BREAKING CHANGE] Apply parent key `parent.filenames` to both context and config files
- Key definitions
  - Add `:bract.core/health-check` to represent health check functions
  - Add `:bract.core/alive-tstamp` to represent last alive timestamp recorder/reporter
  - [BREAKING CHANGE] Rename `:bract.core/shutdown-flag` to `:bract.core/*shutdown-flag`
  - [BREAKING CHANGE] Change `:bract.core/shutdown-hooks` to be a vector of hooked threads
- Dev helpers
  - [BREAKING CHANGE] Do not return context from some `bract.core.dev` functions (for REPL usability)
    - `deinit`, `start`, `stop`
  - Update `app-context` after deinit
- Inducer
  - Make inducer `invoke-launcher` echo the launcher name
  - Make inducer `invoke-deinit` empty the deinit vector before returning context
- Utility
  - [BREAKING CHANGE] Drop `bract.core.util/uuid-str` in favour of `bract.core.util/clean-uuid`
  - Add utility function `bract.core.util.runtime/sysinfo` to report system info
  - Add string and unit conversion functions


### 0.4.1 / 2017-August-08

- Make `bract.core.keydef/ctx-shutdown-flag :bract.core/shutdown-flag` volatile instead of atom
- Fix reverse order of reporting remaining drain time


### 0.4.0 / 2017-August-05

- [BREAKING CHANGE] Remove inducer `bract.core.inducer/config-hook`


### 0.4.0-alpha2 / 2017-August-01

- Merge context from `bract-context.dev.edn` in dev mode when available
- Catch exception and print stack trace in dev mode (due to uncaught handler)
- Key definition for context-inducers now defaults to an empty vector
- Add utility function `pst-when-uncaught-handler` for printing stack trace


### 0.4.0-alpha1 / 2017-July-31

- Upgrade Keypin dependency to version `0.7.1`
  - Seamless 'duration' expression
  - Restrict context file reading to EDN only
- [BREAKING CHANGE] Rename `bract.core.config` namespace to `bract.core.keydef`
- Add `read-context` inducer to read/merge-into context from a file
  - Context-key `:bract.core/context-file` to optionally specify a file to merge context from
- Overhaul application deinit
  - [BREAKING CHANGE] Rename inducer `deinit` to `invoke-deinit` for consistency with other inducers
  - [BREAKING CHANGE] De-init to be a list of `(fn [])` passed around with context
- Add `add-shutdown-hook` inducer to handle application termination signal
- Add `set-default-exception-handler` inducer to handle uncaught exceptions in threads


### 0.3.1 / 2017-June-30

- Do not abort on inducer exception in dev mode, just rethrow (useful for REPL/reload workflow)
- Add `uuid-str` utility function - unique ID for various purposes
- Move generic Bract text from README to the external documentation
- Add `invoke-stopper` inducer to stop a running application


### 0.3.0 / 2017-June-11

- Inducer short-circuit (escape hatch) mechanism
  - Break out of the current batch of inducers upon encountering reduced context
  - Break out of all levels of inducers upon encountering context attribute `:bract.core/exit?` true
- Inducer implementation detail
  - Inducers are now backed by `bract.core.type/IFunction` protocol
  - Supported inducer spec: function, string, symbol, vector, map, var
- Allow inducers to accept additional arguments other than context
  - [BREAKING CHANGE] Remove inducer related functions from the `bract.core.config` namespace
    - `bract.core.config/apply-inducer`
    - `bract.core.config/apply-inducer-by-name`
  - [BREAKING CHANGE] Remove inducer related functions from the `bract.core.util` namespace
    - `bract.core.util/apply-inducer`
    - `bract.core.util/induce`
  - Add functions in the `bract.core.inducer` namespace to apply inducers
    - `bract.core.inducer/apply-inducer` for functions (for direct internal calls to induce)
    - `bract.core.inducer/induce` for applying a collection of inducers
- [BREAKING CHANGE] Change of arity in inducers `context-hook` and `config-hook`
  - Inducer `bract.core.inducer/context-hook` no more supports arity-1, accepts function as second argument
  - Inducer `bract.core.inducer/config-hook` no more supports arity-1, accepts function as second argument
  - Config definition for "bract.core.context-hook" is removed
  - Config definition for "bract.core.config-hook" is removed
- Replace default dev config file `config.dev.edn` with `config/config.dev.edn`
- Inducers for running other inducers
  - [BREAKING CHANGE] Rename `bract.core.inducer/run-inducers` to `run-config-inducers`
    - Config key definition `bract.core.inducers`
    - Add arity-2 to `run-config-inducers` to run inducers specified by a config lookup key
  - Add `bract.core.inducer/run-context-inducers` to run inducers from context
    - Context key definition `:bract.core/inducers`
    - Add arity-2 to `run-context-inducers` to run inducers specified by a context lookup key
- Development mode helpers
  - `bract.core.dev/verbose` to get/set verbosity level
  - `bract.core.dev/config` to get/set config file override
  - Functions `(config)` and `(verbose)` return current setting
  - Functions `config` and `verbose` warn when environment variable will prevent any override


### 0.2.0 / 2017-June-04

- Factor out CLI handling into module `bract.cli`
  - [BREAKING CHANGE] Drop `bract.core.cli` namespace
  - [BREAKING CHANGE] Drop `bract.core.main` namespace
- Overhaul config
  - Verbosity may be now be overridden with environment variable `APP_VERBOSE` or system property `app.verbose`
  - Verbosity is disabled by default
  - Add config keys `ctx-verbose?`, `ctx-config-files`, `ctx-cli-args` in `bract.core.config` namespace
  - Add `bract.core.config/apply-inducer`
  - [BREAKING CHANGE] Drop `bract.core.config/resolve-config-filenames`
  - [BREAKING CHANGE] Drop `bract.core.config/run-app`
  - Use own logger when reading config using Keypin
  - [BREAKING CHANGE] Use config parent key `"parent.config.filenames"` when reading config
- Inducers (namespace `bract.core.inducer`)
  - Add `set-verbosity`
  - Add `read-config`
  - Add `run-inducers`
- Keypin
  - Upgrade to Keypin `0.6.0`


### 0.1.0 / 2017-April-25

- Features
  - CLI entry point
  - Inducer functions
  - Development/test support
- CLI commands
  - Run
  - Dry run
  - Print config
  - Clojure REPL
- Inducer functions
  - Context hook
  - Config hook
  - Export as system properties
  - Un-export system properties
  - Launcher invocation (conditional)
  - De-initialization
