/**
 *   Copyright (c) Shantanu Kumar. All rights reserved.
 *   The use and distribution terms for this software are covered by the
 *   Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
 *   which can be found in the file LICENSE at the root of this distribution.
 *   By using this software in any fashion, you are agreeing to be bound by
 *      the terms of this license.
 *   You must not remove this notice, or any other, from this software.
 **/

package bract.core;

import java.util.concurrent.atomic.AtomicInteger;

public class Echo {

    private static final long echoStartMillis = System.currentTimeMillis();
    private static volatile boolean verbose = false;

    public static boolean isVerbose() {
        return verbose;
    }

    public static void setVerbose(boolean isVerbose) {
        verbose = isVerbose;
    }

    private static final AtomicInteger SECTION_COUNTER = new AtomicInteger();

    private static int nextSectionIndex() {
        return SECTION_COUNTER.incrementAndGet();
    }

    public static class Section {
        private final int sectionIndex;
        private final String sectionDescription;
        public Section(final String desc) {
            this.sectionIndex = nextSectionIndex();
            this.sectionDescription = desc;
        }
        public void echoBegin() {
            echo(String.format("===== [%2d] Begin: %s =====", sectionIndex, sectionDescription));
        }
        public void echoEnd() {
            echo(String.format("..... [%2d]   End: %s .....", sectionIndex, sectionDescription));
        }
    }

    public static Section echoSection(final String sectionDescription) {
        return new Section(sectionDescription);
    }

    public static void echo(String msg) {
        if (verbose) {
            System.err.printf("[bract %dms] %s\n", System.currentTimeMillis() - echoStartMillis, msg);
        }
    }

    public static <T> T echo(String msg, T value) {
        if (verbose) {
            System.err.printf("[bract %dms] %s\n", System.currentTimeMillis() - echoStartMillis, msg + " : " + value);
        }
        return value;
    }

    public static void abort(String msg) {
        System.err.printf("[bract:ABORT %dms] %s\n", System.currentTimeMillis() - echoStartMillis, msg);
        System.exit(1);
    }

}
