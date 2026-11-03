/*
 * Copyright (c) 2015, 2026 Memorial Sloan Kettering Cancer Center.
 *
 * SPDX-License-Identifier: Apache-2.0
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
     * Constructor.
     *
     * @param msg Error Message.
     * @param throwable Throwable Object containing root cause.
     */
    public DaoException(String msg, Throwable throwable) {
        super(throwable);
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
