# bract.core TODO and Change Log

## Ideas and TODO

- [TODO] Dev triggers should accept an optional param
  - Init a sub-system
  - De-init a sub-system
  - Launch a sub-system
  - etc.
- [TODO] Support for parameterized (or prefixed/qualified) config
- [TODO] Support for parsing `project.clj`
  - [TODO] Discover and provide application version
  - [TODO] Discover and provide Bract (core and modules) version

## [WIP] 0.3.0 / 2017-June-??

- Allow inducers to accept additional arguments other than context
  - Supported inducer spec: string, symbol, vector, map, var
  - Inducers are now backed by `bract.core.type/Inducer` protocol
  - [BREAKING CHANGE] Remove inducer related functions from the `bract.core.config` namespace
    - `bract.core.config/apply-inducer`
    - `bract.core.config/apply-inducer-by-name`
  - [BREAKING CHANGE] Remove inducer related functions from the `bract.core.util` namespace
    - `bract.core.util/apply-inducer`
    - `bract.core.util/induce`
  - Introduce functions in the `bract.core.inducer` namespace to apply inducers
    - `bract.core.inducer/apply-inducer` for functions (for direct internal calls to induce)
    - `bract.core.inducer/apply-inducer-by-key` for named inducers (fully qualified fn names)
    - `bract.core.inducer/induce` for applying a collection of inducers
- Replace default dev config file `config.dev.edn` with `config/config.dev.edn`
- [BREAKING CHANGE] Rename `bract.core.inducer/run-inducers` to `run-config-inducers`
- Introduce `bract.core.inducer/run-context-inducers` to run inducers from context
  - Context key definition `:bract.core/inducers`
- [TODO] Support for application shutdown (hook) cleanup
  - As a mandatory/optional callback
  - As a mandatory/optional configured fn
- [TODO] Support for uncaught exception handler
  - As a mandatory/optional callback
  - As a mandatory/optional configured fn


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
