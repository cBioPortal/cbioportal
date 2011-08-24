package org.mskcc.cgds.dao;

/**
 * Exception Occurred while reading/writing data to database.
 *
 * @author Ethan Cerami
 */
public class DaoException extends Exception {
    private String msg;

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
