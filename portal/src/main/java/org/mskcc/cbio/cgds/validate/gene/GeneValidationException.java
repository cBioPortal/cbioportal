package org.mskcc.cgds.validate.gene;

/**
 * Encapsulates a Gene Validation Exception.
 *
 * @author Ethan Cerami.
 */
public class GeneValidationException extends Exception {
    private String userMessage;

    public GeneValidationException(String userMsg) {
        this.userMessage = userMsg;
    }

    public GeneValidationException() {
    }

    public String getUserMessage() {
        return userMessage;
    }

    public void setUserMessage(String userMsg) {
        this.userMessage = userMsg;
    }

    public String getMessage() {
        return userMessage;
    }
}
