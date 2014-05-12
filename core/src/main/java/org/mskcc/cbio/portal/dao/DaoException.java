/** Copyright (c) 2012 Memorial Sloan-Kettering Cancer Center.
 *
 * This library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
 * MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
 * documentation provided hereunder is on an "as is" basis, and
 * Memorial Sloan-Kettering Cancer Center 
 * has no obligations to provide maintenance, support,
 * updates, enhancements or modifications.  In no event shall
 * Memorial Sloan-Kettering Cancer Center
 * be liable to any party for direct, indirect, special,
 * incidental or consequential damages, including lost profits, arising
 * out of the use of this software and its documentation, even if
 * Memorial Sloan-Kettering Cancer Center 
 * has been advised of the possibility of such damage.
*/

package org.mskcc.cbio.portal.dao;

/**
 * Exception Occurred while reading/writing data to database.
 *
 * @author Ethan Cerami
 */
public class DaoException extends Exception {
    private final String msg;

    /**
     * Constructor.
     *
     * @param throwable Throwable Object containing root cause.
     */
    public DaoException(Throwable throwable) {
        super(throwable);
        this.msg = throwable.getMessage();
    }

    /**
     * Constructor.
     *
     * @param msg Error Message.
     */
    public DaoException(String msg) {
        super();
        this.msg = msg;
    }

    /**
     * Gets Error Message.
     *
     * @return Error Message String.I
     */
    public String getMessage() {
        return msg;
    }
}
