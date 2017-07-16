;   Copyright (c) Shantanu Kumar. All rights reserved.
;   The use and distribution terms for this software are covered by the
;   Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
;   which can be found in the file LICENSE at the root of this distribution.
;   By using this software in any fashion, you are agreeing to be bound by
;   the terms of this license.
;   You must not remove this notice, or any other, from this software.


(ns bract.core.keydef-test
  (:require
    [clojure.test :refer :all]
    [bract.core.keydef :as kdef]))


(deftest context-test
  (testing "happy cases"
    (let [good-context {:bract.core/verbose? false
                        :bract.core/config-files ["foo.edn" "bar.properties"]
                        :bract.core/exit? :yes
                        :bract.core/cli-args ["-c" "run"]
                        :bract.core/config {"foo" "bar"}
                        :bract.core/launch? true}]
      (is (false? (kdef/ctx-verbose? good-context)))
      (is (= ["foo.edn" "bar.properties"] (kdef/ctx-config-files good-context)))
      (is (= :yes (kdef/ctx-exit? good-context)))
      (is (= ["-c" "run"] (kdef/ctx-cli-args good-context)))
      (is (= {"foo" "bar"} (kdef/ctx-config good-context)))
      (is (= true (kdef/ctx-launch? good-context)))
      (is (false? (kdef/ctx-launch? {})) "missing/default value")))
  (testing "default values"
    (is (false? (kdef/ctx-verbose? {})))
    (is (= [] (kdef/ctx-config-files {})))
    (is (false? (kdef/ctx-exit? {}))))
  (testing "missing/bad context keys"
    (let [bad-context {:bract.core/config "foobar"
                        :bract.core/launch? 10}]
      (is (thrown? IllegalArgumentException (kdef/ctx-config  bad-context)) "invalid value")
      (is (thrown? IllegalArgumentException (kdef/ctx-config  {}))          "missing value")
      (is (thrown? IllegalArgumentException (kdef/ctx-launch? bad-context)) "invalid value"))))


(deftest config-test
  (testing "happy tests"
    (doseq [good-config [{"bract.core.inducers" ["foo.bar.baz.qux/fred"
                                                 "mary.had.a.little/lamb"]
                          "bract.core.exports" ["foo"
                                                "bar"]
                          "bract.core.launcher" 'bract.core.inducer/apply-inducer}
                         {"bract.core.inducers" "[\"foo.bar.baz.qux/fred\"
                                                   \"mary.had.a.little/lamb\"]"
                          "bract.core.exports"  "[\"foo\"
                                                   \"bar\"]"
                          "bract.core.launcher" "bract.core.inducer/apply-inducer"}]]
      (is (= ["foo.bar.baz.qux/fred"
              "mary.had.a.little/lamb"]
            (kdef/cfg-inducers good-config)))
      (is (= ["foo" "bar"]
            (kdef/cfg-exports good-config)))
      (is (some?
            (kdef/cfg-launcher good-config)))))
  (testing "missing/bad context keys"
    (let [bad-context {"bract.core.inducers" {:foo :bar}
                       "bract.core.context-hook" 10
                       "bract.core.config-hook"  15
                       "bract.core.exports"  20
                       "bract.core.launcher" false}]
      (is (thrown? IllegalArgumentException (kdef/cfg-inducers      bad-context)))
      (is (thrown? IllegalArgumentException (kdef/cfg-inducers      {})) "missing key")
      (is (thrown? IllegalArgumentException (kdef/cfg-exports       bad-context)))
      (is (thrown? IllegalArgumentException (kdef/cfg-exports       {})) "missing key")
      (is (thrown? IllegalArgumentException (kdef/cfg-launcher      bad-context)))
      (is (thrown? IllegalArgumentException (kdef/cfg-launcher      {})) "missing key"))))
