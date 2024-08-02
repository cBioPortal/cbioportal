package org.cbioportal.web.parameter;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import java.util.List;

public class SurvivalRequest implements Serializable {

    @Size(min = 1, max = PagingConstants.MAX_PAGE_SIZE)
    private List<PatientIdentifier> patientIdentifiers;

    @NotNull
    private String attributeIdPrefix;

    @Valid
    private ClinicalEventRequestIdentifier startEventRequestIdentifier;

    @Valid
    private ClinicalEventRequestIdentifier endEventRequestIdentifier;

    @Valid
    private ClinicalEventRequestIdentifier censoredEventRequestIdentifier;

    public List<PatientIdentifier> getPatientIdentifiers() {
        return patientIdentifiers;
    }

    public void setPatientIdentifiers(List<PatientIdentifier> patientIdentifiers) {
        this.patientIdentifiers = patientIdentifiers;
    }

    public String getAttributeIdPrefix() {
        return attributeIdPrefix;
    }

    public void setAttributeIdPrefix(String attributeIdPrefix) {
        this.attributeIdPrefix = attributeIdPrefix;
    }

    public ClinicalEventRequestIdentifier getStartEventRequestIdentifier() {
        return startEventRequestIdentifier;
    }

    public void setStartEventRequestIdentifier(ClinicalEventRequestIdentifier startEventRequestIdentifier) {
        this.startEventRequestIdentifier = startEventRequestIdentifier;
    }

    public ClinicalEventRequestIdentifier getEndEventRequestIdentifier() {
        return endEventRequestIdentifier;
    }

    public void setEndEventRequestIdentifier(ClinicalEventRequestIdentifier endEventRequestIdentifier) {
        this.endEventRequestIdentifier = endEventRequestIdentifier;
    }

    public ClinicalEventRequestIdentifier getCensoredEventRequestIdentifier() {
        return censoredEventRequestIdentifier;
    }

    public void setCensoredEventRequestIdentifier(ClinicalEventRequestIdentifier censoredEventRequestIdentifier) {
        this.censoredEventRequestIdentifier = censoredEventRequestIdentifier;
    }
}
