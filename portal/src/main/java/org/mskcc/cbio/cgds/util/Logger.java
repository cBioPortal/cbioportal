package org.mskcc.cbio.cgds.util;

/**
 * Class which provides logging facility.
 * This comment added to satisfy checkstyle warning.
 * Now that I am looking at class, I wonder why it is needed
 * since we use log4j. -B
 */
public class Logger {
    private static boolean log = false;

    public static void log (String msg) {
        if (log) {
            System.out.println (msg);
        }
    }
}
