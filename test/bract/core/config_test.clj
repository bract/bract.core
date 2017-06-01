;   Copyright (c) Shantanu Kumar. All rights reserved.
;   The use and distribution terms for this software are covered by the
;   Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
;   which can be found in the file LICENSE at the root of this distribution.
;   By using this software in any fashion, you are agreeing to be bound by
;   the terms of this license.
;   You must not remove this notice, or any other, from this software.


(ns bract.core.config-test
  (:require
    [clojure.test :refer :all]
    [bract.core.config :as config]))


(deftest context-test
  (testing "happy cases"
    (let [good-context {:bract.core/verbose? false
                        :bract.core/config-files ["foo.edn" "bar.properties"]
                        :bract.core/cli-args ["-c" "run"]
                        :bract.core/config {"foo" "bar"}
                        :bract.core/launch? true}]
      (is (false? (config/ctx-verbose? good-context)))
      (is (= ["foo.edn" "bar.properties"] (config/ctx-config-files good-context)))
      (is (= ["-c" "run"] (config/ctx-cli-args good-context)))
      (is (= {"foo" "bar"} (config/ctx-config good-context)))
      (is (= true (config/ctx-launch? good-context)))
      (is (false? (config/ctx-launch? {})) "missing/default value")))
  (testing "default values"
    (is (true? (config/ctx-verbose? {})))
    (is (= [] (config/ctx-config-files {}))))
  (testing "missing/bad context keys"
    (let [bad-context {:bract.core/config "foobar"
                        :bract.core/launch? 10}]
      (is (thrown? IllegalArgumentException (config/ctx-config  bad-context)) "invalid value")
      (is (thrown? IllegalArgumentException (config/ctx-config  {}))          "missing value")
      (is (thrown? IllegalArgumentException (config/ctx-launch? bad-context)) "invalid value"))))


(deftest config-test
  (testing "happy tests"
    (doseq [good-config [{"bract.core.inducers" ["foo.bar.baz.qux/fred"
                                                 "mary.had.a.little/lamb"]
                          "bract.core.context-hook" 'bract.core.config/apply-inducer-by-name
                          "bract.core.config-hook"  'bract.core.config/apply-inducer-by-name
                          "bract.core.exports" ["foo"
                                                "bar"]
                          "bract.core.launcher" 'bract.core.config/apply-inducer-by-name}
                         {"bract.core.inducers" "[\"foo.bar.baz.qux/fred\"
                                                   \"mary.had.a.little/lamb\"]"
                          "bract.core.context-hook" "bract.core.config/apply-inducer-by-name"
                          "bract.core.config-hook"  "bract.core.config/apply-inducer-by-name"
                          "bract.core.exports"  "[\"foo\"
                                                   \"bar\"]"
                          "bract.core.launcher" "bract.core.config/apply-inducer-by-name"}]]
      (is (= ["foo.bar.baz.qux/fred"
              "mary.had.a.little/lamb"]
            (config/cfg-inducers good-config)))
      (is (some?
            (config/cfg-context-hook good-config)))
      (is (some?
            (config/cfg-config-hook good-config)))
      (is (= ["foo" "bar"]
            (config/cfg-exports good-config)))
      (is (some?
            (config/cfg-launcher good-config)))))
  (testing "missing/bad context keys"
    (let [bad-context {"bract.core.inducers" {:foo :bar}
                       "bract.core.context-hook" 10
                       "bract.core.config-hook"  15
                       "bract.core.exports"  20
                       "bract.core.launcher" false}]
      (is (thrown? IllegalArgumentException (config/cfg-inducers      bad-context)))
      (is (thrown? IllegalArgumentException (config/cfg-inducers      {})) "missing key")
      (is (thrown? IllegalArgumentException (config/cfg-context-hook  bad-context)))
      (is (thrown? IllegalArgumentException (config/cfg-context-hook  {})) "missing key")
      (is (thrown? IllegalArgumentException (config/cfg-config-hook   bad-context)))
      (is (thrown? IllegalArgumentException (config/cfg-config-hook   {})) "missing key")
      (is (thrown? IllegalArgumentException (config/cfg-exports       bad-context)))
      (is (thrown? IllegalArgumentException (config/cfg-exports       {})) "missing key")
      (is (thrown? IllegalArgumentException (config/cfg-launcher      bad-context)))
      (is (thrown? IllegalArgumentException (config/cfg-launcher      {})) "missing key"))))
