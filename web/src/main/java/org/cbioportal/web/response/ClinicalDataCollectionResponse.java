package org.cbioportal.web.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import org.cbioportal.model.ClinicalData;
import org.cbioportal.model.ClinicalDataCollection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.ArrayList;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ClinicalDataCollectionResponse {

    /**
     * Paginated resource
     */
    private Page<ClinicalData> sampleClinicalData = Page.empty();

    /**
     * Patient info associated with paginated samples
     */
    private List<ClinicalData> patientClinicalData = new ArrayList<>();

    public ClinicalDataCollectionResponse(
        ClinicalDataCollection clinicalDataCollection,
        int pageNumber,
        int pageSize, 
        int total
    ) {
        Pageable params = PageRequest.of(pageNumber, pageSize);
        this.sampleClinicalData = new PageImpl<>(clinicalDataCollection.getSampleClinicalData(), params, total);
        this.patientClinicalData = clinicalDataCollection.getPatientClinicalData();
    }

    public ClinicalDataCollectionResponse() {}

    public Page<ClinicalData> getSampleClinicalData() {
        return sampleClinicalData;
    }

    public void setSampleClinicalData(Page<ClinicalData> sampleClinicalData) {
        this.sampleClinicalData = sampleClinicalData;
    }

    public List<ClinicalData> getPatientClinicalData() {
        return patientClinicalData;
    }

    public void setPatientClinicalData(List<ClinicalData> patientClinicalData) {
        this.patientClinicalData = patientClinicalData;
    }
}
