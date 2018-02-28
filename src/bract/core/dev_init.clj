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

  (ns myapp
    (:require bract.core.dev-init))

  A more elaborate (contrived) example is below:

  (ns mycorp.myapp.foo-test
    (:require
      [mycorp..myapp.foo :as f]
      [clojure.test :refer [deftest is testing]]
      bract.core.dev-init))

  In the snippets above the ns block ensures the initialized DEV environment by referring to `bract.core.dev-init`."
  (:require
    [bract.core.dev :as core-dev]))


(core-dev/ensure-init)