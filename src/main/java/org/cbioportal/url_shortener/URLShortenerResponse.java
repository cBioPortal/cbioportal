package org.cbioportal.url_shortener;

public class URLShortenerResponse {

    private String shortURL;
    private String error;

    public URLShortenerResponse(String shortURL, String error) {
        this.shortURL = shortURL;
        this.error = error;
    }

    public String getShortURL() {
        return shortURL;
    }

    public void setShortURL(String shortURL) {
        this.shortURL = shortURL;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }
}
