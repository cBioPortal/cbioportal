package org.cbioportal.application.file.export.services;

import org.cbioportal.application.file.export.writers.ClinicalAttributeDataWriter;
import org.cbioportal.application.file.export.writers.KeyValueMetadataWriter;
import org.cbioportal.application.file.export.writers.MafRecordWriter;
import org.cbioportal.application.file.model.CancerStudyMetadata;
import org.cbioportal.application.file.model.CaseListMetadata;
import org.cbioportal.application.file.model.ClinicalAttribute;
import org.cbioportal.application.file.model.ClinicalAttributesMetadata;
import org.cbioportal.application.file.model.GenericProfileDatatypeMetadata;
import org.cbioportal.application.file.model.GeneticProfile;
import org.cbioportal.application.file.model.MafRecord;
import org.cbioportal.application.file.utils.CloseableIterator;
import org.cbioportal.application.file.utils.FileWriterFactory;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.Writer;
import java.util.List;
import java.util.SequencedMap;

//TODO do I use file DTO in mybatis layer or not? Be consistent
public class ExportService {

    private final CancerStudyMetadataService cancerStudyMetadataService;
    private final ClinicalAttributeDataService clinicalDataAttributeDataService;
    private final GeneticProfileService geneticProfileService;
    private final MafRecordService mafRecordService;
    private final CaseListMetadataService caseListMetadataService;

    public ExportService(
        CancerStudyMetadataService cancerStudyMetadataService,
        ClinicalAttributeDataService clinicalDataAttributeDataService,
        GeneticProfileService geneticProfileService,
        MafRecordService mafRecordService,
        CaseListMetadataService caseListMetadataService
    ) {
        this.cancerStudyMetadataService = cancerStudyMetadataService;
        this.clinicalDataAttributeDataService = clinicalDataAttributeDataService;
        this.geneticProfileService = geneticProfileService;
        this.mafRecordService = mafRecordService;
        this.caseListMetadataService = caseListMetadataService;
    }

    @Transactional
    public void exportStudyData(FileWriterFactory fileWriterFactory, String studyId) throws IOException {
        CancerStudyMetadata cancerStudyInfo = cancerStudyMetadataService.getCancerStudyMetadata(studyId);
        try (Writer studyMetadataWriter = fileWriterFactory.newWriter("meta_study.txt")) {
            new KeyValueMetadataWriter(studyMetadataWriter).write(cancerStudyInfo);
        }

        // TODO detect what data types are available for a study and export them
        // by iterating over the available data types and calling the appropriate fetchers and writers
        // the boiler plate code below should be replaced by the above logic

        try (CloseableIterator<SequencedMap<ClinicalAttribute, String>> clinicalAttributeData = clinicalDataAttributeDataService.getClinicalSampleAttributeData(studyId)) {
            if (clinicalAttributeData.hasNext()) {
                ClinicalAttributesMetadata clinicalAttributesMetadata = new ClinicalAttributesMetadata(studyId, "", "", "data_clinical_samples.txt");
                try (Writer clinicalSampleMetadataWriter = fileWriterFactory.newWriter("meta_clinical_samples.txt")) {
                    new KeyValueMetadataWriter(clinicalSampleMetadataWriter).write(clinicalAttributesMetadata);
                }

                try (Writer clinicalSampleDataWriter = fileWriterFactory.newWriter("data_clinical_samples.txt")) {
                    ClinicalAttributeDataWriter clinicalAttributeDataWriter = new ClinicalAttributeDataWriter(clinicalSampleDataWriter);
                    clinicalAttributeDataWriter.write(clinicalAttributeData);
                }
            }
        }

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
                    }

                    try (Writer mafDataWriter = fileWriterFactory.newWriter("data_mutations.txt")) {
                        MafRecordWriter mafRecordWriter = new MafRecordWriter(mafDataWriter);
                        mafRecordWriter.write(mafRecordIterator);
                    }
                }
            }
        }

        //TODO Move logic to newly created case list fetcher
        List<CaseListMetadata> sampleLists = caseListMetadataService.getCaseListsMetadata(studyId);
        for (CaseListMetadata sampleList : sampleLists) {
            //we skip this one as we have addGlobalCaseList=true for study
            if (sampleList.getStableId().endsWith("_all")) {
                continue;
            }
            try (Writer caseListWriter = fileWriterFactory.newWriter("case_lists/cases_" + sampleList.getStableId().replace(studyId + "_", "") + ".txt")) {
                new KeyValueMetadataWriter(caseListWriter).write(new CaseListMetadata(studyId, sampleList.getStableId(),
                    //TODO Sometime name/description could contain number of samples from the original study
                    //maybe composing its own name and description would work better
                    sampleList.getName(), sampleList.getDescription(), sampleList.getSampleIds()));
            }
        }
    }
}
