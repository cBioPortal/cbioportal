package org.cbioportal.application.file.export;

import org.cbioportal.application.file.model.CancerStudyMetadata;
import org.cbioportal.application.file.model.ClinicalAttribute;
import org.cbioportal.application.file.model.ClinicalSampleAttributesMetadata;
import org.cbioportal.application.file.model.LongTable;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.Writer;

public class ExportService {

    private final CancerStudyMetadataService cancerStudyMetadataService;
    private final ClinicalAttributeDataService clinicalDataAttributeDataService;

    public ExportService(
            CancerStudyMetadataService cancerStudyMetadataService,
            ClinicalAttributeDataService clinicalDataAttributeDataService) {
        this.cancerStudyMetadataService = cancerStudyMetadataService;
        this.clinicalDataAttributeDataService = clinicalDataAttributeDataService;
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

        try (LongTable<ClinicalAttribute, String> clinicalAttributeData = clinicalDataAttributeDataService.getClinicalSampleAttributeData(studyId)) {
            if (clinicalAttributeData.hasNext()) {
                ClinicalSampleAttributesMetadata clinicalSampleAttributesMetadata = new ClinicalSampleAttributesMetadata(studyId, "data_clinical_samples.txt");
                try (Writer clinicalSampleMetadataWriter = fileWriterFactory.newWriter("meta_clinical_samples.txt")) {
                    new KeyValueMetadataWriter(clinicalSampleMetadataWriter).write(clinicalSampleAttributesMetadata);
                }

                try (Writer clinicalSampleDataWriter = fileWriterFactory.newWriter("data_clinical_samples.txt")) {
                    ClinicalAttributeDataWriter clinicalAttributeDataWriter = new ClinicalAttributeDataWriter(clinicalSampleDataWriter);
                    clinicalAttributeDataWriter.write(clinicalAttributeData);
                }
            }
        }

      /*  Iterator<MafRecord> mafRecordIterator = Iterators.concat(molecularProfileList.stream().map(molecularProfile -> mafRecordFetcher.fetch(cancerStudyInfo.studyToSampleMap, molecularProfile.getStableId())).iterator());
        if (mafRecordIterator.hasNext()) {
            GenericProfileDatatypeMetadata genericProfileDatatypeMetadata = new GenericProfileDatatypeMetadata(stableId,
                    //TODO Use mol. alteration type and datatype from the map above instead
                    MolecularProfile.MolecularAlterationType.MUTATION_EXTENDED.toString(), "MAF", studyId, "data_mutations.txt", molecularProfileList.getFirst().getName(), molecularProfileList.getFirst().getDescription(),
                    //TODO where to get gene panel from?
                    Optional.empty(),
                    //Is it true for all data types?
                    true);
            try (Writer mafMetaWriter = fileWriterFactory.newWriter("meta_mutations.txt")) {
                new KeyValueMetadataWriter(mafMetaWriter).write(genericProfileDatatypeMetadata);
            }

            try (Writer mafDataWriter = fileWriterFactory.newWriter("data_mutations.txt")) {
                MafRecordWriter mafRecordWriter = new MafRecordWriter(mafDataWriter);
                mafRecordWriter.write(mafRecordIterator);
            }
        }

        //TODO Move logic to newly created case list fetcher
        List<SampleList> sampleLists = getStudiesSampleListsUseCase.execute(studyIds);
        Map<String, List<SampleList>> sampleListsBySuffix = sampleLists.stream().map(sl -> {
            sl.getSampleStableIds().retainAll(cancerStudyInfo.studyToSampleMap.get(sl.getCancerStudyStableId()));
            return sl;
        }).filter(sl -> !sl.getSampleStableIds().isEmpty()).collect(Collectors.groupingBy(sampleList -> sampleList.getStableId().replace(sampleList.getCancerStudyStableId(), "")));
        for (Map.Entry<String, List<SampleList>> entry : sampleListsBySuffix.entrySet()) {
            String suffix = entry.getKey();
            //we skip this one as we have addGlobalCaseList=true for study
            if ("_all".equals(suffix)) {
                continue;
            }
            List<SampleList> suffixedSampleLists = entry.getValue();
            String newStableId = cancerStudyInfo.metadata.cancerStudyIdentifier() + suffix;
            SortedSet<String> mergedSapleIds = suffixedSampleLists.stream().flatMap(sl -> sl.getSampleStableIds().stream()).collect(Collectors.toCollection(TreeSet::new));
            try (Writer caseListWriter = fileWriterFactory.newWriter("case_lists/cases" + suffix + ".txt")) {
                new KeyValueMetadataWriter(caseListWriter).write(new CaseListMetadata(studyId, newStableId,
                    //TODO Sometime name/description could contain number of samples from the original study
                    //maybe composing its own name and description would work better
                    suffixedSampleLists.getFirst().getName(), suffixedSampleLists.getFirst().getDescription(), mergedSapleIds));
            }
        }*/
    }
}
