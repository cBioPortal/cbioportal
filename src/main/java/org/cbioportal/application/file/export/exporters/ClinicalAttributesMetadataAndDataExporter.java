package org.cbioportal.application.file.export.exporters;

import org.cbioportal.application.file.export.services.ClinicalAttributeDataService;
import org.cbioportal.application.file.export.writers.ClinicalAttributeDataWriter;
import org.cbioportal.application.file.export.writers.KeyValueMetadataWriter;
import org.cbioportal.application.file.model.ClinicalAttribute;
import org.cbioportal.application.file.model.ClinicalAttributesMetadata;
import org.cbioportal.application.file.utils.CloseableIterator;

import java.io.Writer;
import java.util.SequencedMap;

public abstract class ClinicalAttributesMetadataAndDataExporter extends MetadataAndDataExporter<ClinicalAttributesMetadata, CloseableIterator<SequencedMap<ClinicalAttribute, String>>> {

    protected final ClinicalAttributeDataService clinicalDataAttributeDataService;

    public ClinicalAttributesMetadataAndDataExporter(ClinicalAttributeDataService clinicalDataAttributeDataService) {
        this.clinicalDataAttributeDataService = clinicalDataAttributeDataService;
    }

    @Override
    public String getGeneticAlterationType() {
        return "CLINICAL";
    }

    @Override
    protected void writeData(Writer writer, CloseableIterator<SequencedMap<ClinicalAttribute, String>> data) {
        new ClinicalAttributeDataWriter(writer).write(data);
    }

    @Override
    protected ClinicalAttributesMetadata getMetadata(String studyId) {
        return new ClinicalAttributesMetadata(studyId, getGeneticAlterationType(), getDatatype(), getDataFilename());
    }

    @Override
    protected void writeMetadata(Writer writer, ClinicalAttributesMetadata metadata) {
        new KeyValueMetadataWriter(writer).write(metadata);
    }
}
