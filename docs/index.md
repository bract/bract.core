# bract.core documentation

This page is about _bract.core_ only. Refer [Bract documentation](https://bract.github.io/about.html#how-it-works)
for the fundamental concepts in Bract.

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

All inducers exposed by _bract.core_ as in the namespace `bract.core.inducer`. A summary is below. Input context key
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
