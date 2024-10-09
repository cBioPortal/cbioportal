package org.cbioportal.service.impl;

import org.cbioportal.model.ClinicalAttribute;
import org.cbioportal.model.ClinicalDataBin;
import org.cbioportal.model.ClinicalDataCount;
import org.cbioportal.model.ClinicalDataCountItem;
import org.cbioportal.model.enclave.FilterParams;
import org.cbioportal.model.enclave.Range;
import org.cbioportal.service.EnclaveApiService;
import org.cbioportal.service.EnclaveStudyService;
import org.cbioportal.web.parameter.DataBinMethod;
import org.cbioportal.web.parameter.DataFilterValue;
import org.cbioportal.web.parameter.Projection;
import org.cbioportal.web.parameter.StudyViewFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class EnclaveStudyServiceImpl implements EnclaveStudyService {
    
    @Autowired
    private EnclaveApiService apiService;

    @Override
    public List<ClinicalAttribute> fetchClinicalAttributes(Projection projection) {
        // categorical attributes -- pie chart
        ClinicalAttribute sexAttr = new ClinicalAttribute();
        sexAttr.setAttrId("SEX");
        sexAttr.setDescription("Sex");
        sexAttr.setDisplayName("Sex");
        sexAttr.setDatatype("STRING");
        sexAttr.setPatientAttribute(true);
        sexAttr.setPriority("1");
        sexAttr.setCancerStudyIdentifier("enclave_2024");
        
        ClinicalAttribute primaryDiagnosisAttr = new ClinicalAttribute();
        primaryDiagnosisAttr.setAttrId("PRIMARY_DIAGNOSIS");
        primaryDiagnosisAttr.setDescription("Primary Diagnosis");
        primaryDiagnosisAttr.setDisplayName("Primary Diagnosis");
        primaryDiagnosisAttr.setDatatype("STRING");
        primaryDiagnosisAttr.setPatientAttribute(true);
        primaryDiagnosisAttr.setPriority("1");
        primaryDiagnosisAttr.setCancerStudyIdentifier("enclave_2024");

        ClinicalAttribute ethnicityAttr = new ClinicalAttribute();
        ethnicityAttr.setAttrId("ETHNICITY");
        ethnicityAttr.setDescription("Ethnicity");
        ethnicityAttr.setDisplayName("Ethnicity");
        ethnicityAttr.setDatatype("STRING");
        ethnicityAttr.setPatientAttribute(true);
        ethnicityAttr.setPriority("1");
        ethnicityAttr.setCancerStudyIdentifier("enclave_2024");

        ClinicalAttribute raceAttr = new ClinicalAttribute();
        raceAttr.setAttrId("RACE");
        raceAttr.setDescription("Race");
        raceAttr.setDisplayName("Race");
        raceAttr.setDatatype("STRING");
        raceAttr.setPatientAttribute(true);
        raceAttr.setPriority("1");
        raceAttr.setCancerStudyIdentifier("enclave_2024");

        ClinicalAttribute vitalStatusAttr = new ClinicalAttribute();
        vitalStatusAttr.setAttrId("VITAL_STATUS");
        vitalStatusAttr.setDescription("Vital Status");
        vitalStatusAttr.setDisplayName("Vital Status");
        vitalStatusAttr.setDatatype("STRING");
        vitalStatusAttr.setPatientAttribute(true);
        vitalStatusAttr.setPriority("1");
        vitalStatusAttr.setCancerStudyIdentifier("enclave_2024");

        // numerical attributes -- histogram
        // so far only age is supported
        ClinicalAttribute ageAttr = new ClinicalAttribute();
        ageAttr.setAttrId("AGE");
        ageAttr.setDescription("Age at the time of diagnosis expressed in number of days since birth.");
        ageAttr.setDisplayName("Diagnosis Age");
        ageAttr.setDatatype("NUMBER");
        ageAttr.setPatientAttribute(true);
        ageAttr.setPriority("1");
        ageAttr.setCancerStudyIdentifier("enclave_2024");

        return Arrays.asList(
            sexAttr,
            primaryDiagnosisAttr,
            ethnicityAttr,
            raceAttr,
            vitalStatusAttr,
            ageAttr
        );
    }
    
    @Override
    public List<ClinicalDataCountItem> fetchClinicalDataCounts(
        List<String> attributes,
        StudyViewFilter studyViewFilter) {
        
        List<ClinicalDataCountItem> result = new ArrayList<>();
        FilterParams baseParams = buildFilterParams(studyViewFilter); // corresponds to the base cohort spec'd by the StudyViewFilter

        for (String attribute : attributes) {
            ClinicalDataCountItem it = new ClinicalDataCountItem();
            List<ClinicalDataCount> counts = new ArrayList<>();
            
            var uniqueValues = getUniqueValues(attribute);
            for (String value : uniqueValues) {
                FilterParams sliceParams = addSliceConstraint(baseParams, attribute, value);
                var apiResult = apiService.fetchCohortInfo(sliceParams);
                int patientCount = apiResult.count;
                
                // Translate raw count to ClinicalDataCount
                ClinicalDataCount countObj = new ClinicalDataCount();
                countObj.setAttributeId(attribute);
                countObj.setValue(value);
                countObj.setCount(patientCount);
                counts.add(countObj);
            }
            
            it.setAttributeId(attribute);
            it.setCounts(counts);
            result.add(it);
        }

        return result;
    }

    @Override
    public List<ClinicalDataBin> fetchClinicalDataBinCounts(
        DataBinMethod dataBinMethod,
        List<String> attributes,
        StudyViewFilter studyViewFilter) {
            
        List<ClinicalDataBin> result = new ArrayList<>();
        FilterParams baseParams = buildFilterParams(studyViewFilter); // corresponds to the base cohort spec'd by the StudyViewFilter

        for (String attribute : attributes) {
            for (var range : getRanges(attribute)) {
                FilterParams binParams = addBinConstraint(baseParams, attribute, range);
                var apiResult = apiService.fetchCohortInfo(binParams);
                int patientCount = apiResult.count;
                
                // Translate raw count to ClinicalDataBin
                var bin = new ClinicalDataBin();
                bin.setAttributeId(attribute);
                bin.setCount(patientCount);
                
                if (range.start == null && range.end == null) {
                    bin.setSpecialValue("NA");
                } else if (range.start == null) {
                    bin.setSpecialValue("<=");
                    bin.setEnd(range.end);
                } else if (range.end == null) {
                    bin.setSpecialValue(">");
                    bin.setStart(range.start);
                } else {
                    bin.setStart(range.start);
                    bin.setEnd(range.end);
                }
                
                result.add(bin);
            }
        }

        return result;
    }
    
    FilterParams buildFilterParams(StudyViewFilter studyViewFilter) {
        FilterParams res = new FilterParams();
        
        var clinicalDataFilters = studyViewFilter.getClinicalDataFilters();
        
        if (clinicalDataFilters != null) {
            for (var clinicalDataFilter : clinicalDataFilters) {
                String cbioAttribute = clinicalDataFilter.getAttributeId();
                boolean isCategorical = !cbioAttribute.equals("AGE");
                var filterValues = clinicalDataFilter.getValues();

                if (isCategorical) {
                    List<String> allowedValues = filterValues
                        .stream()
                        .map(DataFilterValue::getValue)
                        .collect(Collectors.toList());

                    switch (cbioAttribute) {
                        case "SEX":
                            res.gender = allowedValues;
                            break;
                        case "PRIMARY_DIAGNOSIS":
                            res.primaryDiagnosis = allowedValues;
                            break;
                        case "ETHNICITY":
                            res.ethnicity = allowedValues;
                            break;
                        case "RACE":
                            res.race = allowedValues;
                            break;
                        case "VITAL_STATUS":
                            res.vitalStatus = allowedValues;
                            break;
                    }
                } else {
                    // TODO: We assume here that all the bins are contiguous
                    // Not sure if it's possible to build non-contiguous bins using the UI

                    // If any bin lacks a 'start' or 'end' value, that means there is no
                    // lower or upper bound to the data, respectively.
                    boolean hasLowerBound = filterValues
                        .stream()
                        .allMatch(v -> v.getStart() != null);
                    boolean hasUpperBound = filterValues
                        .stream()
                        .allMatch(v -> v.getEnd() != null);

                    if (hasLowerBound) {
                        int lowerBound = filterValues
                            .stream()
                            .map(v -> v.getStart().intValueExact())
                            .min(Integer::compare)
                            .orElseThrow();
                        res.ageAtDiagnosisMin = lowerBound + 1; // exclusive -> inclusive
                    }

                    if (hasUpperBound) {
                        int upperBound = filterValues
                            .stream()
                            .map(v -> v.getEnd().intValueExact())
                            .max(Integer::compare)
                            .orElseThrow();
                        res.ageAtDiagnosisMax = upperBound;
                    }
                }
            }
        }
        
        return res;
    }
    
    List<Range> getRanges(String attribute) {
        // The only attribute that should be coming thru here atm is AGE
        
        return Arrays.asList(
            new Range(null, 10),
            new Range(10, 20),
            new Range(20, 30),
            new Range(30, 40),
            new Range(40, 50),
            new Range(50, 60),
            new Range(60, 70),
            new Range(70, 80),
            new Range(80, 90),
            new Range(90, null)
        );
    }
    
    List<String> getUniqueValues(String categoricalAttribute) {
        return switch (categoricalAttribute) {
            case "SEX" -> Arrays.asList(
                    "male",
                    "female",
                    "not reported"
            );
            case "PRIMARY_DIAGNOSIS" -> Arrays.asList(
                    "Adenocarcinoma, NOS",
                    "Infiltrating lobular mixed with other types of carcinoma",
                    "Adenoid cystic carcinoma",
                    "Infiltrating duct mixed with other types of carcinoma",
                    "Lobular carcinoma, NOS",
                    "Paget disease and infiltrating duct carcinoma of breast",
                    "Adenocarcinoma with mixed subtypes",
                    "Pleomorphic carcinoma",
                    "Infiltrating duct and lobular carcinoma",
                    "Apocrine adenocarcinoma"
            );
            case "ETHNICITY" -> Arrays.asList(
                    "hispanic or latino",
                    "not hispanic or latino",
                    "not reported"
            );
            case "RACE" -> Arrays.asList(
                    "white",
                    "black or african american",
                    "asian",
                    "american indian or alaska native",
                    "native hawaiian or other pacific islander",
                    "not reported"
            );
            case "VITAL_STATUS" -> Arrays.asList(
                    "Alive",
                    "Dead",
                    "Not Reported"
            );
            default -> throw new IllegalArgumentException(categoricalAttribute);
        };
    }
    
    FilterParams addSliceConstraint(FilterParams baseParams, String categoricalAttribute, String value) {
        FilterParams params = baseParams.deepClone();
        List<String> targetList = switch (categoricalAttribute) {
            case "SEX" -> params.gender;
            case "PRIMARY_DIAGNOSIS" -> params.primaryDiagnosis;
            case "ETHNICITY" -> params.ethnicity;
            case "RACE" -> params.race;
            case "VITAL_STATUS" -> params.vitalStatus;
            default -> throw new IllegalArgumentException(categoricalAttribute);
        };
        targetList.add(value);
        return params;
    }
    
    FilterParams addBinConstraint(FilterParams baseParams, String numericalAttribute, Range range) {
        FilterParams params = baseParams.deepClone();
        if (range.start != null) {
            params.ageAtDiagnosisMin = range.start.intValueExact() + 1;
        }
        if (range.end != null) {
            params.ageAtDiagnosisMax = range.end.intValueExact();
        }
        return params;
    }
}
