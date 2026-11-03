/*
 * Copyright (c) 2015, 2026 Memorial Sloan Kettering Cancer Center.
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.mskcc.cbio.portal.util;

import java.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Monitors Progress of Long Term Tasks.
 *
 * @author Ethan Cerami.
 */
public class ProgressMonitor {
    private int maxValue;
    private int curValue;
    private String currentMessage;
    private StringBuffer log = new StringBuffer();
    private static Logger logger = LoggerFactory.getLogger(ProgressMonitor.class);
    private boolean consoleMode;
    private boolean showProgress;
    private TreeSet<String> warnings = new TreeSet<>();
    private HashMap<String, Integer> warningCounts = new HashMap<>();
    private List<String> debugMessages = new ArrayList<>();

    private static final ProgressMonitor progressMonitor = new ProgressMonitor();

    /**
     * Private ctor for enforcing singleton
     */
    private ProgressMonitor() {

    }

    /**
     * Sets Console Flag.
     * When set to true Progress Monitor Messages are displayed to System.out.
     *
     * @param consoleFlag Console Mode Flag.
     */
    public static void setConsoleMode(boolean consoleFlag) {
        progressMonitor.consoleMode = consoleFlag;
    }

    /**
     * Sets consoleMode to true and tries to infer showProgress mode from args. If an argument
     * with name "--noprogress" is found, then showProgress is set to false
     *
     * @param args
     */
    public static void setConsoleModeAndParseShowProgress(String[] args) {
        //default
        setConsoleMode(true);
        if (Arrays.asList(args).contains("--noprogress")) {
            setShowProgress(false);
        } else {
            //default:
            setShowProgress(true);
        }

    }

    /**
     * Whether the progress (in % complete and memory used) should be
     * printed to the console.
     *
     * @param showProgress : set to false to avoid extra messages about % complete and memory usage.
     */
    public static void setShowProgress(boolean showProgress) {
        progressMonitor.showProgress = showProgress;
    }

    /**
     * Whether the progress (in % complete and memory used) should be
     * printed to the console.
     *
     * @return returns true if progressMonitor.showProgress==true
     */
    public static boolean isShowProgress() {
        return progressMonitor.showProgress;
    }

    /**
     * Gets Console Mode Flag.
     *
     * @return Boolean Flag.
     */
    public static boolean isConsoleMode() {
        return progressMonitor.consoleMode;
    }

    /**
     * Gets Percentage Complete.
     *
     * @return double value.
     */
    public static double getPercentComplete() {
        if (progressMonitor.curValue == 0) {
            return 0.0;
        } else {
            return (progressMonitor.curValue / (double) progressMonitor.maxValue);
        }
    }

    /**
     * Gets Max Value.
     *
     * @return max value.
     */
    public static int getMaxValue() {
        return progressMonitor.maxValue;
    }

    /**
     * Sets Max Value.
     *
     * @param maxValue Max Value.
     */
    public static void setMaxValue(int maxValue) {
        progressMonitor.maxValue = maxValue;
        progressMonitor.curValue = 0;
    }

    /**
     * Gets Current Value.
     *
     * @return Current Value.
     */
    public static int getCurValue() {
        return progressMonitor.curValue;
    }

    /**
     * Increments the Current Value.
     */
    public static void incrementCurValue() {
        progressMonitor.curValue++;
    }

    /**
     * Sets the Current Value.
     *
     * @param curValue Current Value.
     */
    public static void setCurValue(int curValue) {
        progressMonitor.curValue = curValue;
    }

    /**
     * Gets the Current Task Message.
     *
     * @return Current Task Message.
     */
    public static String getCurrentMessage() {
        return progressMonitor.currentMessage;
    }

    /**
     * Gets Log of All Messages.
     *
     * @return String Object.
     */
    public static String getLog() {
        return progressMonitor.log.toString();
    }

    /**
     * Resets the log.
     */
    public static void resetLog() {
        progressMonitor.log.setLength(0);
    }

    /**
     * Logs a Message.
     *
     * @param currentMessage Current Task Message.
     */
    public static void setCurrentMessage(String currentMessage) {
        progressMonitor.currentMessage = currentMessage;
        progressMonitor.log.append(currentMessage + "\n");
        if (progressMonitor.consoleMode) {
            System.out.println(currentMessage);
        }
    }

    public static void logWarning(String warning) {
        logger.warn(warning);
        progressMonitor.warnings.add(warning);
        if (!progressMonitor.warningCounts.containsKey(warning)) {
            progressMonitor.warningCounts.put(warning, 0);
        }
        progressMonitor.warningCounts.put(warning, progressMonitor.warningCounts.get(warning)+1);
    }

    public static void logDebug(String debugMessage) {
        logger.debug(debugMessage);
        if (isShowProgress())
            progressMonitor.debugMessages.add(debugMessage);
    }

    public static ArrayList<String> getWarnings() {
        ArrayList<String> ret = new ArrayList<>();
        for(Iterator<String> sit = progressMonitor.warnings.iterator(); sit.hasNext(); ) {
            String w = sit.next();
            ret.add(w + "; "+progressMonitor.warningCounts.get(w)+"x");
        }
        return ret;
    }

    /**
     * Reset the warnings list.
     */
    public static void resetWarnings() {
        progressMonitor.warnings.clear();
        progressMonitor.warningCounts.clear();
    }

    public static ArrayList<String> getMessages() {
        ArrayList<String> ret = getWarnings();
        ret.addAll(progressMonitor.debugMessages);
        return ret;
    }

    public static List<String> getDebugMessages() {
        return progressMonitor.debugMessages;

    }
}
