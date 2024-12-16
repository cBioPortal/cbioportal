package org.cbioportal.service.impl;

import org.cbioportal.file.export.*;
import org.cbioportal.file.model.CancerStudyMetadata;
import org.cbioportal.file.model.ClinicalAttributeData;
import org.cbioportal.file.model.ClinicalSampleAttributesMetadata;
import org.cbioportal.file.model.MafRecord;
import org.cbioportal.file.model.MutationMetadata;
import org.cbioportal.model.CancerStudy;
import org.cbioportal.model.MolecularProfile;
import org.cbioportal.model.MolecularProfileCaseIdentifier;
import org.cbioportal.model.Sample;
import org.cbioportal.service.*;
import org.cbioportal.service.exception.MolecularProfileNotFoundException;
import org.cbioportal.service.util.SessionServiceRequestHandler;
import org.cbioportal.web.parameter.VirtualStudy;
import org.cbioportal.web.parameter.VirtualStudyData;
import org.cbioportal.web.parameter.VirtualStudySamples;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
public class ExportService {

    private final StudyService studyService;
    private final SessionServiceRequestHandler sessionServiceRequestHandler;
    private final SampleService sampleService;
    private final MolecularProfileService molecularProfileService;
    private final MafRecordFetcher mafRecordFetcher;
    private final ClinicalAttributeDataFetcher clinicalAttributeDataFetcher;

    public ExportService(StudyService studyService,
                         SessionServiceRequestHandler sessionServiceRequestHandler,
                         SampleService sampleService,
                         MolecularProfileService molecularProfileService,
                         MafRecordFetcher mafRecordFetcher,
                         ClinicalAttributeDataFetcher clinicalAttributeDataFetcher) {
        this.studyService = studyService;
        this.sessionServiceRequestHandler = sessionServiceRequestHandler;
        this.sampleService = sampleService;
        this.molecularProfileService = molecularProfileService;
        this.mafRecordFetcher = mafRecordFetcher;
        this.clinicalAttributeDataFetcher = clinicalAttributeDataFetcher;
    }

    public void exportStudyDataToZip(OutputStream outputStream, String studyId) throws IOException {
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
                virtualStudyData.getTypeOfCancerId(),
                studyId,
                virtualStudyData.getName(),
                virtualStudyData.getDescription(),
                Optional.empty(),
                Optional.ofNullable(virtualStudyData.getPmid()),
                Optional.empty(),
                Optional.empty(),
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
                Optional.empty(),
                //TODO export study tags
                Optional.empty(),
                Optional.ofNullable(cancerStudy.getReferenceGenome())
            );
        }
        try (ZipOutputStream zipOutputStream = new ZipOutputStream(outputStream)) {
            // Add files to the ZIP
            OutputStreamWriter writer = new OutputStreamWriter(zipOutputStream, StandardCharsets.UTF_8);

            zipOutputStream.putNextEntry(new ZipEntry("meta_study.txt"));
            new KeyValueMetadataWriter(writer).write(cancerStudyMetadata);
            zipOutputStream.closeEntry();

            // TODO detect what data types are available for a study and export them
            // by iterating over the available data types and calling the appropriate fetchers and writers
            // the boiler plate code below should be replaced by the above logic

            ClinicalAttributeData clinicalAttributeData = clinicalAttributeDataFetcher.fetch(studyToSampleMap);
            if (clinicalAttributeData.rows().hasNext()) {
                zipOutputStream.putNextEntry(new ZipEntry("meta_clinical_samples.txt"));
                ClinicalSampleAttributesMetadata clinicalSampleAttributesMetadata = new ClinicalSampleAttributesMetadata(
                    studyId,
                    "data_clinical_samples.txt"
                );
                new KeyValueMetadataWriter(writer).write(clinicalSampleAttributesMetadata);
                zipOutputStream.closeEntry();

                zipOutputStream.putNextEntry(new ZipEntry("data_clinical_samples.txt"));
                ClinicalAttributeDataWriter clinicalAttributeDataWriter = new ClinicalAttributeDataWriter(writer);
                clinicalAttributeDataWriter.write(clinicalAttributeData);
                zipOutputStream.closeEntry();
            }

            //TODO what happens here with virtual studies? Do we merge the data from all studies?
            List<MolecularProfileCaseIdentifier> molecularProfileCaseIdentifiers = studyToSampleMap.entrySet().stream().flatMap(entry -> molecularProfileService.getMolecularProfileCaseIdentifiers(List.of(entry.getKey()), List.copyOf(entry.getValue())).stream()).collect(Collectors.toList());
            for (MolecularProfileCaseIdentifier molecularProfileCaseIdentifier : molecularProfileCaseIdentifiers) {
                MolecularProfile molecularProfile = null;
                try {
                    molecularProfile = molecularProfileService.getMolecularProfile(molecularProfileCaseIdentifier.getMolecularProfileId());
                    MolecularProfile.MolecularAlterationType molecularAlterationType = molecularProfile.getMolecularAlterationType();
                    switch (molecularAlterationType) {
                        case MUTATION_EXTENDED -> {
                            Iterator<MafRecord> mafRecordIterator = mafRecordFetcher.fetch(studyToSampleMap);
                            if (mafRecordIterator.hasNext()) {
                                zipOutputStream.putNextEntry(new ZipEntry("meta_mutations.txt"));
                                MutationMetadata mutationMetadata = new MutationMetadata(
                                    studyId,
                                    "data_mutations.txt",
                                    molecularProfile.getName(),
                                    molecularProfile.getDescription(),
                                    //TODO where to get gene panel from?
                                    Optional.empty()
                                );
                                new KeyValueMetadataWriter(writer).write(mutationMetadata);
                                zipOutputStream.closeEntry();

                                zipOutputStream.putNextEntry(new ZipEntry("data_mutations.txt"));
                                MafRecordWriter mafRecordWriter = new MafRecordWriter(writer);
                                mafRecordWriter.write(mafRecordIterator);
                                zipOutputStream.closeEntry();
                            }
                        }
                    }
                } catch (MolecularProfileNotFoundException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }
}
