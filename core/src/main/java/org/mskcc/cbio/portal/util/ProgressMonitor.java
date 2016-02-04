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

import org.apache.log4j.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.TreeSet;

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
    private static org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(ProgressMonitor.class);
    private boolean consoleMode;
    private TreeSet<String> warnings = new TreeSet<>();
    private HashMap<String, Integer> warningCounts = new HashMap<>();

    private static final ProgressMonitor progressMonitor = new ProgressMonitor();

    /**
     * Private ctor for enforcing singleton
     */
    private ProgressMonitor() {
        
    }
    
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
        if (isRunningOnServer())
            return null;
        return progressMonitor.log.toString();
    }

    /**
     * Logs a Message.
     *
     * @param currentMessage Current Task Message.
     */
    public static void setCurrentMessage(String currentMessage) {
        if (isRunningOnServer())
            return;
        progressMonitor.currentMessage = currentMessage;
        progressMonitor.log.append(currentMessage + "\n");
        if (progressMonitor.consoleMode) {
            System.err.println(currentMessage);
        }
    }

    public static void logWarning(String warning) {
        logger.log(Level.WARN, warning);
        if (isRunningOnServer())
            return;
        progressMonitor.warnings.add(warning);
        if (!progressMonitor.warningCounts.containsKey(warning)) {
            progressMonitor.warningCounts.put(warning, 0);
        }
        progressMonitor.warningCounts.put(warning, progressMonitor.warningCounts.get(warning)+1);
    }

    public static ArrayList<String> getWarnings() {
        ArrayList<String> ret = new ArrayList<>();
        if (isRunningOnServer())
            return ret;
        for(Iterator<String> sit = progressMonitor.warnings.iterator(); sit.hasNext(); ) {
            String w = sit.next();
            ret.add(w + "; "+progressMonitor.warningCounts.get(w)+"x");
        }
        return ret;
    }
}
