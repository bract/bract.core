;   Copyright (c) Shantanu Kumar. All rights reserved.
;   The use and distribution terms for this software are covered by the
;   Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
;   which can be found in the file LICENSE at the root of this distribution.
;   By using this software in any fashion, you are agreeing to be bound by
;   the terms of this license.
;   You must not remove this notice, or any other, from this software.


(ns bract.core.inducer-test
  (:require
    [clojure.test :refer :all]
    [bract.core.config  :as config]
    [bract.core.inducer :as inducer]))


(def volatile-holder (volatile! nil))


(defn update-volatile-holder
  [x]
  (vreset! volatile-holder x))


(deftest test-context-hook!
  (let [context {:bract.core/config {"bract.core.context-hook.enabled" true
                                     "bract.core.context-hook" "bract.core.inducer-test/update-volatile-holder"}}]
    (vreset! volatile-holder nil)
    (inducer/context-hook! context)
    (is (= context @volatile-holder))))


(deftest test-config-hook!
  (let [config {"bract.core.config-hook.enabled" true
                "bract.core.config-hook" "bract.core.inducer-test/update-volatile-holder"}
        context {:bract.core/config config}]
    (vreset! volatile-holder nil)
    (inducer/config-hook! context)
    (is (= config @volatile-holder))))


(deftest test-export-as-sysprops
  (let [context {:bract.core/config {"bract.core.exports" ["foo"
                                                           "bar"]
                                     "foo" "foo10"
                                     "bar" "bar20"
                                     "baz" "baz30"}}]
    (testing "export"
      (is (nil? (System/getProperty "foo")))
      (is (nil? (System/getProperty "bar")))
      (inducer/export-as-sysprops context)
      (is (= "foo10" (System/getProperty "foo")))
      (is (= "bar20" (System/getProperty "bar"))))
    (testing "unexport"
      (inducer/unexport-sysprops context)
      (is (nil? (System/getProperty "foo")))
      (is (nil? (System/getProperty "bar"))))))


(def launcher-store (volatile! 0))


(defn launcher-inc
  [context]
  (vswap! launcher-store #(inc ^long %)))


(deftest test-invoke-launcher
  (vreset! launcher-store 0)
  (inducer/invoke-launcher {:bract.core/launch? false})
  (is (zero? @launcher-store))
  (inducer/invoke-launcher {:bract.core/launch? true
                            :bract.core/config {"bract.core.launcher" "bract.core.inducer-test/launcher-inc"}})
  (is (= 1 @launcher-store)))
