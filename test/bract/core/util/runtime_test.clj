;   Copyright (c) Shantanu Kumar. All rights reserved.
;   The use and distribution terms for this software are covered by the
;   Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
;   which can be found in the file LICENSE at the root of this distribution.
;   By using this software in any fashion, you are agreeing to be bound by
;   the terms of this license.
;   You must not remove this notice, or any other, from this software.


(ns bract.core.util.runtime-test
  (:require
    [clojure.pprint :as pp]
    [clojure.test :refer [deftest is testing]]
    [bract.core.util.runtime :as r]))


(deftest test-sysinfo
  #_(pp/pprint (r/sysinfo))
  (is (map? (r/sysinfo))))


(deftest test-runtime-info
  #_(pp/pprint (r/runtime-info [#(throw (IllegalStateException. "this is a test exception"))
                                #(str "Arity mismatch:" %1 %2)
                                #(do [:foo 20 'bar])
                                #(do {:zindex 30})]))
  (is (map? (r/runtime-info [#(throw (IllegalStateException. "this is a test exception"))
                             #(str "Arity mismatch:" %1 %2)
                             #(do [:foo 20 'bar])
                             #(do {:zindex 30})]))))
