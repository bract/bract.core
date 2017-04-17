# bract.core

Bract is a multi-purpose, modular Clojure application framework.

The _bract.core_ module implements the essential functionality and is a common dependency for all modules.


### Why Bract?

The Clojure community prefers libraries over frameworks. Bract respects that notion and aims to be the minimal glue to
bind the following pieces together:

- Application entrypoint (CLI)
- Application configuration
- Application initialization
- Libraries/Frameworks (via Bract modules)

The _bract.core_ module has only few dependencies - [Keypin](https://github.com/kumarshantanu/keypin) for config and
[clojure/tools.cli](https://github.com/clojure/tools.cli) for CLI args. While _bract.core_ is low level and prescribes
no style, it is possible to author opinionated Bract modules on top of it. One may even extend Bract to build a custom
application framework. Bract is suited for various types of applications, e.g. command-line tools, web services, batch
jobs, web applications etc. and integrates well with complex aspects e.g. sophisticated JVM logging.


## Usage

Leiningen coordinates: `[bract/bract.core "0.1.0-SNAPSHOT"]`

For documentation refer demo applications.


## License

Copyright © 2017 Shantanu Kumar (kumar.shantanu@gmail.com, shantanu.kumar@concur.com)

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
