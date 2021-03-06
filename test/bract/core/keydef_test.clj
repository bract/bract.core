;   Copyright (c) Shantanu Kumar. All rights reserved.
;   The use and distribution terms for this software are covered by the
;   Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
;   which can be found in the file LICENSE at the root of this distribution.
;   By using this software in any fashion, you are agreeing to be bound by
;   the terms of this license.
;   You must not remove this notice, or any other, from this software.


(ns bract.core.keydef-test
  (:require
    [clojure.test :refer [deftest is testing]]
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
                        :bract.core/launchers      ['bract.core.inducer/apply-inducer]
                        :bract.core/stopper        (fn [] :stopped)
                        :bract.core/health-check   [(fn [] {:id :mysql :status :degraded})
                                                    (fn [] {:id :cache :status :healthy})]
                        :bract.core/runtime-info   [(fn [] {:foo 10})
                                                    (fn [] {:bar 20})]
                        :bract.core/alive-tstamp   (util/alive-millis)
                        :bract.core/app-exit-code  10
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
      (is (coll?                          (kdef/ctx-launchers      good-context)))
      (is (fn?                            (kdef/ctx-stopper        good-context)))
      (is (vector?                        (kdef/ctx-health-check   good-context)))
      (is (vector?                        (kdef/ctx-runtime-info   good-context)))
      (is (ifn?                           (kdef/ctx-alive-tstamp   good-context)))
      (is (= 10                           (kdef/ctx-app-exit-code  good-context)))
      (is (volatile?                      (kdef/*ctx-shutdown-flag good-context)))
      (is (vector?                        (kdef/ctx-shutdown-hooks good-context)))))
  (testing "default values"
    (is (false?       (kdef/ctx-verbose?       {})))
    (is (string?      (kdef/ctx-context-file   {})))
    (is (= []         (kdef/ctx-config-files   {})))
    (is (= false      (kdef/ctx-dev-mode?      {})))
    (is (some?        (kdef/ctx-event-logger   {})))
    (is (= []         (kdef/ctx-inducers       {})))
    (is (false?       (kdef/ctx-exit?          {})))
    (is (= []         (kdef/ctx-deinit         {})))
    (is (false?       (kdef/ctx-launch?        {})))
    (is (vector?      (kdef/ctx-health-check   {})))
    (is (vector?      (kdef/ctx-runtime-info   {})))
    (is (ifn?         (kdef/ctx-alive-tstamp   {})))
    (is (nil?         (kdef/ctx-app-exit-code  {})))
    (is (volatile?    (kdef/*ctx-shutdown-flag {})))
    (is (false?      @(kdef/*ctx-shutdown-flag {})))
    (is (vector?      (kdef/ctx-shutdown-hooks {})))
    (is (= []         (kdef/ctx-shutdown-hooks {}))))
  (testing "missing values"
    (is (thrown? IllegalArgumentException (kdef/ctx-cli-args  {})))
    (is (thrown? IllegalArgumentException (kdef/ctx-config    {})))
    (is (thrown? IllegalArgumentException (kdef/ctx-launchers {}))))
  (testing "bad values"
    (let [bad-context {:bract.core/verbose?       10
                       :bract.core/context-file   10
                       :bract.core/config-files   10
                       :bract.core/cli-args       10
                       :bract.core/config         "foobar"
                       :bract.core/inducers       10
                       :bract.core/deinit         10
                       :bract.core/launch?        10
                       :bract.core/launchers      false
                       :bract.core/stopper        10
                       :bract.core/health-check   10
                       :bract.core/runtime-info   10
                       :bract.core/alive-tstamp   10
                       :bract.core/app-exit-code  "foo"
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
      (is (thrown? IllegalArgumentException (kdef/ctx-launchers      bad-context)))
      (is (thrown? IllegalArgumentException (kdef/ctx-stopper        bad-context)))
      (is (thrown? IllegalArgumentException (kdef/ctx-health-check   bad-context)))
      (is (thrown? IllegalArgumentException (kdef/ctx-runtime-info   bad-context)))
      (is (thrown? IllegalArgumentException (kdef/ctx-alive-tstamp   bad-context)))
      (is (thrown? IllegalArgumentException (kdef/ctx-app-exit-code  bad-context)))
      (is (thrown? IllegalArgumentException (kdef/ctx-app-exit-code  {:bract.core/app-exit-code "foo"})))
      (is (thrown? IllegalArgumentException (kdef/*ctx-shutdown-flag bad-context)))
      (is (thrown? IllegalArgumentException (kdef/ctx-shutdown-hooks bad-context))))))


(deftest config-test
  (testing "happy tests"
    (doseq [good-config [{"bract.core.inducers"      ["foo.bar.baz.qux/fred"
                                                      "mary.had.a.little/lamb"]
                          "bract.core.exports"       ["foo"
                                                      "bar"]
                          "bract.core.drain.timeout" [5 :seconds]}
                         {"bract.core.inducers"      "[\"foo.bar.baz.qux/fred\"
                                                       \"mary.had.a.little/lamb\"]"
                          "bract.core.exports"       "[\"foo\"
                                                       \"bar\"]"
                          "bract.core.drain.timeout" "5 seconds"}]]
      (is (= ["foo.bar.baz.qux/fred"
              "mary.had.a.little/lamb"] (kdef/cfg-inducers      good-config)))
      (is (= ["foo" "bar"]              (kdef/cfg-exports       good-config)))
      (is (some?                        (kdef/cfg-drain-timeout good-config)))))
  (testing "default values"
    (is (= 10000 (kptype/millis (kdef/cfg-drain-timeout {}))))
    (is (= []    (kdef/cfg-inducers {}))))
  (testing "missing values"
    (is (thrown? IllegalArgumentException (kdef/cfg-exports  {}))))
  (testing "missing/bad config entries"
    (let [bad-config {"bract.core.inducers"     {:foo :bar}
                      "bract.core.exports"      20}]
      (is (thrown? IllegalArgumentException (kdef/cfg-inducers bad-config)))
      (is (thrown? IllegalArgumentException (kdef/cfg-exports  bad-config))))))


(deftest test-event-logger
  (testing "default event logger"
    (let [el (kdef/ctx-event-logger {})]
      (el :event1)
      (el :event2 {:data 20})
      (el "event3" {:foo 40} (Exception. "text"))))
  (testing "custom event logger"
    (let [store   (volatile! {:a1 0 :a2 0 :a3 0})
          context {:bract.core/event-logger (fn
                                              ([name] (vswap! store update :a1 inc))
                                              ([name data] (vswap! store update :a2 inc))
                                              ([name data ex] (vswap! store update :a3 inc)))
                   :bract.core/config {"bract.core.eventlog.enable" true}}
          elogger (kdef/resolve-event-logger context :foo)]
      (elogger :foo)
      (elogger :foo {:bar 20})
      (elogger :foo {:bar 30} (Exception.))
      (is (= {:a1 1 :a2 1 :a3 1}
            @store))))
  (testing "allow and block"
    (let [context1 {:bract.core/config {"bract.core.eventlog.enable" true
                                        "bract.core.eventlog.block"  #{:foo :bar}}}
          context2 {:bract.core/config {"bract.core.eventlog.enable" false
                                        "bract.core.eventlog.allow"  #{:baz}}}]
      (is (= util/nop (kdef/resolve-event-logger context1 :foo)))
      (is (not= util/nop (kdef/resolve-event-logger context1 :baz)))
      (is (= util/nop (kdef/resolve-event-logger context2 :foo)))
      (is (not= util/nop (kdef/resolve-event-logger context2 :baz)))))
  (testing "event-logger that throws"
    (let [context-throws {:bract.core/event-logger (fn [& args] (throw (ex-info "Bad event logger")))
                          :bract.core/config {"bract.core.eventlog.enable" true}}
          context-normal {:bract.core/event-logger println
                          :bract.core/config {"bract.core.eventlog.enable" true}}
          el-throws (kdef/resolve-event-logger context-throws :foo)
          el-normal (kdef/resolve-event-logger context-normal :foo)]
      (is (nil? (el-throws :foo)))
      (is (nil? (el-normal :foo))))))
