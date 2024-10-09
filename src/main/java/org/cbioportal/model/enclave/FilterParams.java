package org.cbioportal.model.enclave;

import java.util.ArrayList;
import java.util.List;

public class FilterParams {
    public Integer ageAtDiagnosisMin;
    public Integer ageAtDiagnosisMax;
    public List<String> primaryDiagnosis = new ArrayList<>();
    public List<String> ethnicity = new ArrayList<>();
    public List<String> gender = new ArrayList<>();
    public List<String> race = new ArrayList<>();
    public List<String> vitalStatus = new ArrayList<>();
    
    public FilterParams deepClone() {
        var r = new FilterParams();
        r.ageAtDiagnosisMin = this.ageAtDiagnosisMin;
        r.ageAtDiagnosisMax = this.ageAtDiagnosisMax;
        r.primaryDiagnosis.addAll(this.primaryDiagnosis);
        r.ethnicity.addAll(this.ethnicity);
        r.gender.addAll(this.gender);
        r.race.addAll(this.race);
        r.vitalStatus.addAll(this.vitalStatus);
        return r;
    }
}
