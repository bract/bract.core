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
    [clojure.test    :refer :all]
    [bract.core.inducer :as inducer]
    [bract.core.util    :as util])
  (:import
    [java.util.concurrent Executor Executors ThreadPoolExecutor]
    [bract.core Echo]))


(defn assoc-foo-10
  [m]
  (assoc m :foo 10))


(deftest test-apply-inducer
  (is (= {:foo 10} (inducer/apply-inducer {} assoc-foo-10)))
  (is (= {:foo 10} (inducer/apply-inducer "random inducer" {} assoc-foo-10)))
  (is (thrown? IllegalArgumentException
        (inducer/apply-inducer {} 'foo/bar)))
  (is (thrown? IllegalArgumentException
        (inducer/apply-inducer {} "foo/bar"))))


(deftest test-apply-inducer-by-key
  (is (= {:foo 10} (inducer/apply-inducer {} 'bract.core.inducer-test/assoc-foo-10)))
  (is (= {:foo 10} (inducer/apply-inducer "random inducer" {} 'bract.core.inducer-test/assoc-foo-10)))
  (is (= {:foo 10} (inducer/apply-inducer {} assoc-foo-10)))
  (is (= {:foo 10} (inducer/apply-inducer "random inducer" {} assoc-foo-10))))


(deftest test-induce
  (is (= {:foo 10 :bar 20} (inducer/induce {} [(fn [m] (assoc m :bar 20)) assoc-foo-10])))
  (is (= {:bar 20} (inducer/induce {} [(fn [m] (reduced (assoc m :bar 20))) assoc-foo-10])))
  (is (= {:foo 10} (inducer/induce {} ['bract.core.inducer-test/assoc-foo-10])))
  (is (= {:bract.core/exit? true}
        (inducer/induce {} [(fn [m] (assoc m :bract.core/exit? true)) assoc-foo-10])))
  (is (= {:qux 30 :bract.core/exit? true}
        (inducer/induce {:qux 30} [(fn [m] (inducer/induce m [(fn [mm] (assoc mm :bract.core/exit? true))
                                                              (fn [x] (assoc x :bar 20))]))
                                   assoc-foo-10]))))


(deftest test-set-verbosity
  (let [verbosity? (Echo/isVerbose)]
    (try
      (Echo/setVerbose false)
      (inducer/set-verbosity {:bract.core/verbose? true})
      (is (true? (Echo/isVerbose)) "Verbosity should be enabled after configuring it as true")
      (finally
        (Echo/setVerbose verbosity?)))))


(deftest test-read-context
  (testing "context file not specified"
    (let [context {:foo :bar}]
      (is (= context
            (inducer/read-context context)))))
  (testing "specified context file"
    (let [context {:bract.core/context-file "sample.edn"}]
      (is (= (merge context (-> "sample.edn" io/resource slurp edn/read-string))
            (inducer/read-context context)))))
  (testing "specified, but absent context file"
    (let [context {:bract.core/context-file "example.edn"}]
      (is (= context
            (inducer/read-context context))))))


(deftest test-read-config
  (let [context {:bract.core/config-files "sample.edn"}]
    (is (= (assoc context
             :bract.core/config (-> "sample.edn" io/resource slurp edn/read-string))
          (inducer/read-config context)))))


(deftest test-run-context-inducers
  (testing "arity 1"
    (let [verbosity? (Echo/isVerbose)
          context {:bract.core/verbose? true
                   :bract.core/inducers ['bract.core.inducer/set-verbosity]}]
      (try
        (Echo/setVerbose false)
        (inducer/run-context-inducers context)
        (is (true? (Echo/isVerbose)) "Verbosity should be enabled after configuring it as true")
        (finally
          (Echo/setVerbose verbosity?)))))
  (testing "arity 2"
    (let [verbosity? (Echo/isVerbose)
          context {:bract.core/verbose? true
                   :bract.core/delegate ['bract.core.inducer/set-verbosity]}]
      (try
        (Echo/setVerbose false)
        (inducer/run-context-inducers context :bract.core/delegate)
        (is (true? (Echo/isVerbose)) "Verbosity should be enabled after configuring it as true")
        (finally
          (Echo/setVerbose verbosity?))))))


(deftest test-run-config-inducers
  (testing "arity 1"
    (let [verbosity? (Echo/isVerbose)
          context {:bract.core/verbose? true
                   :bract.core/config {"bract.core.inducers" ['bract.core.inducer/set-verbosity]}}]
      (try
        (Echo/setVerbose false)
        (inducer/run-config-inducers context)
        (is (true? (Echo/isVerbose)) "Verbosity should be enabled after configuring it as true")
        (finally
          (Echo/setVerbose verbosity?)))))
  (testing "arity 2"
    (let [verbosity? (Echo/isVerbose)
          context {:bract.core/verbose? true
                   :bract.core/config {"bract.core.delegate" ['bract.core.inducer/set-verbosity]}}]
      (try
        (Echo/setVerbose false)
        (inducer/run-config-inducers context "bract.core.delegate")
        (is (true? (Echo/isVerbose)) "Verbosity should be enabled after configuring it as true")
        (finally
          (Echo/setVerbose verbosity?))))))


(def volatile-holder (volatile! nil))


(defn update-volatile-holder
  [x]
  (vreset! volatile-holder x))


(deftest test-context-hook
  (let [context {:bract.core/config {"bract.core.context-hook" "bract.core.inducer-test/update-volatile-holder"}}]
    (testing "context-hook, fqvn"
      (vreset! volatile-holder nil)
      (is (= {:foo 10} (inducer/context-hook {:foo 10} "bract.core.inducer-test/update-volatile-holder")))
      (is (= {:foo 10} @volatile-holder)))
    (testing "context-hook, fn"
      (vreset! volatile-holder nil)
      (is (= {:foo 10} (inducer/context-hook {:foo 10} update-volatile-holder)))
      (is (= {:foo 10} @volatile-holder)))))


(deftest test-config-hook
  (let [config {"bract.core.config-hook" "bract.core.inducer-test/update-volatile-holder"}
        context {:bract.core/config config}]
    (testing "config-hook, fqvn"
      (vreset! volatile-holder nil)
      (inducer/config-hook context "bract.core.inducer-test/update-volatile-holder")
      (is (= config @volatile-holder)))
    (testing "config-hook, fn"
      (vreset! volatile-holder nil)
      (inducer/config-hook context update-volatile-holder)
      (is (= config @volatile-holder)))))


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
  (inducer/invoke-deinit {:bract.core/deinit [(fn [] (vreset! volatile-holder 10))]})
  (is (= 10 @volatile-holder)))


(deftest test-invoke-stopper
  (vreset! volatile-holder 0)
  (inducer/invoke-stopper {})
  (is (zero? @volatile-holder))
  (inducer/invoke-stopper {:bract.core/stopper (fn [] (vswap! volatile-holder (fn [^long x] (inc x))))})
  (is (= 1 @volatile-holder)))


(deftest test-add-shutdown-hook
  (let [hooks (atom [])
        context {:bract.core/shutdown-hooks hooks
                 :bract.core/config {"bract.core.drain.timeout" [1 :seconds]}}]
    (testing "default invocation"
      (try
        (inducer/add-shutdown-hook context)
        (doto ^Thread (last @hooks)
          (.start)
          (.join))
        (.removeShutdownHook ^Runtime (Runtime/getRuntime) (last @hooks))
        (finally
          (swap! hooks pop))))
    (testing "inducer invocation"
      (try
        (inducer/add-shutdown-hook context 'bract.core.inducer/invoke-stopper)
        (doto ^Thread (last @hooks)
          (.start)
          (.join))
        (.removeShutdownHook ^Runtime (Runtime/getRuntime) (last @hooks))
        (finally
          (swap! hooks pop))))))


(deftest test-default-exception-handler
  (let [^Executor pool (Executors/newCachedThreadPool)]
    (try
      (testing "default exception handler"
        (inducer/set-default-exception-handler {})
        (.execute pool #(Integer/parseInt "first test")))
      (testing "custom exception handler"
        (vreset! volatile-holder 0)
        (inducer/set-default-exception-handler {} (fn [t ^Throwable ex]
                                                    (.printStackTrace ex)
                                                    (vswap! volatile-holder (fn [^long x] (inc x)))))
        (.execute pool #(Integer/parseInt "second test"))
        (while (zero? ^long @volatile-holder)
          (Thread/yield)))
      (finally
        (.shutdown ^ThreadPoolExecutor pool)
        (util/set-default-uncaught-exception-handler nil)))))
