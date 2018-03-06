package org.cbioportal.web.parameter;

import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.Size;
import java.util.List;
import java.io.Serializable;

public class PatientFilter implements Serializable {
    
    @Size(min = 1, max = PagingConstants.MAX_PAGE_SIZE)
    private List<PatientIdentifier> patientIdentifiers;
    @Size(min = 1, max = PagingConstants.MAX_PAGE_SIZE)
    private List<String> uniquePatientKeys;
    
    @AssertTrue
    private boolean isEitherPatientIdentifiersOrUniquePatientKeysPresent() {
        return patientIdentifiers != null ^ uniquePatientKeys != null;
    }

    public List<PatientIdentifier> getPatientIdentifiers() {
        return patientIdentifiers;
    }

    public void setPatientIdentifiers(List<PatientIdentifier> patientIdentifiers) {
        this.patientIdentifiers = patientIdentifiers;
    }

    public List<String> getUniquePatientKeys() {
        return uniquePatientKeys;
    }

    public void setUniquePatientKeys(List<String> uniquePatientKeys) {
        this.uniquePatientKeys = uniquePatientKeys;
    }
}
