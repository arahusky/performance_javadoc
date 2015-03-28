/*
 * Copyright (c) 2005, 2013, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */
package cz.cuni.mff.d3s.tools.perfdoc.blackhole;

import sun.misc.Unsafe;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class Utils {

    private static final Unsafe U;

    static {
        try {
            Field unsafe = Unsafe.class.getDeclaredField("theUnsafe");
            unsafe.setAccessible(true);
            U = (Unsafe) unsafe.get(null);
        } catch (NoSuchFieldException e) {
            throw new IllegalStateException(e);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException(e);
        }
    }

    private Utils() {

    }

    public static String[] concat(String[] t1, String[] t2) {
        String[] r = new String[t1.length + t2.length];
        System.arraycopy(t1, 0, r, 0, t1.length);
        System.arraycopy(t2, 0, r, t1.length, t2.length);
        return r;
    }

    public static String join(Collection<String> src, String delim) {
        StringBuilder sb = new StringBuilder();
        boolean first = true;
        for (String s : src) {
            if (first) {
                first = false;
            } else {
                sb.append(delim);
            }
            sb.append(s);
        }
        return sb.toString();
    }

    public static String join(String[] src, String delim) {
        return join(Arrays.asList(src), delim);
    }

    public static int sum(int[] arr) {
        int sum = 0;
        for (int i : arr) {
            sum += i;
        }
        return sum;
    }

    public static int roundUp(int v, int quant) {
        if ((v % quant) == 0) {
            return v;
        } else {
            return ((v / quant) + 1)*quant;
        }
    }

    public static String throwableToString(Throwable t) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        t.printStackTrace(pw);
        pw.flush();
        pw.close();
        return sw.toString();
    }

    public static int[] unmarshalIntArray(String src) {
        String[] ss = src.split("=");
        int[] arr = new int[ss.length];
        int cnt = 0;
        for (String s : ss) {
            arr[cnt] = Integer.valueOf(s.trim());
            cnt++;
        }
        return arr;
    }

    public static String marshalIntArray(int[] arr) {
        StringBuilder sb = new StringBuilder();
        for (int i : arr) {
            sb.append(i);
            sb.append("=");
        }
        return sb.toString();
    }

    /**
     * Warm up the CPU schedulers, bring all the CPUs online to get the
     * reasonable estimate of the system capacity. Some systems, notably embedded Linuxes,
     * power down the idle CPUs and so availableProcessors() may report lower CPU count
     * than would be present after the load-up.
     *
     * @return max CPU count
     */
    public static int figureOutHotCPUs() {
        ExecutorService service = Executors.newCachedThreadPool();

        int warmupTime = 1000;
        long lastChange = System.currentTimeMillis();

        List<Future<?>> futures = new ArrayList<Future<?>>();
        futures.add(service.submit(new BurningTask()));

        int max = 0;
        while (System.currentTimeMillis() - lastChange < warmupTime) {
            int cur = Runtime.getRuntime().availableProcessors();
            if (cur > max) {
                max = cur;
                lastChange = System.currentTimeMillis();
                futures.add(service.submit(new BurningTask()));
            }
        }

        for (Future<?> f : futures) {
            f.cancel(true);
        }

        service.shutdown();

        return max;
    }

    static class BurningTask implements Runnable {
        @Override
        public void run() {
            while (!Thread.interrupted()); // burn;
        }
    }

    public static void check(Class<?> klass, String... fieldNames) {
        for (String fieldName : fieldNames) {
            check(klass, fieldName);
        }
    }

    public static void check(Class<?> klass, String fieldName) {
        final long requiredGap = 128;
        long markerBegin = getOffset(klass, "markerBegin");
        long markerEnd = getOffset(klass, "markerEnd");
        long off = getOffset(klass, fieldName);
        if (markerEnd - off < requiredGap || off - markerBegin < requiredGap) {
            throw new IllegalStateException("Consistency check failed for " + fieldName + ", off = " + off + ", markerBegin = " + markerBegin + ", markerEnd = " + markerEnd);
        }
    }

    public static long getOffset(Class<?> klass, String fieldName) {
        do {
            try {
                Field f = klass.getDeclaredField(fieldName);
                return U.objectFieldOffset(f);
            } catch (NoSuchFieldException e) {
                // whatever, will try superclass
            }
            klass = klass.getSuperclass();
        } while (klass != null);
        throw new IllegalStateException("Can't find field \"" + fieldName + "\"");
    }

    public static boolean isWindows() {
        return System.getProperty("os.name").contains("indows");
    }

    public static String getCurrentJvm() {
        return System.getProperty("java.home") +
                File.separator +
                "bin" +
                File.separator +
                "java" +
                (isWindows() ? ".exe" : "");
    }

}
