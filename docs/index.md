# bract.core

This documentation is for the _bract.core_ module. Refer [Bract documentation](https://bract.github.io/about.html)
for overall Bract documentation.

Leiningen coordinates: `[bract/bract.core "0.3.1"]`


## Keys

Bract supports specifying fully qualified function names as configuration. We use the abbreviation `FQFN` to imply
_"Fully Qualified Function Name"_ in this section.

### Context keys

| Context key                | Value                      | Description |
|----------------------------|----------------------------|-------------|
| `:bract.core/verbose?`     | Parseable as boolean       | Flag for verbose initialization, env var `APP_VERBOSE`   |
| `:bract.core/config-files` | Parseable as filenames vec | Config filenames (comma separated), env var `APP_CONFIG` |
| `:bract.core/exit?`        | Logical boolean            | Whether break out of all inducer levels (exec control)   |
| `:bract.core/cli-args`     | Vector of CLI args         | Command line arguments                         |
| `:bract.core/config`       | Config map                 | Application config, typically with string keys |
| `:bract.core/inducers`     | Coll of inducer fns/FQVNs  | Inducer fns or their fully qualified names     |
| `:bract.core/deinit`       | Function `(fn [])`         | De-initialization function for the app         |
| `:bract.core/launch?`      | Boolean                    | Whether invoke launcher fn                     |
| `:bract.core/stopper`      | Function `(fn [])`         | Function to stop the started application       |


### Config keys

The application config is placed under the context key `:bract.core/config` (see above).

| Config key                 | Value                      | Description |
|----------------------------|----------------------------|-------------|
| `"bract.core.inducers"`    | Vector of inducer FQVNs    | Inducer fn names |
| `"bract.core.exports"`     | Vector of config keys      | Config keys to export as system properties |
| `"bract.core.launcher"`    | Launcher FQVN              | Launcher fn `(fn [context])` name |


## Inducers

All inducers exposed by _bract.core_ are in the namespace `bract.core.inducer`. A summary is below. Input context key
`:bract.core/config` has been omitted where input config key is specified.

| Inducer function       | Input context keys         | Input config keys       | Output context keys  | Description |
|------------------------|----------------------------|-------------------------|----------------------|-------------|
| `set-verbosity`        | `:bract.core/verbose?`     |                         |                      | Set verbosity as per flag  |
| `read-config`          | `:bract.core/config-files` |                         | `:bract.core/config` | Read config from filenames |
| `run-context-inducers` | `:bract.core/inducers`     |                         |                      | Execute specified inducers |
| `run-config-inducers`  | `:bract.core/config`       | `"bract.core.inducers"` |                      | Execute specified inducers |
| `context-hook`         |                            |                         |                      | Do something with context  |
| `config-hook`          | `:bract.core/config`       |                         |                      | Do something with config   |
| `export-as-sysprops`   |                            | `"bract.core.exports"`  |                      | Export system properties   |
| `unexport-sysprops`    |                            | `"bract.core.exports"`  |                      | Remove system properties   |
| `invoke-launcher`      | `:bract.core/launch?`      | `"bract.core.launcher"` |                      | Launch application         |
| `deinit`               | `:bract.core/deinit`       |                         |                      | De-initialize application  |
| `invoke-stopper`       | `:bract.core/stopper`      |                         |                      | Stop running application   |


## Entry points

This module provides several functions as entry point for REPL based interactive development. These functions are
available in the `bract.core.dev` namespace:

| Function call       | Description |
|---------------------|-------------|
| `(verbose)`         | Return the verbosity override status |
| `(verbose status?)` | Set verbosity override (boolean) for subsequent operations (`nil` clears override) |
| `(config)`          | Return the config filename override |
| `(config filename)` | Set the config filename override for subsequent operations (`nil` clears override) |
| `(init)`            | Initialize application without launching it |
| `(deinit)`          | De-initialize application based on `:bract.core/deinit` key in the context |
| `(start)`           | Start application, i.e. init + launch application |
| `(stop)`            | Stop a started application based on `:bract.core/stopper` key in the context |

**Note:** While these functions are for REPL support, they don't provide code reload feature.
