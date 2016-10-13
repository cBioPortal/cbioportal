package org.cbioportal.web.exception;

public class PageSizeInvalidFormatException extends Exception {

    private String pageSize;

    public PageSizeInvalidFormatException(String pageSize) {
        super();
        this.pageSize = pageSize;
    }

    public String getPageSize() {
        return pageSize;
    }

    public void setPageSize(String pageSize) {
        this.pageSize = pageSize;
    }
}
