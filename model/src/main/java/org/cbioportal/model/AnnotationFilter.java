package org.cbioportal.model;

import java.io.Serializable;
import java.util.List;

/**
 * Created by Hongxin Zhang on 2019-07-30.
 */
public class AnnotationFilter implements Serializable {
    private String path;
    private List<String> values;

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public List<String> getValues() {
        return values;
    }

    public void setValues(List<String> values) {
        this.values = values;
    }
}