# TODO and Change Log

## TODO

- [TODO] Some inducers may accept additional arguments and argument-sets, to be specified via config files
- [TODO] Support for parameterized (or prefixed/qualified) config
- [TODO] Implicit config-reading stage: file `bract/init.edn` falling back to `bract/core/init.edn`
  - [TODO] The implicit filename should be a volatile field in a Java class, so that Java entry-points can access
  - [TODO] Parent key in config files should be overridable from this implicit config file
  - [TODO] Modules to define standard keys that may be overridden e.g. `b.c.keyname.inducer=bract.core.inducers`
- [TODO] Factor out CLI as a module
  - [TODO] The existing `main` entry point should only insert the command-line args into the context
  - [TODO] The CLI module should read command-line args from the context, then read config and merge into context
  - [TODO] Allow custom permitted CLI commands and command-handlers via config
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
