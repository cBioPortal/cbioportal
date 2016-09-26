package org.cbioportal.web.exception;

public class PageSizeTooSmallException extends Exception {

    private int pageSize;

    public PageSizeTooSmallException(int pageSize) {
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
