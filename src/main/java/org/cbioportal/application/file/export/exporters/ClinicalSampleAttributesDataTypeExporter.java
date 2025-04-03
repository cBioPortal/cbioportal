package org.cbioportal.application.file.export.exporters;

import org.cbioportal.application.file.export.services.ClinicalAttributeDataService;
import org.cbioportal.application.file.model.ClinicalAttribute;
import org.cbioportal.application.file.model.ClinicalAttributesMetadata;
import org.cbioportal.application.file.model.ClinicalAttributesTable;
import org.cbioportal.application.file.model.ClinicalSampleAttributeValue;
import org.cbioportal.application.file.utils.CloseableIterator;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Export metadata and data for clinical sample attributes.
 */
public class ClinicalSampleAttributesDataTypeExporter extends DataTypeExporter<ClinicalAttributesMetadata, ClinicalAttributesTable> {

    private final ClinicalAttributeDataService clinicalDataAttributeDataService;

    public ClinicalSampleAttributesDataTypeExporter(ClinicalAttributeDataService clinicalDataAttributeDataService) {
        this.clinicalDataAttributeDataService = clinicalDataAttributeDataService;
    }

    @Override
    protected Optional<ClinicalAttributesMetadata> getMetadata(String studyId) {
        if (!clinicalDataAttributeDataService.hasClinicalSampleAttributes(studyId)) {
            return Optional.empty();
        }
        return Optional.of(new ClinicalAttributesMetadata(studyId, "CLINICAL", "SAMPLE_ATTRIBUTES"));
    }

    @Override
    protected ClinicalAttributesTable getData(String studyId) {
        List<ClinicalAttribute> clinicalSampleAttributes = new ArrayList<>();
        clinicalSampleAttributes.add(ClinicalAttribute.PATIENT_ID);
        clinicalSampleAttributes.add(ClinicalAttribute.SAMPLE_ID);
        clinicalSampleAttributes.addAll(clinicalDataAttributeDataService.getClinicalSampleAttributes(studyId));
        CloseableIterator<ClinicalSampleAttributeValue> clinicalSampleAttributeValues = clinicalDataAttributeDataService.getClinicalSampleAttributeValues(studyId);
        return new ClinicalAttributesTable(clinicalSampleAttributes, clinicalSampleAttributeValues);
    }
}
