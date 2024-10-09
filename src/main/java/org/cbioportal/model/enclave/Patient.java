package org.cbioportal.model.enclave;

import java.util.Map;

public class Patient {
    public int ageAtDiagnosis;
    public String primaryDiagnosis;
    public String ethnicity;
    public String gender;
    public String race;
    public String vitalStatus;
    public Map<String, Integer> mutatedGenes;
}
