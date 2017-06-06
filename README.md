# bract.core

Bract is a multi-purpose, modular Clojure application initialization framework.

The _bract.core_ module implements the essential functionality and is a common dependency for all modules.

**Early days. Expect breaking changes!**

_**Requires Clojure 1.7 or higher, Java 7 or higher.**_


### Rationale

Application development could be a blissful experience if we only have to focus on the application logic, and not worry
about configuration, initialization and the sundry development and maintenance tasks applications frequently need. This
should be a solved problem.


### What is Bract?

Bract aims to be the minimal glue to bind the following pieces together:

- Application entry-point
- Application configuration
- Application initialization and launching
- Extensions to _bract.core_ via Bract modules
- Integration with libraries/frameworks via Bract modules

[Keypin](https://github.com/kumarshantanu/keypin) is the only _bract.core_ dependency, for config support. While
_bract.core_ is low level and prescribes no style, it is possible to author opinionated Bract modules on top of it.
One may even extend Bract to build a custom application framework. Bract is suited for various types of applications,
e.g. command-line tools, web services, batch jobs, web applications etc. and integrates well with tangential, yet
relevant aspects such as configurable JVM logging.


## Usage

Leiningen coordinates: `[bract/bract.core "0.3.0-SNAPSHOT"]`

For documentation refer demo applications.


## License

Copyright © 2017 Shantanu Kumar (kumar.shantanu@gmail.com, shantanu.kumar@concur.com)

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
