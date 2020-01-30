package org.cbioportal.model;

import java.io.Serializable;
import javax.validation.constraints.NotNull;

public class TableTimestampPair implements Serializable {
    @NotNull
    private String tableName;

    private String updateTime;

    public String getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(String updateTime) {
        this.updateTime = updateTime;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }
}
