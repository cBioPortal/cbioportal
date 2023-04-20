package org.cbioportal.web.response;

import org.cbioportal.model.ClinicalData;
import org.cbioportal.model.ClinicalDataCollection;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.ArrayList;
import java.util.List;

public class PaginatedClinicalData {

    public static class ClinicalDataPage extends PageImpl<ClinicalData> {
        public ClinicalDataPage(List<ClinicalData> content, Pageable pageable, long total) {
            super(content, pageable, total);
        }
    }

    /**
     * Paginated resource
     */
    public ClinicalDataPage samplePage;

    /**
     * Patient info associated with paginated samples
     */
    private List<ClinicalData> patientClinicalData = new ArrayList<>();

    public PaginatedClinicalData(
        ClinicalDataCollection clinicalDataCollection,
        int pageNumber,
        int pageSize,
        int total
    ) {
        Pageable params = PageRequest.of(pageNumber, pageSize);
        this.samplePage = new ClinicalDataPage(clinicalDataCollection.getSampleClinicalData(), params, total);
        this.patientClinicalData = clinicalDataCollection.getPatientClinicalData();
    }

    public PaginatedClinicalData() {}

    public ClinicalDataPage getSamplePage() {
        return samplePage;
    }

    public void setSamplePage(ClinicalDataPage samplePage) {
        this.samplePage = samplePage;
    }

    public List<ClinicalData> getPatientClinicalData() {
        return patientClinicalData;
    }

    public void setPatientClinicalData(List<ClinicalData> patientClinicalData) {
        this.patientClinicalData = patientClinicalData;
    }
}
