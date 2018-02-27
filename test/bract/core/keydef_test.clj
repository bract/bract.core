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
    [keypin.type       :as kptype]
    [keypin.util       :as kputil]
    [bract.core.keydef :as kdef]
    [bract.core.util   :as util]))


(deftest context-test
  (testing "happy cases"
    (let [good-context {:bract.core/verbose?       false
                        :bract.core/context-file   "foo.edn"
                        :bract.core/config-files   ["foo.edn" "bar.properties"]
                        :bract.core/exit?          :yes
                        :bract.core/cli-args       ["-c" "run"]
                        :bract.core/config         {"foo" "bar"}
                        :bract.core/inducers       '[foo.bar
                                                     [bar.baz qux]]
                        :bract.core/deinit         '[foo bar]
                        :bract.core/launch?        true
                        :bract.core/stopper        (fn [] :stopped)
                        :bract.core/health-check   [(fn [] {:id :mysql :status :degraded})
                                                    (fn [] {:id :cache :status :healthy})]
                        :bract.core/runtime-info   [(fn [] {:foo 10})
                                                    (fn [] {:bar 20})]
                        :bract.core/alive-tstamp   (util/alive-millis)
                        :bract.core/jvm-exit-code  10
                        :bract.core/*shutdown-flag (volatile! false)
                        :bract.core/shutdown-hooks [(fn [] :hook1)]}]
      (is (false?                         (kdef/ctx-verbose?       good-context)))
      (is (= "foo.edn"                    (kdef/ctx-context-file   good-context)))
      (is (= ["foo.edn" "bar.properties"] (kdef/ctx-config-files   good-context)))
      (is (= :yes                         (kdef/ctx-exit?          good-context)))
      (is (= ["-c" "run"]                 (kdef/ctx-cli-args       good-context)))
      (is (= {"foo" "bar"}                (kdef/ctx-config         good-context)))
      (is (vector?                        (kdef/ctx-inducers       good-context)))
      (is (= true                         (kdef/ctx-launch?        good-context)))
      (is (fn?                            (kdef/ctx-stopper        good-context)))
      (is (vector?                        (kdef/ctx-health-check   good-context)))
      (is (vector?                        (kdef/ctx-runtime-info   good-context)))
      (is (ifn?                           (kdef/ctx-alive-tstamp   good-context)))
      (is (= 10                           (kdef/ctx-jvm-exit-code  good-context)))
      (is (volatile?                      (kdef/*ctx-shutdown-flag good-context)))
      (is (vector?                        (kdef/ctx-shutdown-hooks good-context)))))
  (testing "default values"
    (is (false?       (kdef/ctx-verbose?       {})))
    (is (nil?         (kdef/ctx-context-file   {})))
    (is (= []         (kdef/ctx-config-files   {})))
    (is (= []         (kdef/ctx-inducers       {})))
    (is (false?       (kdef/ctx-exit?          {})))
    (is (= []         (kdef/ctx-deinit         {})))
    (is (false?       (kdef/ctx-launch?        {})))
    (is (vector?      (kdef/ctx-health-check   {})))
    (is (vector?      (kdef/ctx-runtime-info   {})))
    (is (ifn?         (kdef/ctx-alive-tstamp   {})))
    (is (nil?         (kdef/ctx-jvm-exit-code  {})))
    (is (volatile?    (kdef/*ctx-shutdown-flag {})))
    (is (false?      @(kdef/*ctx-shutdown-flag {})))
    (is (vector?      (kdef/ctx-shutdown-hooks {})))
    (is (= []         (kdef/ctx-shutdown-hooks {}))))
  (testing "missing values"
    (is (thrown? IllegalArgumentException (kdef/ctx-cli-args  {})))
    (is (thrown? IllegalArgumentException (kdef/ctx-config    {}))))
  (testing "bad values"
    (let [bad-context {:bract.core/verbose?       10
                       :bract.core/context-file   10
                       :bract.core/config-files   10
                       :bract.core/cli-args       10
                       :bract.core/config         "foobar"
                       :bract.core/inducers       10
                       :bract.core/deinit         10
                       :bract.core/launch?        10
                       :bract.core/stopper        10
                       :bract.core/health-check   10
                       :bract.core/runtime-info   10
                       :bract.core/alive-tstamp   10
                       :bract.core/jvm-exit-code  "foo"
                       :bract.core/*shutdown-flag 10
                       :bract.core/shutdown-hooks 10}]
      (is (thrown? IllegalArgumentException (kdef/ctx-verbose?       bad-context)))
      (is (thrown? IllegalArgumentException (kdef/ctx-context-file   bad-context)))
      (is (thrown? IllegalArgumentException (kdef/ctx-config-files   bad-context)))
      (is (thrown? IllegalArgumentException (kdef/ctx-cli-args       bad-context)))
      (is (thrown? IllegalArgumentException (kdef/ctx-config         bad-context)))
      (is (thrown? IllegalArgumentException (kdef/ctx-inducers       bad-context)))
      (is (thrown? IllegalArgumentException (kdef/ctx-deinit         bad-context)))
      (is (thrown? IllegalArgumentException (kdef/ctx-launch?        bad-context)))
      (is (thrown? IllegalArgumentException (kdef/ctx-stopper        bad-context)))
      (is (thrown? IllegalArgumentException (kdef/ctx-health-check   bad-context)))
      (is (thrown? IllegalArgumentException (kdef/ctx-runtime-info   bad-context)))
      (is (thrown? IllegalArgumentException (kdef/ctx-alive-tstamp   bad-context)))
      (is (thrown? IllegalArgumentException (kdef/ctx-jvm-exit-code  bad-context)))
      (is (thrown? IllegalArgumentException (kdef/ctx-jvm-exit-code  {:bract.core/jvm-exit-code -10})))
      (is (thrown? IllegalArgumentException (kdef/*ctx-shutdown-flag bad-context)))
      (is (thrown? IllegalArgumentException (kdef/ctx-shutdown-hooks bad-context))))))


(deftest config-test
  (testing "happy tests"
    (doseq [good-config [{"bract.core.inducers"      ["foo.bar.baz.qux/fred"
                                                      "mary.had.a.little/lamb"]
                          "bract.core.exports"       ["foo"
                                                      "bar"]
                          "bract.core.launcher"      'bract.core.inducer/apply-inducer
                          "bract.core.drain.timeout" [5 :seconds]}
                         {"bract.core.inducers"      "[\"foo.bar.baz.qux/fred\"
                                                       \"mary.had.a.little/lamb\"]"
                          "bract.core.exports"       "[\"foo\"
                                                       \"bar\"]"
                          "bract.core.launcher"      "bract.core.inducer/apply-inducer"
                          "bract.core.drain.timeout" "5 seconds"}]]
      (is (= ["foo.bar.baz.qux/fred"
              "mary.had.a.little/lamb"] (kdef/cfg-inducers      good-config)))
      (is (= ["foo" "bar"]              (kdef/cfg-exports       good-config)))
      (is (some?                        (kdef/cfg-launcher      good-config)))
      (is (some?                        (kdef/cfg-drain-timeout good-config)))))
  (testing "default values"
    (is (= 10000 (kptype/millis (kdef/cfg-drain-timeout {})))))
  (testing "missing values"
    (is (thrown? IllegalArgumentException (kdef/cfg-inducers {})))
    (is (thrown? IllegalArgumentException (kdef/cfg-exports  {})))
    (is (thrown? IllegalArgumentException (kdef/cfg-launcher {}))))
  (testing "missing/bad config entries"
    (let [bad-context {"bract.core.inducers"     {:foo :bar}
                       "bract.core.exports"      20
                       "bract.core.launcher"     false}]
      (is (thrown? IllegalArgumentException (kdef/cfg-inducers bad-context)))
      (is (thrown? IllegalArgumentException (kdef/cfg-exports  bad-context)))
      (is (thrown? IllegalArgumentException (kdef/cfg-launcher bad-context))))))
