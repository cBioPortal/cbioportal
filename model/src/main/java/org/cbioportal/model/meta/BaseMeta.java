package org.cbioportal.model.meta;

import java.io.Serializable;

public class BaseMeta implements Serializable {

    private Integer totalCount;

    public Integer getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(Integer totalCount) {
        this.totalCount = totalCount;
    }
}
