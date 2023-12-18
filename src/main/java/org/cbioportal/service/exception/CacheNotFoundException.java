package org.cbioportal.service.exception;

public class CacheNotFoundException extends Exception {

    private String cacheName;

    public CacheNotFoundException(String cacheName) {
        super("No cache found with name " + cacheName);
        this.cacheName = cacheName;
    }

    public String getCacheName() {
        return cacheName;
    }

    public void setCacheName(String cacheName) {
        this.cacheName = cacheName;
    }

}
