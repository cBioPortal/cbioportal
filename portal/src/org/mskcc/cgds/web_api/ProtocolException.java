package org.mskcc.cgds.web_api;

/**
 * Wrapper exception class
 */
public class ProtocolException extends Exception {
    private String msg;

    public ProtocolException (String msg) {
        this.msg = msg;
    }

    public String getMsg() {
        return msg;
    }
}
