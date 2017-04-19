# TODO and Change Log

## TODO

- [TODO] Support for parameterized (or prefixed/qualified) config
- [TODO] Implicit config-reading stage: file `bract/init.edn` falling back to `bract/core/init.edn`
  - [TODO] The implicit filename should be a volatile field in a Java class, so that Java entry-points can access
- [TODO] Factor out CLI as a module
  - [TODO] Allow custom permitted CLI commands and command-handlers via config
- [TODO] CLI should only populate config filenames, a stage should load config from those config files
- [TODO] Support for parsing `project.clj`
  - [TODO] Discover and provide application version
  - [TODO] Discover and provide Bract (core and modules) version
- [TODO] Support for application shutdown (hook) cleanup
  - As a mandatory/optional callback
  - As a mandatory/optional configured fn
- [TODO] Support for uncaught exception handler
  - As a mandatory/optional callback
  - As a mandatory/optional configured fn


## [WIP] 0.1.0 / [Unreleased]

### Added

- Features
  - CLI entry point
  - Inducer functions
- CLI commands
  - Run
  - Dry run
  - Print config
  - Clojure REPL
- Inducer functions
  - Export as system properties
  - Conditional launcher invocation
