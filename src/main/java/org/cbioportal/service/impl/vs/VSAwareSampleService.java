package org.cbioportal.service.impl.vs;

import org.cbioportal.model.Sample;
import org.cbioportal.model.SampleList;
import org.cbioportal.model.meta.BaseMeta;
import org.cbioportal.persistence.PersistenceConstants;
import org.cbioportal.service.SampleListService;
import org.cbioportal.service.SampleService;
import org.cbioportal.service.exception.PatientNotFoundException;
import org.cbioportal.service.exception.SampleNotFoundException;
import org.cbioportal.service.exception.StudyNotFoundException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

public class VSAwareSampleService implements SampleService {

    private final SampleService sampleService;
    private final PublishedVirtualStudyService publishedVirtualStudyService;
    private final SampleListService sampleListService;

    public VSAwareSampleService(SampleService sampleService, PublishedVirtualStudyService publishedVirtualStudyService, SampleListService sampleListService) {
        this.sampleService = sampleService;
        this.publishedVirtualStudyService = publishedVirtualStudyService;
        this.sampleListService = sampleListService;
    }

    @Override
    public List<Sample> getAllSamplesInStudy(String studyId, String projection, Integer pageSize, Integer pageNumber, String sortBy, String direction) throws StudyNotFoundException {
        return getAllSamplesInStudies(List.of(studyId), projection, pageSize, pageNumber, sortBy, direction);
    }

    private static Comparator<Sample> buildComparator(String sortBy, String direction) {
        Function<Sample, Comparable> getValue;
        //TODO add more fields to sort by
        if (sortBy.equalsIgnoreCase("sampleType")) {
            getValue = Sample::getSampleType;
        } else {
            throw new IllegalArgumentException("Invalid sortBy value: " + sortBy);
        }
        if (direction != null && direction.equalsIgnoreCase("desc")) {
            return Comparator.comparing(getValue).reversed();
        }
        if (direction != null && direction.equalsIgnoreCase("asc")) {
            return Comparator.comparing(getValue);
        }
        throw new IllegalArgumentException("Invalid direction value: " + direction);
    }

    @Override
    public BaseMeta getMetaSamplesInStudy(String studyId) throws StudyNotFoundException {
        BaseMeta baseMeta = new BaseMeta();
        baseMeta.setTotalCount(getAllSamplesInStudy(studyId, null, null, null, null, null).size());
        return baseMeta;
    }

    @Override
    public List<Sample> getAllSamplesInStudies(List<String> studyIds, String projection, Integer pageSize, Integer pageNumber, String sortBy, String direction) {
        List<Sample> materializedSamples = sampleService.getAllSamplesInStudies(studyIds, projection, null, null, null, null);
        List<Sample> virtualSamples = publishedVirtualStudyService.getAllPublishedVirtualStudies().stream()
            .flatMap(vs ->
                vs.getData()
                    .getStudies().stream().flatMap(vss ->
                        sampleService.fetchSamples(List.of(vss.getId()), new ArrayList<>(vss.getSamples()), PersistenceConstants.SUMMARY_PROJECTION).stream())
                    .map(
                        sample -> {
                            sample.setCancerStudyIdentifier(vs.getId());
                            return sample;
                        }
                    )
                    //TODO should we keep duplicate samples separated by study instead of merging?
                    .distinct()).toList();

        Stream<Sample> resultStream = Stream.concat(
            materializedSamples.stream(),
            virtualSamples.stream()
        );

        if (sortBy != null) {
            resultStream = resultStream.sorted(buildComparator(sortBy, direction));
        }

        if (pageSize != null && pageNumber != null) {
            resultStream = resultStream.skip((long) pageSize * pageNumber).limit(pageSize);
        }

        return resultStream.toList();
    }

    @Override
    public Sample getSampleInStudy(String studyId, String sampleId) throws SampleNotFoundException {
        //TODO improve performance
        return getAllSamplesInStudies(List.of(studyId), null, null, null, null, null).stream()
            .filter(sample -> sample.getStableId().equals(sampleId))
            .findFirst()
            .orElseThrow(() -> new SampleNotFoundException(studyId, sampleId));
    }

    @Override
    public List<Sample> getAllSamplesOfPatientInStudy(String studyId, String patientId, String projection, Integer pageSize, Integer pageNumber, String sortBy, String direction) throws StudyNotFoundException, PatientNotFoundException {
        //TODO improve performance
        return getAllSamplesInStudies(List.of(studyId), projection, pageSize, pageNumber, sortBy, direction).stream()
            .filter(sample -> sample.getPatientStableId().equals(patientId))
            .toList();
    }

    @Override
    public BaseMeta getMetaSamplesOfPatientInStudy(String studyId, String patientId) throws StudyNotFoundException, PatientNotFoundException {
        BaseMeta baseMeta = new BaseMeta();
        baseMeta.setTotalCount(getAllSamplesOfPatientInStudy(studyId, patientId, null, null, null, null, null).size());
        return baseMeta;
    }

    @Override
    public List<Sample> getAllSamplesOfPatientsInStudy(String studyId, List<String> patientIds, String projection) {
        return getSamplesOfPatientsInMultipleStudies(List.of(studyId), patientIds, projection);
    }

    @Override
    public List<Sample> getSamplesOfPatientsInMultipleStudies(List<String> studyIds, List<String> patientIds, String projection) {
        return getAllSamplesInStudies(studyIds, projection, null, null, null, null).stream()
            .filter(sample -> patientIds.contains(sample.getPatientStableId()))
            .toList();
    }

    @Override
    public List<Sample> fetchSamples(List<String> studyIds, List<String> sampleIds, String projection) {
        return getAllSamplesInStudies(studyIds, projection, null, null, null, null).stream()
            .filter(sample -> sampleIds.contains(sample.getStableId()))
            .toList();
    }

    @Override
    public List<Sample> fetchSamples(List<String> sampleListIds, String projection) {
        List<SampleList> sampleLists = sampleListService.fetchSampleLists(sampleListIds, PersistenceConstants.ID_PROJECTION);
        return sampleLists.stream()
            .flatMap(sampleList -> fetchSamples(List.of(sampleList.getCancerStudyIdentifier()), sampleList.getSampleIds(), projection).stream())
            .toList();
    }

    @Override
    public BaseMeta fetchMetaSamples(List<String> studyIds, List<String> sampleIds) {
        BaseMeta baseMeta = new BaseMeta();
        baseMeta.setTotalCount(fetchSamples(studyIds, sampleIds, PersistenceConstants.ID_PROJECTION).size());
        return baseMeta;
    }

    @Override
    public BaseMeta fetchMetaSamples(List<String> sampleListIds) {
        BaseMeta baseMeta = new BaseMeta();
        baseMeta.setTotalCount(fetchSamples(sampleListIds, PersistenceConstants.ID_PROJECTION).size());
        return baseMeta;
    }

    @Override
    public List<Sample> getSamplesByInternalIds(List<Integer> internalIds) {
        return sampleService.getSamplesByInternalIds(internalIds);
    }

    @Override
    public List<Sample> getAllSamples(String keyword, List<String> studyIds, String projection, Integer pageSize, Integer pageNumber, String sort, String direction) {
        String[] keywords = keyword.trim().split("\\s+");
        return getAllSamplesInStudies(studyIds, projection, pageSize, pageNumber, sort, direction).stream()
            .filter(sample -> Arrays.stream(keywords).anyMatch(k -> sample.getStableId().contains(k))).toList();
    }

    @Override
    public BaseMeta getMetaSamples(String keyword, List<String> studyIds) {
        BaseMeta baseMeta = new BaseMeta();
        baseMeta.setTotalCount(getAllSamples(keyword, studyIds, PersistenceConstants.ID_PROJECTION, null, null, null, null).size());
        return baseMeta;
    }
}
