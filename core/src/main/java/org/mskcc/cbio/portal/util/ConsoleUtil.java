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

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;

/**
 * Misc Utility Methods for Console Applications.
 *
 * @author Ethan Cerami
 */
public class ConsoleUtil {
    private static String msg = "";

    /**
     * Outputs Progress Messages to Console.
     * Uses ASNI Terminal Codes
     * For future reference, ANSI Codes are here:
     * http://www.dee.ufcg.edu.br/~rrbrandt/tools/ansi.html
     *
     * @param pMonitor ProgressMonitor Object.
     */
    public static synchronized void showProgress(ProgressMonitor pMonitor) {
        if (pMonitor.isConsoleMode()) {
            int currentValue = pMonitor.getCurValue();
            if (currentValue % 100 == 0) {
                System.err.print(".");
            }
            
            // TODO: this writes progress every 1000 records, which can be far too much output; how about every 1%, or, even better, every s sec
            // where s is configurable?
            // TODO: build the solution into ProgressMonitor
            if (currentValue % 1000 == 0) {
                NumberFormat format = DecimalFormat.getPercentInstance();
                double percent = pMonitor.getPercentComplete();
                msg = new String("Percentage Complete:  "
                        + format.format(percent));
                System.err.println("\n" + msg);
                Runtime rt = Runtime.getRuntime();
                long used = rt.totalMemory() - rt.freeMemory();
                System.err.println("Mem Allocated:  " + getMegabytes(rt.totalMemory())
                        + ", Mem used:  " + getMegabytes(used) + ", Mem free:  "
                        + getMegabytes(rt.freeMemory()));
            }
            if (currentValue == pMonitor.getMaxValue()) {
                System.err.println();
            }
        }
    }

    public static void showWarnings(ProgressMonitor pMonitor) {
        ArrayList warningList = pMonitor.getWarnings();
        if (warningList.size() == 0) {
            System.err.println("\nNo warning/error messages generated.");
        } else {
            System.err.println("\nWarnings / Errors:");
            System.err.println("-------------------");
            for (int i = 0; i < warningList.size(); i++) {
                System.err.println(i + ".  " + warningList.get(i));
            }
        }
    }

    private static String getMegabytes(long bytes) {
        double mBytes = (bytes / 1024.0) / 1024.0;
        DecimalFormat formatter = new DecimalFormat("#,###,###.###");
        return formatter.format(mBytes) + " MB";
    }
}

