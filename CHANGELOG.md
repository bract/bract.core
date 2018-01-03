# bract.core Change Log

## Ideas and TODO

- [TODO] Dev triggers should accept an optional param
  - Init a sub-system
  - De-init a sub-system
  - Launch a sub-system
  - etc.
- [TODO] Support for parameterized (or prefixed/qualified) config
- [TODO] Support for config substitution/reference, via Keypin `0.8.0`
- [TODO] Function to transform every config key, based on `:bract.core/ctx-config-key-xf {:default identity}`
  - Helpful to auto-stringify keys in EDN config files
- [TODO - Dev] Several tasks should accept an optional env key, e.g. `(start :qa)` that looks up env from context
  - Requires env key/alias definition
  - Env key/alias should switch the config file(s)
- [TODO] Metaphor web service framework
  - https://zalando.github.io/restful-api-guidelines/
  - OWASP Top-10 security guidelines


## [WIP] 0.5.0 / 2018-January-??

- [TODO - BREAKING CHANGE] Use parent key `parent.filenames` for both context and config
- [TODO] Shutdown hooks should be invokable at any point
  - By default the hook adds invocation of `deinit` tasks
  - Doesn't that mean the deinit value should be `(atom [tasks])` instead of `[tasks]`?
- [TODO] Do not return context from `bract.core.dev` functions
- [TODO] Add utility function to report system info
- [TODO] Echo for invoke-launcher should include launcher name


## 0.4.1 / 2017-August-08

- Make `bract.core.keydef/ctx-shutdown-flag :bract.core/shutdown-flag` volatile instead of atom
- Fix reverse order of reporting remaining drain time


## 0.4.0 / 2017-August-05

- [BREAKING CHANGE] Remove inducer `bract.core.inducer/config-hook`


## 0.4.0-alpha2 / 2017-August-01

- Merge context from `bract-context.dev.edn` in dev mode when available
- Catch exception and print stack trace in dev mode (due to uncaught handler)
- Key definition for context-inducers now defaults to an empty vector
- Add utility function `pst-when-uncaught-handler` for printing stack trace


## 0.4.0-alpha1 / 2017-July-31

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


## 0.3.1 / 2017-June-30

- Do not abort on inducer exception in dev mode, just rethrow (useful for REPL/reload workflow)
- Add `uuid-str` utility function - unique ID for various purposes
- Move generic Bract text from README to the external documentation
- Add `invoke-stopper` inducer to stop a running application


## 0.3.0 / 2017-June-11

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


## 0.2.0 / 2017-June-04

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


## 0.1.0 / 2017-April-25

### Added

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
