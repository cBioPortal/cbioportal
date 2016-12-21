package org.cbioportal.web.exception;

public class PageNumberTooSmallException extends Exception {

    private int pageNumber;

    public PageNumberTooSmallException(int pageNumber) {
        super();
        this.pageNumber = pageNumber;
    }

    public int getPageNumber() {
        return pageNumber;
    }

    public void setPageNumber(int pageNumber) {
        this.pageNumber = pageNumber;
    }
}
