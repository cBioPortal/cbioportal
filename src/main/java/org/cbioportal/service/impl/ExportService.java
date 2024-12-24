package org.cbioportal.service.impl;

import com.google.common.collect.Iterators;
import org.apache.commons.lang3.tuple.Pair;
import org.cbioportal.file.export.*;
import org.cbioportal.file.model.CancerStudyMetadata;
import org.cbioportal.file.model.CaseListMetadata;
import org.cbioportal.file.model.ClinicalAttributeData;
import org.cbioportal.file.model.ClinicalSampleAttributesMetadata;
import org.cbioportal.file.model.MafRecord;
import org.cbioportal.file.model.GenericProfileDatatypeMetadata;
import org.cbioportal.model.CancerStudy;
import org.cbioportal.model.MolecularProfile;
import org.cbioportal.model.Sample;
import org.cbioportal.model.SampleList;
import org.cbioportal.service.*;
import org.cbioportal.service.util.SessionServiceRequestHandler;
import org.cbioportal.web.parameter.VirtualStudy;
import org.cbioportal.web.parameter.VirtualStudyData;
import org.cbioportal.web.parameter.VirtualStudySamples;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ExportService {

    private final StudyService studyService;
    private final SessionServiceRequestHandler sessionServiceRequestHandler;
    private final SampleService sampleService;
    private final MolecularProfileService molecularProfileService;
    private final MafRecordFetcher mafRecordFetcher;
    private final ClinicalAttributeDataFetcher clinicalAttributeDataFetcher;
    private final SampleListService sampleListService;

    public ExportService(StudyService studyService,
                         SessionServiceRequestHandler sessionServiceRequestHandler,
                         SampleService sampleService,
                         MolecularProfileService molecularProfileService,
                         MafRecordFetcher mafRecordFetcher,
                         ClinicalAttributeDataFetcher clinicalAttributeDataFetcher,
                         SampleListService sampleListService) {
        this.studyService = studyService;
        this.sessionServiceRequestHandler = sessionServiceRequestHandler;
        this.sampleService = sampleService;
        this.molecularProfileService = molecularProfileService;
        this.mafRecordFetcher = mafRecordFetcher;
        this.clinicalAttributeDataFetcher = clinicalAttributeDataFetcher;
        this.sampleListService = sampleListService;
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
            ClinicalSampleAttributesMetadata clinicalSampleAttributesMetadata = new ClinicalSampleAttributesMetadata(
                studyId,
                "data_clinical_samples.txt"
            );
            try (Writer clinicalSampleMetadataWriter = fileWriterFactory.newWriter("meta_clinical_samples.txt")) {
                new KeyValueMetadataWriter(clinicalSampleMetadataWriter).write(clinicalSampleAttributesMetadata);
            }

            try (Writer clinicalSampleDataWriter = fileWriterFactory.newWriter("data_clinical_samples.txt")) {
                ClinicalAttributeDataWriter clinicalAttributeDataWriter = new ClinicalAttributeDataWriter(clinicalSampleDataWriter);
                clinicalAttributeDataWriter.write(clinicalAttributeData);
            }
        }

        List<String> studyIds = cancerStudyInfo.studyToSampleMap.keySet().stream().toList();
        Map<String, List<MolecularProfile>> molecularProfilesByStableId = this.molecularProfileService.getMolecularProfilesInStudies(studyIds, "SUMMARY").stream()
            .collect(Collectors.groupingBy(molecularProfile -> molecularProfile.getStableId().replace(molecularProfile.getCancerStudyIdentifier() + "_", "")));
        for (Map.Entry<String, List<MolecularProfile>> molecularProfiles : molecularProfilesByStableId.entrySet()) {
            String stableId = molecularProfiles.getKey();
            List<MolecularProfile> molecularProfileList = molecularProfiles.getValue();
            List<Pair<MolecularProfile.MolecularAlterationType, String>> molecularAlterationTypeToDatatype = molecularProfileList.stream()
                .map(molecularProfile -> Pair.of(molecularProfile.getMolecularAlterationType(), molecularProfile.getDatatype()))
                    .distinct().toList();
            if (molecularAlterationTypeToDatatype.size() > 1) {
                throw new IllegalStateException("Molecular profiles with the same stable Id ("
                    + stableId + ") have different molecular alteration types and datatypes:" + molecularAlterationTypeToDatatype);
            }
            if (Pair.of(MolecularProfile.MolecularAlterationType.MUTATION_EXTENDED, "MAF").equals(molecularAlterationTypeToDatatype.getFirst())) {
                Iterator<MafRecord> mafRecordIterator = Iterators.concat(molecularProfileList.stream()
                    .map(molecularProfile -> mafRecordFetcher.fetch(cancerStudyInfo.studyToSampleMap, molecularProfile.getStableId())).iterator());
                if (mafRecordIterator.hasNext()) {
                    GenericProfileDatatypeMetadata genericProfileDatatypeMetadata = new GenericProfileDatatypeMetadata(
                        stableId,
                        //TODO Use mol. alteration type and datatype from the map above instead
                        MolecularProfile.MolecularAlterationType.MUTATION_EXTENDED.toString(),
                        "MAF",
                        studyId,
                        "data_mutations.txt",
                        molecularProfileList.getFirst().getName(),
                        molecularProfileList.getFirst().getDescription(),
                        //TODO where to get gene panel from?
                        Optional.empty(),
                        //Is it true for all data types?
                        true
                    );
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
        List<SampleList> sampleLists = sampleListService.getAllSampleListsInStudies(studyIds, "DETAILED");
        Map<String, List<SampleList>> sampleListsBySuffix = sampleLists.stream()
            .map(sl -> {
                sl.getSampleIds().retainAll(cancerStudyInfo.studyToSampleMap.get(sl.getCancerStudyIdentifier()));
                return sl;
            })
            .filter(sl -> !sl.getSampleIds().isEmpty())
            .collect(Collectors.groupingBy(sampleList -> sampleList.getStableId().replace(sampleList.getCancerStudyIdentifier(), "")));
        for (Map.Entry<String, List<SampleList>> entry: sampleListsBySuffix.entrySet()) {
            String suffix = entry.getKey();
            //we skip this one as we have addGlobalCaseList=true for study
            if ("_all".equals(suffix)) {
                continue;
            }
            List<SampleList> suffixedSampleLists = entry.getValue();
            String newStableId = cancerStudyInfo.metadata.cancerStudyIdentifier() + suffix;
            SortedSet<String> mergedSapleIds = suffixedSampleLists.stream()
                .flatMap(sl -> sl.getSampleIds().stream())
                .collect(Collectors.toCollection(TreeSet::new));
            try (Writer caseListWriter = fileWriterFactory.newWriter("case_lists/cases" + suffix + ".txt")) {
                new KeyValueMetadataWriter(caseListWriter).write(new CaseListMetadata(
                    studyId,
                    newStableId,
                    //TODO Sometime name/description could contain number of samples from the original study
                    //maybe composing its own name and description would work better
                    suffixedSampleLists.getFirst().getName(),
                    suffixedSampleLists.getFirst().getDescription(),
                    mergedSapleIds
                ));
            }
        }
    }

    record CancerStudyInfo(
        CancerStudyMetadata metadata,
        Map<String, Set<String>> studyToSampleMap
    ) {
    }

    ;

    private CancerStudyInfo getCancerStudyInfo(String studyId) {
        List<CancerStudy> studies = studyService.fetchStudies(List.of(studyId), "DETAILED");
        Map<String, Set<String>> studyToSampleMap = new HashMap<>();
        CancerStudyMetadata cancerStudyMetadata;
        if (studies.isEmpty()) {
            VirtualStudy virtualStudy = sessionServiceRequestHandler.getVirtualStudyById(studyId);
            if (virtualStudy == null) {
                throw new IllegalArgumentException("Study not found: " + studyId);
            }
            VirtualStudyData virtualStudyData = virtualStudy.getData();
            //TODO take care of refreshing sample ids for dynamic virtual studies
            studyToSampleMap.putAll(
                virtualStudyData.getStudies().stream().collect(Collectors.toMap(VirtualStudySamples::getId, VirtualStudySamples::getSamples)));
            cancerStudyMetadata = new CancerStudyMetadata(
                //TODO type of cancer could be calculated based on the type of cancer of the studies in the virtual study
                virtualStudyData.getTypeOfCancerId() == null ? "mixed" : virtualStudyData.getTypeOfCancerId(),
                studyId,
                virtualStudyData.getName(),
                virtualStudyData.getDescription(),
                Optional.empty(),
                Optional.ofNullable(virtualStudyData.getPmid()),
                Optional.empty(),
                Optional.of(true),
                Optional.empty(),
                Optional.empty()
            );
        } else {
            List<Sample> samples = sampleService.getAllSamplesInStudies(List.of(studyId), "ID", null, null, null, null);
            studyToSampleMap.put(studyId, samples.stream().map(Sample::getStableId).collect(Collectors.toSet()));
            CancerStudy cancerStudy = studies.getFirst();
            cancerStudyMetadata = new CancerStudyMetadata(
                cancerStudy.getTypeOfCancerId(),
                cancerStudy.getCancerStudyIdentifier(),
                cancerStudy.getName(),
                cancerStudy.getDescription(),
                Optional.ofNullable(cancerStudy.getCitation()),
                Optional.ofNullable(cancerStudy.getPmid()),
                Optional.ofNullable(cancerStudy.getGroups()),
                Optional.of(true),
                //TODO export study tags
                Optional.empty(),
                Optional.ofNullable(cancerStudy.getReferenceGenome())
            );
        }
        return new CancerStudyInfo(cancerStudyMetadata, studyToSampleMap);
    }
}
