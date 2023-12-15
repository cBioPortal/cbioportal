package org.cbioportal.service.util;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.cbioportal.model.Binnable;

import java.io.Serializable;

@JsonIgnoreProperties(ignoreUnknown = true)
public class BinnableCustomDataValue implements Binnable, Serializable {

    private final CustomDataValue customDataValue;
    private final String attrId;
    private final Boolean patientAttribute;

    public BinnableCustomDataValue(
        CustomDataValue customDataValue,
        String attributeId,
        Boolean patientAttribute
    ) {
        this.customDataValue = customDataValue;
        this.attrId = attributeId;
        this.patientAttribute = patientAttribute;
    }

    public String getSampleId() {
        return customDataValue.getSampleId();
    }

    public String getPatientId() {
        return customDataValue.getPatientId();
    }

    public String getStudyId() {
        return customDataValue.getStudyId();
    }

    public String getAttrId() {
        return attrId;
    }

    public Boolean isPatientAttribute() {
        return patientAttribute;
    }
    
    public String getAttrValue() {
        return customDataValue.getValue();
    }

}
