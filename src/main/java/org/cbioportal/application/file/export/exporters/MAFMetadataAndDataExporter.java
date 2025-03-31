package org.cbioportal.application.file.export.exporters;

import org.cbioportal.application.file.export.services.MafRecordService;
import org.cbioportal.application.file.export.writers.KeyValueMetadataWriter;
import org.cbioportal.application.file.export.writers.MafRecordWriter;
import org.cbioportal.application.file.model.GenericProfileDatatypeMetadata;
import org.cbioportal.application.file.model.MafRecord;
import org.cbioportal.application.file.utils.CloseableIterator;

import java.io.Writer;

//TODO think how profile data types metaddata and data write has to be different
public class MAFMetadataAndDataExporter extends MetadataAndDataExporter<GenericProfileDatatypeMetadata, CloseableIterator<MafRecord>> {

    private final MafRecordService mafRecordService;

    public MAFMetadataAndDataExporter(MafRecordService mafRecordService) {
        this.mafRecordService = mafRecordService;
    }

    @Override
    public String getGeneticAlterationType() {
        return "MUTATION_EXTENDED";
    }

    @Override
    public String getDatatype() {
        return "MAF";
    }

    @Override
    protected CloseableIterator<MafRecord> getData(String studyId) {
        return mafRecordService.getMafRecords(studyId);
    }

    @Override
    protected void writeData(Writer writer, CloseableIterator<MafRecord> data) {
        new MafRecordWriter(writer).write(data);
    }

    @Override
    protected GenericProfileDatatypeMetadata getMetadata(String studyId) {
        /**
         * String stableId, String geneticAlterationType, String datatype, String cancerStudyIdentifier, String dataFilename, String profileName, String profileDescription, String genePanel, Boolean showProfileInAnalysisTab
         */
        //GeneticProfile
        return new GenericProfileDatatypeMetadata("profile", getGeneticAlterationType(), getDatatype(), studyId, getDataFilename(), "profileName", "profileDescription", "genePanel", true);
    }

    @Override
    protected void writeMetadata(Writer writer, GenericProfileDatatypeMetadata metadata) {
        new KeyValueMetadataWriter(writer).write(metadata);
    }
}
