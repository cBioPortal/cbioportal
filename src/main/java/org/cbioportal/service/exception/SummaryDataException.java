package org.cbioportal.service.exception;

public class SummaryDataException extends Exception {
    
    public SummaryDataException(String reason, Exception inner) {
        super(reason, inner);
    }
}
