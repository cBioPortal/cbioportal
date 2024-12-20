package org.cbioportal.model;

import java.io.Serializable;

public record PatientTreatment (String treatment, int count) implements Serializable {
    
}
