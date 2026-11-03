/*
 * Copyright (c) 2015 Memorial Sloan-Kettering Cancer Center.
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.mskcc.cbio.portal.validate;

public class validationException extends Exception {
    /**
     * Throws a data validation exception.  Data can be, for example, a bean or a line from a data file.
     * @param param
     */
    public validationException(Object param) {
        super(param.toString());
    }
}
