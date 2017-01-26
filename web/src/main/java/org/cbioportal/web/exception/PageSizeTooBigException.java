package org.cbioportal.web.exception;

public class PageSizeTooBigException extends Exception {

    private int pageSize;

    public PageSizeTooBigException(int pageSize) {
        super();
        this.pageSize = pageSize;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }
}
