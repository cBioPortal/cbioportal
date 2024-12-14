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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringWriter;
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
            StringWriter mafRecordsStringWriter = new StringWriter();
            MafRecordWriter mafRecordWriter = new MafRecordWriter(mafRecordsStringWriter);
            //TODO do not produce the file if no data has been retrieved
            mafRecordWriter.write(mafRecordFetcher.fetch(studyToSampleMap));
            addFileToZip(zipOutputStream, "data_mutation.txt", mafRecordsStringWriter.toString().getBytes());
        }
    }

    private void addFileToZip(ZipOutputStream zipOutputStream, String fileName, byte[] fileContent) throws IOException {
        // Create a new ZIP entry for the file
        ZipEntry zipEntry = new ZipEntry(fileName);
        zipOutputStream.putNextEntry(zipEntry);

        // Write file content
        zipOutputStream.write(fileContent);
        zipOutputStream.closeEntry();
    }
}
