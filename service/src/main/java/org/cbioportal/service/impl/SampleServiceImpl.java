package org.cbioportal.service.impl;

import org.cbioportal.model.CopyNumberSeg;
import org.cbioportal.model.Sample;
import org.cbioportal.model.meta.BaseMeta;
import org.cbioportal.persistence.CopyNumberSegmentRepository;
import org.cbioportal.persistence.SampleListRepository;
import org.cbioportal.persistence.SampleRepository;
import org.cbioportal.service.PatientService;
import org.cbioportal.service.SampleService;
import org.cbioportal.service.StudyService;
import org.cbioportal.service.exception.PatientNotFoundException;
import org.cbioportal.service.exception.SampleNotFoundException;
import org.cbioportal.service.exception.StudyNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class SampleServiceImpl implements SampleService {
    private static final String SEGMENT = "_segments";
    private static final String SEQUENCED = "_sequenced";
    
    @Autowired
    private SampleRepository sampleRepository;
    @Autowired
    private StudyService studyService;
    @Autowired
    private PatientService patientService;
    @Autowired
    private SampleListRepository sampleListRepository;
    @Autowired
    private CopyNumberSegmentRepository copyNumberSegmentRepository;

    @Override
    @PreAuthorize("hasPermission(#studyId, 'CancerStudy', 'read')")
    public List<Sample> getAllSamplesInStudy(String studyId, String projection, Integer pageSize, Integer pageNumber,
                                             String sortBy, String direction) throws StudyNotFoundException {
        
        studyService.getStudy(studyId);
        List<Sample> samples = sampleRepository.getAllSamplesInStudy(studyId, projection, pageSize, pageNumber, sortBy, 
            direction);

        processSamples(samples, projection);
        return samples;
    }
    
    @Override
    @PreAuthorize("hasPermission(#studyId, 'CancerStudy', 'read')")
    public BaseMeta getMetaSamplesInStudy(String studyId) throws StudyNotFoundException {

        studyService.getStudy(studyId);

        return sampleRepository.getMetaSamplesInStudy(studyId);
    }

    @Override
    @PreAuthorize("hasPermission(#studyId, 'CancerStudy', 'read')")
    public Sample getSampleInStudy(String studyId, String sampleId) throws SampleNotFoundException, 
        StudyNotFoundException {

        studyService.getStudy(studyId);
        Sample sample = sampleRepository.getSampleInStudy(studyId, sampleId);

        if (sample == null) {
            throw new SampleNotFoundException(studyId, sampleId);
        }

        processSamples(Arrays.asList(sample), "DETAILED");
        return sample;
    }

    @Override
    @PreAuthorize("hasPermission(#studyId, 'CancerStudy', 'read')")
    public List<Sample> getAllSamplesOfPatientInStudy(String studyId, String patientId, String projection,
                                                      Integer pageSize, Integer pageNumber, String sortBy,
                                                      String direction) throws StudyNotFoundException, 
        PatientNotFoundException {
        
        patientService.getPatientInStudy(studyId, patientId);
        List<Sample> samples = sampleRepository.getAllSamplesOfPatientInStudy(studyId, patientId, projection, pageSize, 
            pageNumber, sortBy, direction);

        processSamples(samples, projection);
        return samples;
    }

    @Override
    @PreAuthorize("hasPermission(#studyId, 'CancerStudy', 'read')")
    public BaseMeta getMetaSamplesOfPatientInStudy(String studyId, String patientId) throws StudyNotFoundException, 
        PatientNotFoundException {

        patientService.getPatientInStudy(studyId, patientId);

        return sampleRepository.getMetaSamplesOfPatientInStudy(studyId, patientId);
    }

    @Override
    @PreAuthorize("hasPermission(#studyId, 'CancerStudy', 'read')")
    public List<Sample> getAllSamplesOfPatientsInStudy(String studyId, List<String> patientIds, String projection) {
        
        List<Sample> samples = sampleRepository.getAllSamplesOfPatientsInStudy(studyId, patientIds, projection);

        processSamples(samples, projection);
        return samples;
    }

    @Override
    @PreAuthorize("hasPermission(#studyIds, 'List<CancerStudyId>', 'read')")
    public List<Sample> fetchSamples(List<String> studyIds, List<String> sampleIds, String projection) {
        
        List<Sample> samples = sampleRepository.fetchSamples(studyIds, sampleIds, projection);
        processSamples(samples, projection);
        return samples;
    }

    @Override
    @PreAuthorize("hasPermission(#studyIds, 'List<CancerStudyId>', 'read')")
    public BaseMeta fetchMetaSamples(List<String> studyIds, List<String> sampleIds) {

        return sampleRepository.fetchMetaSamples(studyIds, sampleIds);
    }

    // this is not secured as it is only used interally by other services which have
    // already had permissions checked
    @Override
    public List<Sample> getSamplesByInternalIds(List<Integer> internalIds) {

        return sampleRepository.getSamplesByInternalIds(internalIds);
    }

    private void processSamples(List<Sample> samples, String projection) {
        
        if (projection.equals("DETAILED")) {
            Map<String, List<String>> sequencedSampleIdsMap = new HashMap<>();
            List<String> distinctStudyIds = samples.stream().map(Sample::getCancerStudyIdentifier).distinct()
                .collect(Collectors.toList());
            for (String studyId : distinctStudyIds) {
                sequencedSampleIdsMap.put(studyId, sampleListRepository.getAllSampleIdsInSampleList(studyId + SEQUENCED));
            }

            List<CopyNumberSeg> copyNumberSegs = copyNumberSegmentRepository.fetchCopyNumberSegments(
                distinctStudyIds.stream().map(p -> p + SEGMENT).collect(Collectors.toList()), samples.stream()
                    .map(Sample::getStableId).collect(Collectors.toList()), "ID");

            samples.forEach(sample -> {
                sample.setSequenced(sequencedSampleIdsMap.get(sample.getCancerStudyIdentifier())
                    .contains(sample.getStableId()));
                sample.setCopyNumberSegmentPresent(copyNumberSegs.stream().anyMatch(c -> c.getCancerStudyIdentifier()
                    .equals(sample.getCancerStudyIdentifier()) && c.getSampleStableId().equals(sample.getStableId())));
            });
        }
    }    
}
