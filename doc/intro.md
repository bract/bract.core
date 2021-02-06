# Introduction to bract.core

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


## Creating a simple demo application

In this quickstart example, we will setup a "Greeting" demo application that simply prints "Hello, world!"
using a configured greeting word ("Hello"). Let us create this application using the _bract.core_ module.

The instructions below are for [Leiningen](https://leiningen.org/) users.


### Create the application skeleton

```
lein new app greeting
cd greeting
```

Now, you need to make some changes to the `project.clj` file:

1. The key `:dependencies` lists `org.clojure/clojure` as the only dependency. Add `bract/bract.core` to it.
2. Update the `:profiles` entry to reflect the application entry point as follows:

```clojure
  :profiles {:dev {:main ^:skip-aot bract.core.dev
                   :repl-options {:init-ns bract.core.dev}}
             :uberjar {:aot [bract.core.main]
                       :main ^:skip-aot bract.core.main}}
```


### Setup Bract context

Create a file `resources/bract-context.edn` with following content:

```edn
{:bract.core/inducers  [(bract.core.inducer/run-context-inducers :app/main-inducers)
                        (bract.core.inducer/run-context-inducers :app/dev-inducers)]

 :app/main-inducers    [(clojure.core/assoc :bract.core/launch? true)]
 :app/dev-inducers     []

 :bract.core/launchers [greeting.core/main]

 ;; configuration
 :greeting-word        "Namaste"
 }
```


### Write the main code

Open the file `src/greeting/core.clj` and edit as follows:

```clojure
(ns greeting.core)

(defn main [context]
  (let [gw (:greeting-word context)]
    (printf "%s, world!\n" gw))
  (flush)
  context)
```

Notice that the `(:gen-class)` directive is removed from the `ns` block, and the `-main` function is removed too.
Because this namespace is no more the application entry point, though Bract calls `greeting.core/main` as a launcher.

At this point you can run the application using `lein run` or `APP_VERBOSE=true lein run` command. The latter enables
a verbose output of what Bract is doing under the hood.


### Setup DEV mode

Let us try to setup unit testing for this app. For DEV mode, create a file `test/bract-context.dev.edn` with the
following content:

```edn
{;; inherit everything from bract-context.edn
 "parent.filenames" "bract-context.edn"

 ;; override entries for DEV mode
 :app/main-inducers []
 :app/dev-inducers  [bract.core.dev/record-context!]
 }
```


### Write a unit test

Edit the test file `test/greeting/core_test.clj` as follows:

```clojure
(ns greeting.core-test
  (:require [clojure.test :refer :all]
            [greeting.core :refer :all]
            [bract.core.dev :as dev]
            bract.core.dev-init))

(deftest a-test
  (is (= "Namaste, world!\n"
        (with-out-str (main dev/app-context)))))
```

You can run the test with `lein do clean, test` or `APP_VERBOSE=true lein do clean, test`.


### Working at the REPL

Run `lein repl` to start a REPL:

```clojure
(help)   ; show REPL help text
(start)  ; launch the application
;; bract.core.dev/app-context is now bound to initialized context
```


### Package and run application as an UberJAR

```shell
lein do clean, uberjar
java -jar target/uberjar/greeting-0.1.0-SNAPSHOT-standalone.jar
```
