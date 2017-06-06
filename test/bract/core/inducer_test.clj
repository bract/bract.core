;   Copyright (c) Shantanu Kumar. All rights reserved.
;   The use and distribution terms for this software are covered by the
;   Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
;   which can be found in the file LICENSE at the root of this distribution.
;   By using this software in any fashion, you are agreeing to be bound by
;   the terms of this license.
;   You must not remove this notice, or any other, from this software.


(ns bract.core.inducer-test
  (:require
    [clojure.edn     :as edn]
    [clojure.java.io :as io]
    [clojure.test :refer :all]
    [bract.core.config  :as config]
    [bract.core.inducer :as inducer])
  (:import
    [bract.core Echo]))


(deftest test-set-verbosity
  (let [verbosity? (Echo/isVerbose)]
    (try
      (Echo/setVerbose false)
      (inducer/set-verbosity {:bract.core/verbose? true})
      (is (true? (Echo/isVerbose)) "Verbosity should be enabled after configuring it as true")
      (finally
        (Echo/setVerbose verbosity?)))))


(deftest test-read-config
  (let [context {:bract.core/config-files "sample.edn"}]
    (is (= (assoc context
             :bract.core/config (-> "sample.edn" io/resource slurp edn/read-string))
          (inducer/read-config context)))))


(deftest test-run-context-inducers
  (let [verbosity? (Echo/isVerbose)
        context {:bract.core/verbose? true
                 :bract.core/inducers ['bract.core.inducer/set-verbosity]}]
    (try
      (Echo/setVerbose false)
      (inducer/run-context-inducers context)
      (is (true? (Echo/isVerbose)) "Verbosity should be enabled after configuring it as true")
      (finally
        (Echo/setVerbose verbosity?)))))


(deftest test-run-config-inducers
  (let [verbosity? (Echo/isVerbose)
        context {:bract.core/verbose? true
                 :bract.core/config {"bract.core.inducers" ['bract.core.inducer/set-verbosity]}}]
    (try
      (Echo/setVerbose false)
      (inducer/run-config-inducers context)
      (is (true? (Echo/isVerbose)) "Verbosity should be enabled after configuring it as true")
      (finally
        (Echo/setVerbose verbosity?)))))


(def volatile-holder (volatile! nil))


(defn update-volatile-holder
  [x]
  (vreset! volatile-holder x))


(deftest test-context-hook
  (let [context {:bract.core/config {"bract.core.context-hook.enabled" true
                                     "bract.core.context-hook" "bract.core.inducer-test/update-volatile-holder"}}]
    (vreset! volatile-holder nil)
    (inducer/context-hook context)
    (is (= context @volatile-holder))))


(deftest test-config-hook
  (let [config {"bract.core.config-hook.enabled" true
                "bract.core.config-hook" "bract.core.inducer-test/update-volatile-holder"}
        context {:bract.core/config config}]
    (vreset! volatile-holder nil)
    (inducer/config-hook context)
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


(defn launcher-inc
  [context]
  (vswap! volatile-holder #(inc ^long %)))


(deftest test-invoke-launcher
  (vreset! volatile-holder 0)
  (inducer/invoke-launcher {:bract.core/launch? false})
  (is (zero? @volatile-holder))
  (inducer/invoke-launcher {:bract.core/launch? true
                            :bract.core/config {"bract.core.launcher" "bract.core.inducer-test/launcher-inc"}})
  (is (= 1 @volatile-holder)))


(deftest test-deinit
  (vreset! volatile-holder 0)
  (inducer/deinit {:bract.core/deinit (fn [] (vreset! volatile-holder 10))})
  (is (= 10 @volatile-holder)))
