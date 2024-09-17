package org.cbioportal.web.parameter;

import org.cbioportal.service.util.CustomAttributeWithData;
import org.cbioportal.service.util.CustomDataValue;

import java.io.Serializable;
import java.util.List;

public class ClinicalDataFilter extends DataFilter implements Serializable {

    private String attributeId;
    private List<CustomSampleIdentifier> samples;
    private String datatype;
    private String displayName;

    public String getAttributeId() {
        return attributeId;
    }

    public void setAttributeId(String attributeId) {
        this.attributeId = attributeId;
    }

    public List<CustomSampleIdentifier> getSamples() {
        return samples;
    }

    public void setSamples(List<CustomSampleIdentifier> samples) {
        this.samples = samples;
    }

    public String getDatatype() {
        return datatype;
    }

    public void setDatatype(String datatype) {
        this.datatype = datatype;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

}
