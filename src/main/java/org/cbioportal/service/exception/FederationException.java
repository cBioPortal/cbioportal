package org.cbioportal.service.exception;

public class FederationException extends Exception {
    
    public FederationException(String reason) {
        super(reason);
    }
    
    public FederationException(String reason, Exception inner) {
        super(reason, inner);
    }
}
