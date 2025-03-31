package org.cbioportal.application.file.export.exporters;

import org.cbioportal.application.file.export.services.GeneticProfileService;
import org.cbioportal.application.file.export.services.MafRecordService;
import org.cbioportal.application.file.export.writers.KeyValueMetadataWriter;
import org.cbioportal.application.file.export.writers.MafRecordWriter;
import org.cbioportal.application.file.model.GenericProfileDatatypeMetadata;
import org.cbioportal.application.file.model.GeneticProfile;
import org.cbioportal.application.file.model.MafRecord;
import org.cbioportal.application.file.utils.CloseableIterator;
import org.cbioportal.application.file.utils.FileWriterFactory;

import java.io.IOException;
import java.io.Writer;
import java.util.List;

public class MAFMetadataAndDataExporter implements Exporter {

    private final MafRecordService mafRecordService;
    private final GeneticProfileService geneticProfileService;

    public MAFMetadataAndDataExporter(MafRecordService mafRecordService, GeneticProfileService geneticProfileService) {
        this.mafRecordService = mafRecordService;
        this.geneticProfileService = geneticProfileService;
    }    

    //TODO Refactor
    //TODO Add support for multiple genetic profiles of the same datatype
    @Override
    public boolean exportData(FileWriterFactory fileWriterFactory, String studyId) {
        boolean exported = false;
        List<GeneticProfile> geneticProfiles = geneticProfileService.getGeneticProfiles(studyId);
        for (GeneticProfile geneticProfile : geneticProfiles) {
            if ("MAF".equals(geneticProfile.getDatatype())) {
                CloseableIterator<MafRecord> mafRecordIterator = mafRecordService.getMafRecords(geneticProfile.getStableId());
                if (mafRecordIterator.hasNext()) {
                    GenericProfileDatatypeMetadata genericProfileDatatypeMetadata = new GenericProfileDatatypeMetadata(
                        geneticProfile.getStableId().replace(studyId + "_", ""),
                        //TODO Use mol. alteration type and datatype from the map above instead
                        geneticProfile.getGeneticAlterationType(),
                        geneticProfile.getDatatype(),
                        studyId,
                        "data_mutations.txt",
                        geneticProfile.getName(),
                        geneticProfile.getDescription(),
                        //TODO where to get gene panel from?
                        null,
                        geneticProfile.getShowProfileInAnalysisTab());
                    try (Writer mafMetaWriter = fileWriterFactory.newWriter("meta_mutations.txt")) {
                        new KeyValueMetadataWriter(mafMetaWriter).write(genericProfileDatatypeMetadata);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }

                    try (Writer mafDataWriter = fileWriterFactory.newWriter("data_mutations.txt")) {
                        MafRecordWriter mafRecordWriter = new MafRecordWriter(mafDataWriter);
                        mafRecordWriter.write(mafRecordIterator);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    exported = true;
                }
            }
        }
        return exported;
    }

   /* private final MafRecordService mafRecordService;

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
        *//**
         * String stableId, String geneticAlterationType, String datatype, String cancerStudyIdentifier, String dataFilename, String profileName, String profileDescription, String genePanel, Boolean showProfileInAnalysisTab
         *//*
        //GeneticProfile
        return new GenericProfileDatatypeMetadata("profile", getGeneticAlterationType(), getDatatype(), studyId, getDataFilename(), "profileName", "profileDescription", "genePanel", true);
    }

    @Override
    protected void writeMetadata(Writer writer, GenericProfileDatatypeMetadata metadata) {
        new KeyValueMetadataWriter(writer).write(metadata);
    }*/
}
