;   Copyright (c) Shantanu Kumar. All rights reserved.
;   The use and distribution terms for this software are covered by the
;   Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
;   which can be found in the file LICENSE at the root of this distribution.
;   By using this software in any fashion, you are agreeing to be bound by
;   the terms of this license.
;   You must not remove this notice, or any other, from this software.


(ns bract.core.main-test
  (:require
    [clojure.test :refer :all]
    [bract.core.inducer :as core-inducer]
    [bract.core.main    :as main]))


(defn inc-inducer
  [context]
  (update context :inc-target inc))


(deftest test-main
  (testing "no arg, one pre-configured inducer"
    (let [context (core-inducer/induce
                    {:inc-target 0
                     :bract.core/inducers [inc-inducer]}
                    main/root-inducers)]
      (is (= 1 (:inc-target context)))))
  (testing "no custom inducer"
    (let [context (core-inducer/induce
                    {:inc-target 0}
                    main/root-inducers)]
      (is (= 0 (:inc-target context))))))
