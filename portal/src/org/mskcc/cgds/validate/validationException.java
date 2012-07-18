package org.mskcc.cgds.validate;

public class validationException extends Exception {
    /**
     * Throws a data validation exception.  Data can be, for example, a bean or a line from a data file.
     * @param param
     */
    public validationException(Object param) {
        super(param.toString());
    }
}
