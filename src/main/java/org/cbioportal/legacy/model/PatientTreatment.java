package org.cbioportal.legacy.model;

import java.io.Serializable;

public record PatientTreatment (String treatment, int count) implements Serializable {
    
}
