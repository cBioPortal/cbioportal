package org.cbioportal.application.file.export.exporters;

import org.cbioportal.application.file.export.services.ClinicalAttributeDataService;
import org.cbioportal.application.file.model.ClinicalAttribute;
import org.cbioportal.application.file.utils.CloseableIterator;

import java.util.SequencedMap;

public class ClinicalSampleAttributesMetadataAndDataExporter extends ClinicalAttributesMetadataAndDataExporter {

    public ClinicalSampleAttributesMetadataAndDataExporter(ClinicalAttributeDataService clinicalDataAttributeDataService) {
        super(clinicalDataAttributeDataService);
    }

    @Override
    public String getDatatype() {
        return "SAMPLE_ATTRIBUTES";
    }

    @Override
    protected CloseableIterator<SequencedMap<ClinicalAttribute, String>> getData(String studyId) {
        return clinicalDataAttributeDataService.getClinicalSampleAttributeData(studyId);
    }
}
