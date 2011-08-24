package org.mskcc.cgds.util;


public class Logger {
    private static boolean log = false;

    public static void log (String msg) {
        if (log) {
            System.out.println (msg);
        }
    }
}
