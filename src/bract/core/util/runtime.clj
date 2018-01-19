;   Copyright (c) Shantanu Kumar. All rights reserved.
;   The use and distribution terms for this software are covered by the
;   Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
;   which can be found in the file LICENSE at the root of this distribution.
;   By using this software in any fashion, you are agreeing to be bound by
;   the terms of this license.
;   You must not remove this notice, or any other, from this software.


(ns bract.core.util.runtime
  (:require
    [bract.core.util :as u])
  (:import
    [java.io File]
    [java.lang.management ManagementFactory OperatingSystemMXBean]))


(def sunext-osinfo
  (if (try (Class/forName "com.sun.management.OperatingSystemMXBean")
        (catch ClassNotFoundException e))
    (do
      (require 'bract.core.util.sunext)
      ;; assuming Oracle JVM - os-bean being com.sun.management.OperatingSystemMXBean
      ;; dynamically call (bract.util.runtime.sunext/osinfo os-bean)
      (-> (find-ns 'bract.core.util.sunext)
        (ns-resolve 'osinfo)
        deref))
    (constantly {})))


(let [;; load-time constants
      startms (u/now-millis)
      runtime (Runtime/getRuntime)
      os-bean (ManagementFactory/getOperatingSystemMXBean)
      rt-bean (ManagementFactory/getRuntimeMXBean)]
  (defn sysinfo
    "Return system information map."
    []
    (let [app-uptime-ms  (u/now-millis startms)
          jvm-uptime-ms  (.getUptime rt-bean)
          free-heap-mem  (.freeMemory  runtime)
          max-heap-mem   (.maxMemory   runtime)
          total-heap-mem (.totalMemory runtime)]
      (-> (sorted-map-by #(compare (u/as-str %1) (u/as-str %2)))  ; empty map that remains sorted after assoc/merge
        (assoc
          ;; hardware
          :processor-count       (.availableProcessors runtime)
          ;; os
          :os-name               (.getName os-bean)
          :os-arch               (.getArch os-bean)
          :os-version            (.getVersion os-bean)
          :system-load-avg       (.getSystemLoadAverage os-bean)
          ;; user
          :user-dir              (System/getProperty "user.dir")
          :user-home             (System/getProperty "user.home")
          :user-name             (System/getProperty "user.name")
          ;; java
          :java-class-path       (System/getProperty "java.class.path")
          :java-home             (System/getProperty "java.home")
          :java-vendor           (System/getProperty "java.vendor")
          :java-version          (System/getProperty "java.version")
          ;; startup
          :jvm-launch-args       (vec (.getInputArguments rt-bean))
          ;; uptime
          :app-uptime-millis     app-uptime-ms
          :app-uptime-string     (u/millis->str app-uptime-ms)
          :jvm-uptime-millis     jvm-uptime-ms
          :jvm-uptime-string     (u/millis->str jvm-uptime-ms)
          ;; heap
          :jvm-heap-free-nbytes  free-heap-mem
          :jvm-heap-free-string  (u/nbytes->str free-heap-mem)
          :jvm-heap-max-nbytes   max-heap-mem
          :jvm-heap-max-string   (u/nbytes->str max-heap-mem)
          :jvm-heap-total-nbytes total-heap-mem
          :jvm-heap-total-string (u/nbytes->str total-heap-mem)
          ;; disk
          :disk-info             (->> (File/listRoots)
                                   (mapv (fn [^File root]
                                           {:path   (.getAbsolutePath root)
                                            :total  (u/nbytes->str (.getTotalSpace root))
                                            :free   (u/nbytes->str (.getFreeSpace root))
                                            :usable (u/nbytes->str (.getUsableSpace root))}))))
        (merge (sunext-osinfo os-bean))))))


(defn runtime-info
  "Return system info merged with information gathered from info generator fns, each being (fn [])."
  [info-fns]
  (reduce (fn [m f]
            (let [r (try (f)
                      (catch Exception e
                        {(str f) (str e)}))]
              (conj m (if (map? r)
                        r
                        {(str f) r}))))
    (sysinfo) info-fns))


(defn sysinfo-extension-middleware
  "Given an extender fn `(fn []) -> map` extend the sysinfo function to merge results."
  ([extender]
    (sysinfo-extension-middleware sysinfo extender))
  ([info-generator extender]
    (fn []
      (merge (info-generator)
        (try
          (let [extn-data (extender)]
            (if (map? extn-data)
              extn-data
              {:extension extn-data}))
          (catch Exception e
            {:extension (str e)}))))))
