;   Copyright (c) Shantanu Kumar. All rights reserved.
;   The use and distribution terms for this software are covered by the
;   Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
;   which can be found in the file LICENSE at the root of this distribution.
;   By using this software in any fashion, you are agreeing to be bound by
;   the terms of this license.
;   You must not remove this notice, or any other, from this software.


(ns bract.core.dev-init
  "This namespace is solely for DEV mode initialization, typically suited for running tests from the command-line.
  To use this, simply 'require' it in the 'ns' macro of the namespace that expects DEV mode initialization:

  ```clojure
  (ns myapp
    (:require bract.core.dev-init))
  ```

  A more elaborate (contrived) example is below:

  ```clojure
  (ns mycorp.myapp.foo-test
    (:require
      [mycorp.myapp.foo :as f]
      [clojure.test :refer [deftest is testing]]
      bract.core.dev-init))
  ```

  In the snippets above the ns block ensures the initialized DEV environment by referring to `bract.core.dev-init`."
  (:require
    [clojure.java.io   :as io]
    [bract.core.dev    :as core-dev]
    [bract.core.keydef :as kdef]
    [bract.core.util   :as util]))


(let [context-file (kdef/ctx-context-file (core-dev/initial-context))]
  (if (and (string? context-file)
        (or (io/resource context-file)
          (.exists (io/file context-file))))
    (core-dev/ensure-init)
    (util/err-print-banner
      "ERROR: Context file" (pr-str context-file)
      "not found in classpath or filesystem - ignoring DEV initialization")))
