package org.cbioportal.application.file.export;

import com.google.common.collect.Iterators;
import org.apache.commons.lang3.tuple.Pair;
import org.cbioportal.application.file.model.CancerStudyMetadata;
import org.cbioportal.domain.cancerstudy.usecase.GetCancerStudyMetadataUseCase;
import org.cbioportal.application.file.model.CaseListMetadata;
import org.cbioportal.application.file.model.ClinicalAttributeData;
import org.cbioportal.application.file.model.ClinicalSampleAttributesMetadata;
import org.cbioportal.application.file.model.MafRecord;
import org.cbioportal.application.file.model.GenericProfileDatatypeMetadata;
import org.cbioportal.domain.generic_assay.usecase.GetFilteredMolecularProfilesByAlterationType;
import org.cbioportal.domain.sample.Sample;
import org.cbioportal.domain.sample.SampleList;
import org.cbioportal.domain.sample.usecase.GetFilteredSamplesUseCase;
import org.cbioportal.domain.sample.usecase.GetStudiesSampleListsUseCase;
import org.cbioportal.domain.studyview.StudyViewFilterContext;
import org.cbioportal.legacy.model.MolecularProfile;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ExportService {

    private final GetCancerStudyMetadataUseCase getCancerStudyMetadata;
    private final GetFilteredSamplesUseCase getFilteredSamplesUseCase;
    private final GetFilteredMolecularProfilesByAlterationType getFilteredMolecularProfilesByAlterationType;
    private final GetStudiesSampleListsUseCase getStudiesSampleListsUseCase;
    private final MafRecordFetcher mafRecordFetcher;
    private final ClinicalAttributeDataFetcher clinicalAttributeDataFetcher;

    public ExportService(GetCancerStudyMetadataUseCase getCancerStudyMetadata,
                         GetFilteredSamplesUseCase getFilteredSamplesUseCase,
                         GetFilteredMolecularProfilesByAlterationType getFilteredMolecularProfilesByAlterationType,
                         GetStudiesSampleListsUseCase getStudiesSampleListsUseCase,
                         MafRecordFetcher mafRecordFetcher,
                         ClinicalAttributeDataFetcher clinicalAttributeDataFetcher) {
        this.getCancerStudyMetadata = getCancerStudyMetadata;
        this.getFilteredSamplesUseCase = getFilteredSamplesUseCase;
        this.getFilteredMolecularProfilesByAlterationType = getFilteredMolecularProfilesByAlterationType;
        this.getStudiesSampleListsUseCase = getStudiesSampleListsUseCase;
        this.mafRecordFetcher = mafRecordFetcher;
        this.clinicalAttributeDataFetcher = clinicalAttributeDataFetcher;
    }

    public void exportStudyData(FileWriterFactory fileWriterFactory, String studyId) throws IOException {
        CancerStudyInfo cancerStudyInfo = getCancerStudyInfo(studyId);
        try (Writer studyMetadataWriter = fileWriterFactory.newWriter("meta_study.txt")) {
            new KeyValueMetadataWriter(studyMetadataWriter).write(cancerStudyInfo.metadata);
        }

        // TODO detect what data types are available for a study and export them
        // by iterating over the available data types and calling the appropriate fetchers and writers
        // the boiler plate code below should be replaced by the above logic

        ClinicalAttributeData clinicalAttributeData = clinicalAttributeDataFetcher.fetch(cancerStudyInfo.studyToSampleMap);
        if (clinicalAttributeData.rows().hasNext()) {
            ClinicalSampleAttributesMetadata clinicalSampleAttributesMetadata = new ClinicalSampleAttributesMetadata(studyId, "data_clinical_samples.txt");
            try (Writer clinicalSampleMetadataWriter = fileWriterFactory.newWriter("meta_clinical_samples.txt")) {
                new KeyValueMetadataWriter(clinicalSampleMetadataWriter).write(clinicalSampleAttributesMetadata);
            }

            try (Writer clinicalSampleDataWriter = fileWriterFactory.newWriter("data_clinical_samples.txt")) {
                ClinicalAttributeDataWriter clinicalAttributeDataWriter = new ClinicalAttributeDataWriter(clinicalSampleDataWriter);
                clinicalAttributeDataWriter.write(clinicalAttributeData);
            }
        }

        List<String> studyIds = cancerStudyInfo.studyToSampleMap.keySet().stream().toList();
        Map<String, List<MolecularProfile>> molecularProfilesByStableId = this.getFilteredMolecularProfilesByAlterationType.execute(
            StudyViewFilterContext.builder().studyIds(List.of(studyId)).customDataFilterCancerStudies(List.of(studyId)).build(),
            MolecularProfile.MolecularAlterationType.MUTATION_EXTENDED.name()).stream().collect(Collectors.groupingBy(molecularProfile -> molecularProfile.getStableId().replace(molecularProfile.getCancerStudyIdentifier() + "_", "")));
        for (Map.Entry<String, List<MolecularProfile>> molecularProfiles : molecularProfilesByStableId.entrySet()) {
            String stableId = molecularProfiles.getKey();
            List<MolecularProfile> molecularProfileList = molecularProfiles.getValue();
            List<Pair<MolecularProfile.MolecularAlterationType, String>> molecularAlterationTypeToDatatype = molecularProfileList.stream().map(molecularProfile -> Pair.of(molecularProfile.getMolecularAlterationType(), molecularProfile.getDatatype())).distinct().toList();
            if (molecularAlterationTypeToDatatype.size() > 1) {
                throw new IllegalStateException("Molecular profiles with the same stable Id (" + stableId + ") have different molecular alteration types and datatypes:" + molecularAlterationTypeToDatatype);
            }
            if (Pair.of(MolecularProfile.MolecularAlterationType.MUTATION_EXTENDED, "MAF").equals(molecularAlterationTypeToDatatype.getFirst())) {
                Iterator<MafRecord> mafRecordIterator = Iterators.concat(molecularProfileList.stream().map(molecularProfile -> mafRecordFetcher.fetch(cancerStudyInfo.studyToSampleMap, molecularProfile.getStableId())).iterator());
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
        }
    }

    record CancerStudyInfo(CancerStudyMetadata metadata, Map<String, Set<String>> studyToSampleMap) {
    }

    ;

    private CancerStudyInfo getCancerStudyInfo(String studyId) {
        Optional<org.cbioportal.domain.cancerstudy.CancerStudyMetadata> savedCancerStudyMetadata = this.getCancerStudyMetadata.execute(studyId);
        if (savedCancerStudyMetadata.isEmpty()) {
            throw new IllegalArgumentException("Cancer study with id " + studyId + " not found");
        }
        org.cbioportal.domain.cancerstudy.CancerStudyMetadata cancerStudy = savedCancerStudyMetadata.get();
        Map<String, Set<String>> studyToSampleMap = new HashMap<>();
        CancerStudyMetadata cancerStudyMetadata;
        List<Sample> samples = this.getFilteredSamplesUseCase.execute(StudyViewFilterContext.builder().studyIds(List.of(studyId)).customDataFilterCancerStudies(List.of(studyId)).build());
        studyToSampleMap.put(studyId, samples.stream().map(Sample::stableId).collect(Collectors.toSet()));
        cancerStudyMetadata = new CancerStudyMetadata(cancerStudy.typeOfCancerId(), cancerStudy.cancerStudyIdentifier(), cancerStudy.name(), cancerStudy.description(), Optional.ofNullable(cancerStudy.citation()), Optional.ofNullable(cancerStudy.pmid()), Optional.ofNullable(cancerStudy.groups()),
            // add global case list
            Optional.of(true),
            //TODO export study tags
            Optional.empty(), Optional.ofNullable(cancerStudy.referenceGenome()));
        return new CancerStudyInfo(cancerStudyMetadata, studyToSampleMap);
    }
}
