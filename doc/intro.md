# Documentation for bract.core

Bract is a functional application initialization and de-initilizaion framework
based around the idea of context and inducers, explained in the sections below.
Bract follows a dynamic, configurable approach with provided defaults and hooks
so that you can override and customize any aspect of the app-runtime lifecycle.


## Context

A context is a map that represents the state of application initialization or
de-initialization at any given point of time. An initial context is rolled
through several inducers (see section below) to complete the process. 

The context may have well known keys from various Bract modules and application.


## Inducers

An inducer is a function having the following characteristics:

- An inducer is a function of arity one `(fn [context])` or more `(fn [context & more])`
- An inducer always receives a context map as its first argument
- An inducer always returns updated or unchanged context map


### Inducer example

Below is a hello-world inducer function that returns the context map unchanged:

```clojure
(defn hello-world-inducer
  [context]
  (println "Hello World!")
  context)
```


### Inducers are chained

An inducer performs a specific unit of work during the initialization or
de-initialization of a Bract application. Inducers are usually executed in a
sequence, and often nested sub-sequences. That is, an initial context map is
rolled through a sequence of inducers to arrive at the final value. To avoid
executing the remaining inducers in a chain, you may return `(reduced context)`
from an inducer.

Inducers work with well known context and config keys, which are explained in
the sections below. You may override or update the default values of the
context keys in your application. Similarly, you may override the default
values of the config keys as you see fit in your application.

Based on the entry point of the application (e.g. main, script execution, DEV
mode, unit testing etc.) the inducer chains may be dynamic and different in
order to suit various purposes.


## Context keys

The context keys defined by _bract.core_ are listed below. Note that these may
be referred to and used by other modules too.

Legend:

    - FQFN: Fully qualified function name (string or symbol)
    - FNable: Function or FQFN
    - FNable-0: Zero arity function or FQFN

| Context key                | Value type        | Description                                               |
|----------------------------|-------------------|-----------------------------------------------------------|
|`:bract.core/verbose?`      |boolean            | controls verbosity, value could be `true` or `false`      |
|`:bract.core/context-file`  |string             | EDN filename containing context map                       |
|`:bract.core/config-files`  |vector of string   | vector of (or comma separated) config file names          |
|`:bract.core/exit?`         |boolean            | whether break out of all inducer levels                   |
|`:bract.core/cli-args`      |vector of string   | collection of CLI arguments                               |
|`:bract.core/config`        |app config map     | application configuraton map                              |
|`:bract.core/inducers`      |vector of FQFNs    | vector of inducer functions or their fully qualified names|
|`:bract.core/deinit`        |vector of `(fn [])`| functions `[(fn []) ..]` to de-initialize the app         |
|`:bract.core/launch?`       |boolean            | whether invoke launcher function, `true` or `false`       |
|`:bract.core/launchers`     |vector of FQFNs    | fully qualified launcher function names                   |
|`:bract.core/stopper`       |function `(fn [])` | function `(fn [])` to stop the started application        |
|`:bract.core/health-check`  |vector of `(fn [])`| health check functions `[(fn []) ..]`                     |
|`:bract.core/runtime-info`  |vector of `(fn [])`| runtime-info functions `[(fn []) ..]`                     |
|`:bract.core/alive-tstamp`  |derefable `(fn [])`| derefable `(fn [])`: alive timestamp in milliseconds      |
|`:bract.core/app-exit-code` |integer            | application exit code (int)                               |
|`:bract.core/*shutdown-flag`|volatile of boolean| volatile: shutdown begun?                                 |
|`:bract.core/shutdown-hooks`|vector of `Thread`s| added shutdown hook threads                               |


## Config keys

The config keys defined by _bract.core_ are listed below. Note that these may
be referred to and used by other modules too.

| Config key                 | Value type     | Description                                           |
|----------------------------|----------------|-------------------------------------------------------|
|`"bract.core.inducers"`     |vector of FQFNs |vector of fully qualified inducer fn names             |
|`"bract.core.exports"`      |vector of string|vector of config keys to export as system properties   |
|`"bract.core.drain.timeout"`|Keypin duration |workload drain timeout duration, e.g. `[10000 :millis]`|
