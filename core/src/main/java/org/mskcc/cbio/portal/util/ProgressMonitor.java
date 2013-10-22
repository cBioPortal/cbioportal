/** Copyright (c) 2012 Memorial Sloan-Kettering Cancer Center.
**
** This library is free software; you can redistribute it and/or modify it
** under the terms of the GNU Lesser General Public License as published
** by the Free Software Foundation; either version 2.1 of the License, or
** any later version.
**
** This library is distributed in the hope that it will be useful, but
** WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
** MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
** documentation provided hereunder is on an "as is" basis, and
** Memorial Sloan-Kettering Cancer Center 
** has no obligations to provide maintenance, support,
** updates, enhancements or modifications.  In no event shall
** Memorial Sloan-Kettering Cancer Center
** be liable to any party for direct, indirect, special,
** incidental or consequential damages, including lost profits, arising
** out of the use of this software and its documentation, even if
** Memorial Sloan-Kettering Cancer Center 
** has been advised of the possibility of such damage.  See
** the GNU Lesser General Public License for more details.
**
** You should have received a copy of the GNU Lesser General Public License
** along with this library; if not, write to the Free Software Foundation,
** Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
**/

package org.mskcc.cbio.portal.util;

import java.util.ArrayList;

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
    private boolean consoleMode;
    private ArrayList<String> warningList = new ArrayList<String>();

    /**
     * Sets Console Flag.
     * When set to true Progress Monitor Messages are displayed to System.out.
     *
     * @param consoleFlag Console Mode Flag.
     */
    public void setConsoleMode(boolean consoleFlag) {
        this.consoleMode = consoleFlag;
    }

    /**
     * Gets Console Mode Flag.
     *
     * @return Boolean Flag.
     */
    public boolean isConsoleMode() {
        return this.consoleMode;
    }

    /**
     * Gets Percentage Complete.
     *
     * @return double value.
     */
    public double getPercentComplete() {
        if (curValue == 0) {
            return 0.0;
        } else {
            return (curValue / (double) maxValue);
        }
    }

    /**
     * Gets Max Value.
     *
     * @return max value.
     */
    public int getMaxValue() {
        return maxValue;
    }

    /**
     * Sets Max Value.
     *
     * @param maxValue Max Value.
     */
    public void setMaxValue(int maxValue) {
        this.maxValue = maxValue;
        this.curValue = 0;
    }

    /**
     * Gets Current Value.
     *
     * @return Current Value.
     */
    public int getCurValue() {
        return curValue;
    }

    /**
     * Increments the Current Value.
     */
    public void incrementCurValue() {
        curValue++;
    }

    /**
     * Sets the Current Value.
     *
     * @param curValue Current Value.
     */
    public void setCurValue(int curValue) {
        this.curValue = curValue;
    }

    /**
     * Gets the Current Task Message.
     *
     * @return Current Task Message.
     */
    public String getCurrentMessage() {
        return currentMessage;
    }

    /**
     * Gets Log of All Messages.
     *
     * @return String Object.
     */
    public String getLog() {
        return log.toString();
    }

    /**
     * Logs a Message.
     *
     * @param currentMessage Current Task Message.
     */
    public void setCurrentMessage(String currentMessage) {
        this.currentMessage = currentMessage;
        this.log.append(currentMessage + "\n");
        if (consoleMode) {
            System.err.println(currentMessage);
        }
    }

    public void logWarning(String warning) {
        warningList.add(warning);
    }

    public ArrayList<String> getWarnings() {
        return warningList;
    }
}
