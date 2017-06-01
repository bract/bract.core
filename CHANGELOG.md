# TODO and Change Log

## TODO

- [TODO] Dev triggers should accept an optional param
  - Init a sub-system
  - De-init a sub-system
  - Launch a sub-system
  - etc.
- [TODO] Some inducers may accept additional arguments and argument-sets, to be specified via config files
  - [TODO] Example: A dev-mode inducer specifies a config key, which points to [] in prod and [...] in dev/testing
- [TODO] Support for parameterized (or prefixed/qualified) config
- [TODO] Support for parsing `project.clj`
  - [TODO] Discover and provide application version
  - [TODO] Discover and provide Bract (core and modules) version
- [TODO] Support for application shutdown (hook) cleanup
  - As a mandatory/optional callback
  - As a mandatory/optional configured fn
- [TODO] Support for uncaught exception handler
  - As a mandatory/optional callback
  - As a mandatory/optional configured fn


## [WIP] 0.2.0 / 2017-June-??

- [TODO - BREAKING CHANGE] Drop `bract.core.cli` namespace (Moved to module `bract.cli`)
- [TODO - BREAKING CHANGE] Drop `bract.core.main` namespace (Moved to module `bract.cli`)
- [TODO - BREAKING CHANGE] Drop `bract.core.dev` namespace (Moved to module `bract.dev`)


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
