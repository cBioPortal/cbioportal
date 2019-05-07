package org.cbioportal.service.exception;

public class CacheNotFoundException extends Exception {

    public CacheNotFoundException(String cacheName) {
        super("No cache found with name " + cacheName);
    }

}
