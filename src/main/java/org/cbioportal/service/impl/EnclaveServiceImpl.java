package org.cbioportal.service.impl;

import org.cbioportal.model.ClinicalAttribute;
import org.cbioportal.model.ClinicalDataBin;
import org.cbioportal.model.ClinicalDataCount;
import org.cbioportal.model.ClinicalDataCountItem;
import org.cbioportal.service.EnclaveService;
import org.cbioportal.web.parameter.DataBinMethod;
import org.cbioportal.web.parameter.Projection;
import org.cbioportal.web.parameter.StudyViewFilter;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
public class EnclaveServiceImpl implements EnclaveService {
    
    @Override
    public List<ClinicalDataCountItem> fetchEnclaveClinicalDataCounts(
        List<String> attributes,
        StudyViewFilter studyViewFilter) {
            
        List<ClinicalDataCountItem> result = new ArrayList<>();

        for (String attribute : attributes) {
            // TODO just mocking out the response for now
            List<String> uniqueValues = Arrays.asList("Male", "Female");

            ClinicalDataCountItem it = new ClinicalDataCountItem();
            List<ClinicalDataCount> counts = new ArrayList<>();
            for (String value : uniqueValues) {
                ClinicalDataCount count = new ClinicalDataCount();
                count.setAttributeId(attribute);
                count.setValue(value);
                count.setCount(500);
                counts.add(count);
            }
            
            it.setAttributeId(attribute);
            it.setCounts(counts);
            result.add(it);
        }

        return result;
    }

    @Override
    public List<ClinicalDataBin> fetchEnclaveClinicalDataBinCounts(
        DataBinMethod dataBinMethod,
        List<String> attributes,
        StudyViewFilter studyViewFilter) {
            
        List<ClinicalDataBin> result = new ArrayList<>();

        for (String attribute : attributes) {
            // TODO just mocking out the response for now
            ClinicalDataBin lowBin = new ClinicalDataBin();
            lowBin.setSpecialValue("<=");
            lowBin.setEnd(BigDecimal.valueOf(20));
            lowBin.setCount(4);
            lowBin.setAttributeId(attribute);

            ClinicalDataBin midBin = new ClinicalDataBin();
            midBin.setStart(BigDecimal.valueOf(20));
            midBin.setEnd(BigDecimal.valueOf(30));
            midBin.setCount(5);
            midBin.setAttributeId(attribute);

            ClinicalDataBin highBin = new ClinicalDataBin();
            highBin.setSpecialValue(">");
            highBin.setStart(BigDecimal.valueOf(30));
            highBin.setCount(6);
            highBin.setAttributeId(attribute);

            ClinicalDataBin naBin = new ClinicalDataBin();
            naBin.setSpecialValue("NA");
            naBin.setCount(7);
            naBin.setAttributeId(attribute);

            result.add(lowBin);
            result.add(midBin);
            result.add(highBin);
            result.add(naBin);
        }

        return result;
    }

    @Override
    public List<ClinicalAttribute> fetchEnclaveClinicalAttributes(Projection projection) {
        // categorical attribute -- pie chart
        ClinicalAttribute sexAttr = new ClinicalAttribute();
        sexAttr.setAttrId("SEX");
        sexAttr.setDescription("Sex");
        sexAttr.setDisplayName("Sex");
        sexAttr.setDatatype("STRING");
        sexAttr.setPatientAttribute(true);
        sexAttr.setPriority("1");
        sexAttr.setCancerStudyIdentifier("enclave_2024");

        // numerical attribute -- histogram
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
            ageAttr
        );
    }
}
