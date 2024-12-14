package org.cbioportal.service.impl;

import org.cbioportal.file.export.MafRecordFetcher;
import org.cbioportal.file.export.MafRecordWriter;
import org.cbioportal.model.CancerStudy;
import org.cbioportal.model.Sample;
import org.cbioportal.service.*;
import org.cbioportal.service.util.SessionServiceRequestHandler;
import org.cbioportal.web.parameter.VirtualStudy;
import org.cbioportal.web.parameter.VirtualStudySamples;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
public class ExportService {

    private final StudyService studyService;
    private final SessionServiceRequestHandler sessionServiceRequestHandler;
    private final SampleService sampleService;
    private final MafRecordFetcher mafRecordFetcher;

    public ExportService(StudyService studyService,
                         SessionServiceRequestHandler sessionServiceRequestHandler,
                         SampleService sampleService,
                         MafRecordFetcher mafRecordFetcher) {
        this.studyService = studyService;
        this.sessionServiceRequestHandler = sessionServiceRequestHandler;
        this.sampleService = sampleService;
        this.mafRecordFetcher = mafRecordFetcher;
    }
    
    public void exportStudyDataToZip(OutputStream outputStream, String studyId) throws IOException {
        List<CancerStudy> studies = studyService.fetchStudies(List.of(studyId), "DETAILED");
        Map<String, Set<String>> studyToSampleMap = new HashMap<>();
        if (studies.isEmpty()) {
           VirtualStudy virtualStudy = sessionServiceRequestHandler.getVirtualStudyById(studyId);
           studyToSampleMap.putAll(
               virtualStudy.getData().getStudies().stream().collect(Collectors.toMap(VirtualStudySamples::getId, VirtualStudySamples::getSamples)));
        } else {
           List<Sample> samples = sampleService.getAllSamplesInStudies(List.of(studyId), "ID", null, null, null, null);
           studyToSampleMap.put(studyId, samples.stream().map(Sample::getStableId).collect(Collectors.toSet()));
        }
        try (ZipOutputStream zipOutputStream = new ZipOutputStream(outputStream)) {
            // Add files to the ZIP
            ZipEntry zipEntry = new ZipEntry("data_mutation.txt");
            zipOutputStream.putNextEntry(zipEntry);
            MafRecordWriter mafRecordWriter = new MafRecordWriter(new OutputStreamWriter(zipOutputStream, StandardCharsets.UTF_8));
            //TODO do not produce the file if no data has been retrieved
            mafRecordWriter.write(mafRecordFetcher.fetch(studyToSampleMap));
            zipOutputStream.closeEntry();
        }
    }
}
