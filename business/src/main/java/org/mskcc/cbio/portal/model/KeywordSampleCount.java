package org.mskcc.cbio.portal.model;

import java.io.Serializable;

public class KeywordSampleCount implements Serializable {
    private String keyword;
    private Integer count;

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }
}
