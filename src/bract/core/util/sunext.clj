;   Copyright (c) Shantanu Kumar. All rights reserved.
;   The use and distribution terms for this software are covered by the
;   Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
;   which can be found in the file LICENSE at the root of this distribution.
;   By using this software in any fashion, you are agreeing to be bound by
;   the terms of this license.
;   You must not remove this notice, or any other, from this software.


(ns bract.core.util.sunext
  "Load this namespace only for Sun JVM."
  (:require
    [bract.core.util :as u])
  (:import
    [com.sun.management OperatingSystemMXBean UnixOperatingSystemMXBean]))


(defn osinfo
  "Return operating system stats."
  [^OperatingSystemMXBean os]
  (when (instance? OperatingSystemMXBean os)
    (merge {:os-memory-virtual-committed (u/nbytes->str (.getCommittedVirtualMemorySize os))
            :os-memory-physical-total    (u/nbytes->str (.getTotalPhysicalMemorySize os))
            :os-memory-physical-free     (u/nbytes->str (.getFreePhysicalMemorySize os))
            :os-swap-space-total         (u/nbytes->str (.getTotalSwapSpaceSize os))
            :os-swap-space-free          (u/nbytes->str (.getFreeSwapSpaceSize os))
            :jvm-cpu-usage   (.getProcessCpuLoad os)
            :jvm-cpu-time    (u/millis->str (quot ^long (.getProcessCpuTime os)
                                              (* 1000 1000)))  ; nanos to millis
            :jvm-cpu-percent (let [nano-before (System/nanoTime)
                                   cpu-before  (.getProcessCpuTime os)
                                   _           (u/sleep-millis 500)
                                   cpu-after   (.getProcessCpuTime os)
                                   nano-after  (System/nanoTime)]
                               (if (> nano-after nano-before)
                                 (double
                                   (/ (* 100 (- cpu-after cpu-before))
                                     (- nano-after nano-before)))
                                 0.0))
            :system-cpu-load (.getSystemCpuLoad os)}
      (when (instance? UnixOperatingSystemMXBean os)
        {:os-file-descriptors-max  (.getMaxFileDescriptorCount  ^UnixOperatingSystemMXBean os)
         :os-file-descriptors-open (.getOpenFileDescriptorCount ^UnixOperatingSystemMXBean os)}))))
