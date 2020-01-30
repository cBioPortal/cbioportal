/*
 * Copyright (c) 2015 Memorial Sloan-Kettering Cancer Center.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF MERCHANTABILITY OR FITNESS
 * FOR A PARTICULAR PURPOSE. The software and documentation provided hereunder
 * is on an "as is" basis, and Memorial Sloan-Kettering Cancer Center has no
 * obligations to provide maintenance, support, updates, enhancements or
 * modifications. In no event shall Memorial Sloan-Kettering Cancer Center be
 * liable to any party for direct, indirect, special, incidental or
 * consequential damages, including lost profits, arising out of the use of this
 * software and its documentation, even if Memorial Sloan-Kettering Cancer
 * Center has been advised of the possibility of such damage.
 */

/*
 * This file is part of cBioPortal.
 *
 * cBioPortal is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.mskcc.cbio.portal.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;
import org.apache.log4j.*;

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
    private static org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(
        ProgressMonitor.class
    );
    private boolean consoleMode;
    private boolean showProgress;
    private TreeSet<String> warnings = new TreeSet<>();
    private HashMap<String, Integer> warningCounts = new HashMap<>();
    private List<String> debugMessages = new ArrayList<>();

    private static final ProgressMonitor progressMonitor = new ProgressMonitor();

    /**
     * Private ctor for enforcing singleton
     */
    private ProgressMonitor() {}

    private static boolean isRunningOnServer() {
        return ServerDetector.getServerId() != null;
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
     * @return returns true if !isRunningOnServer() and progressMonitor.showProgress==true
     */
    public static boolean isShowProgress() {
        return !isRunningOnServer() && progressMonitor.showProgress;
    }

    /**
     * Gets Console Mode Flag.
     *
     * @return Boolean Flag.
     */
    public static boolean isConsoleMode() {
        return !isRunningOnServer() && progressMonitor.consoleMode;
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
            return (
                progressMonitor.curValue / (double) progressMonitor.maxValue
            );
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
        if (isRunningOnServer()) return null;
        return progressMonitor.log.toString();
    }

    /**
     * Logs a Message.
     *
     * @param currentMessage Current Task Message.
     */
    public static void setCurrentMessage(String currentMessage) {
        if (isRunningOnServer()) return;
        progressMonitor.currentMessage = currentMessage;
        progressMonitor.log.append(currentMessage + "\n");
        if (progressMonitor.consoleMode) {
            System.out.println(currentMessage);
        }
    }

    public static void logWarning(String warning) {
        logger.log(Level.WARN, warning);
        if (isRunningOnServer()) return;
        progressMonitor.warnings.add(warning);
        if (!progressMonitor.warningCounts.containsKey(warning)) {
            progressMonitor.warningCounts.put(warning, 0);
        }
        progressMonitor.warningCounts.put(
            warning,
            progressMonitor.warningCounts.get(warning) + 1
        );
    }

    public static void logDebug(String debugMessage) {
        logger.log(Level.DEBUG, debugMessage);
        if (isShowProgress()) progressMonitor.debugMessages.add(debugMessage);
    }

    public static ArrayList<String> getWarnings() {
        ArrayList<String> ret = new ArrayList<>();
        if (isRunningOnServer()) return ret;
        for (
            Iterator<String> sit = progressMonitor.warnings.iterator();
            sit.hasNext();
        ) {
            String w = sit.next();
            ret.add(w + "; " + progressMonitor.warningCounts.get(w) + "x");
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
