/*
 * Copyright (c) 2016 The Hyve B.V.
 * This code is licensed under the GNU Affero General Public License (AGPL),
 * version 3, or (at your option) any later version.
 *
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

package org.mskcc.cbio.portal.scripts;

import java.io.IOException;
import java.util.Date;
import org.mskcc.cbio.portal.util.ConsoleUtil;
import org.mskcc.cbio.portal.util.ProgressMonitor;

/**
 * A superclass for procedures runnable as a command-line script.
 *
 * This keeps the procedure logic in {@link #run()} clean of the
 * command line-specific functionality added by {@link #runInConsole()},
 * such as exiting the JVM process with a usage message and exit status code.
 *
 * @see {@link UsageException}
 */
public abstract class ConsoleRunnable implements Runnable {

    /**
     * Standard Unix exit codes from <code>sysexits.h</code>.
     *
     * @see  <a href="https://www.freebsd.org/cgi/man.cgi?query=sysexits&sektion=3">
     *           The (Free)BSD man page for <code>sysexits(3)</code>
     *       </a>
     */
    private enum SysExit {
        EX_OK(0), // successful termination
        EX_USAGE(64), // command line usage error
        EX_DATAERR(65), // data format error
        EX_NOINPUT(66), // cannot open input
        EX_NOUSER(67), // addressee unknown
        EX_NOHOST(68), // host name unknown
        EX_UNAVAILABLE(69), // service unavailable
        EX_SOFTWARE(70), // internal software error
        EX_OSERR(71), // system error (e.g., can't fork)
        EX_OSFILE(72), // critical OS file missing
        EX_CANTCREAT(73), // can't create (user) output file
        EX_IOERR(74), // input/output error
        EX_TEMPFAIL(75), // temp failure; user is invited to retry
        EX_PROTOCOL(76), // remote error in protocol
        EX_NOPERM(77), // permission denied
        EX_CONFIG(78); // configuration error

        private final int statusCode;

        /**
         * Instantiates an exit status symbol for a numeric code.
         *
         * @param statusCode  the exit status code
         */
        private SysExit(int statusCode) {
            this.statusCode = statusCode;
        }

        /**
         * Gets the numeric value of the exit status.
         *
         * @return  the exit status code
         */
        public int getStatusCode() {
            return statusCode;
        }

        /**
         * Selects an appropriate exit status symbol to reflect an exception.
         *
         * @param t  the exception or error to base the exit status on
         */
        public static SysExit select(Throwable t) {
            Class<? extends Throwable> exceptionClass;
            // if this is an uninformative RuntimeException packing another
            // cause, use the class of the cause to determine exit status
            if (
                t.getClass() == RuntimeException.class &&
                t.getCause() != null &&
                (
                    t.getMessage() == null ||
                    t.getMessage().equals(t.getCause().toString())
                )
            ) {
                exceptionClass = t.getCause().getClass();
            } else {
                exceptionClass = t.getClass();
            }
            // Select an appropriate exit status for this class of throwable
            if (
                IllegalArgumentException.class.isAssignableFrom(
                        exceptionClass
                    ) ||
                exceptionClass.equals(RuntimeException.class)
            ) {
                // These are usually thrown because of input data errors
                return EX_DATAERR;
            } else if (IOException.class.isAssignableFrom(exceptionClass)) {
                return EX_IOERR;
            } else {
                return EX_SOFTWARE;
            }
        }
    }

    /**
     * the command line arguments to be used by the {@link #run()} method
     */
    protected String[] args;

    /**
     * Instantiates a ConsoleRunnable to run with the given command line args.
     *
     * @param args  the command line arguments to be used
     * @see  {@link #run()}
     */
    public ConsoleRunnable(String[] args) {
        this.args = args;
    }

    /**
     * Performs the functionality of the script not specific to console usage.
     */
    @Override
    public abstract void run();

    /**
     * Runs the command as a script and exits with an appropriate exit code.
     */
    public void runInConsole() {
        SysExit status;
        try {
            Date start = new Date();
            ProgressMonitor.setConsoleModeAndParseShowProgress(this.args);
            this.run();
            ConsoleUtil.showMessages();
            System.err.println("Done.");
            Date end = new Date();
            long totalTime = end.getTime() - start.getTime();
            System.err.println("Total time:  " + totalTime + " ms\n");
            status = SysExit.EX_OK;
        } catch (UsageException e) {
            e.printUsageLine();
            status = SysExit.EX_USAGE;
        } catch (Throwable t) {
            ConsoleUtil.showWarnings();
            System.err.println("\nABORTED!");
            t.printStackTrace();
            status = SysExit.select(t);
        }
        System.exit(status.getStatusCode());
    }
}
