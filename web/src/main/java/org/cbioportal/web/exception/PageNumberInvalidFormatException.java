package org.cbioportal.web.exception;

public class PageNumberInvalidFormatException extends Exception {

    private String pageNumber;

    public PageNumberInvalidFormatException(String pageNumber) {
        super();
        this.pageNumber = pageNumber;
    }

    public String getPageNumber() {
        return pageNumber;
    }

    public void setPageNumber(String pageNumber) {
        this.pageNumber = pageNumber;
    }
}
