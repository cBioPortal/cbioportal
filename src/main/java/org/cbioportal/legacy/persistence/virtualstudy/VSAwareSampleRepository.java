package org.cbioportal.legacy.persistence.virtualstudy;

import org.cbioportal.legacy.model.Sample;
import org.cbioportal.legacy.model.meta.BaseMeta;
import org.cbioportal.legacy.persistence.SampleRepository;
import org.cbioportal.legacy.service.VirtualStudyService;
import org.cbioportal.legacy.web.parameter.Direction;
import org.cbioportal.legacy.web.parameter.Projection;
import org.cbioportal.legacy.web.parameter.VirtualStudy;
import org.cbioportal.legacy.web.parameter.sort.SampleSortBy;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class VSAwareSampleRepository implements SampleRepository {

    private SampleRepository sampleRepository;
    private VirtualStudyService virtualStudyService;

    public VSAwareSampleRepository(VirtualStudyService virtualStudyService, SampleRepository sampleRepository) {
        this.virtualStudyService = virtualStudyService;
        this.sampleRepository = sampleRepository;
    }

    @Override
    public List<Sample> getAllSamples(String keyword, List<String> studyIds, String projection, Integer pageSize, Integer pageNumber, String sort, String direction) {
        Stream<Sample> resultStream = fetchSamples(studyIds, null, projection).stream();
        if (keyword != null) {
            resultStream = resultStream.filter(sample -> sample.getStableId().toLowerCase().contains(keyword.toLowerCase()));
        }

        if (sort != null) {
            resultStream = resultStream.sorted(composeComparator(sort, direction));
        }

        if (pageSize != null && pageNumber != null) {
            resultStream = resultStream.skip((long) pageSize * pageNumber).limit(pageSize);
        }
        return resultStream.toList();
    }

    private Comparator<Sample> composeComparator(String sortBy, String direction) {
        SampleSortBy ca = SampleSortBy.valueOf(sortBy);
        Comparator<Sample> result = switch (ca) {
            case sampleId -> Comparator.comparing(Sample::getStableId);
            case sampleType -> Comparator.comparing(Sample::getSampleType);
        };
        if (direction == null) {
            return result;
        } else {
            Direction d = Direction.valueOf(direction.toUpperCase());
            return d == Direction.ASC ? result : result.reversed();
        }
    }

    @Override
    public BaseMeta getMetaSamples(String keyword, List<String> studyIds) {
        BaseMeta baseMeta = new BaseMeta();
        baseMeta.setTotalCount(sampleRepository.getAllSamples(keyword, studyIds, Projection.ID.name(), null, null, null, null).size());
        return baseMeta;
    }

    @Override
    public List<Sample> getAllSamplesInStudy(String studyId, String projection, Integer pageSize, Integer pageNumber, String sortBy, String direction) {
        return getAllSamples(null, List.of(studyId), projection, pageSize, pageNumber, sortBy, direction);
    }

    @Override
    public BaseMeta getMetaSamplesInStudy(String studyId) {
        BaseMeta baseMeta = new BaseMeta();
        baseMeta.setTotalCount(sampleRepository.getAllSamplesInStudy(studyId, Projection.ID.name(), null, null, null, null).size());
        return baseMeta;
    }

    @Override
    public List<Sample> getAllSamplesInStudies(List<String> studyIds, String projection, Integer pageSize, Integer pageNumber, String sortBy, String direction) {
        return getAllSamples(null, studyIds, projection, pageSize, pageNumber, sortBy, direction);
    }

    @Override
    public Sample getSampleInStudy(String studyId, String sampleId) {
        return fetchSamples(List.of(studyId), List.of(sampleId), Projection.DETAILED.name()).stream()
            .findFirst()
            .orElse(null);
    }

    @Override
    public List<Sample> getAllSamplesOfPatientInStudy(String studyId, String patientId, String projection, Integer pageSize, Integer pageNumber, String sortBy, String direction) {
        return getAllSamplesInStudy(studyId, projection, pageSize, pageNumber, sortBy, direction).stream()
            .filter(sample -> sample.getPatientStableId().equals(patientId))
            .toList();
    }

    @Override
    public BaseMeta getMetaSamplesOfPatientInStudy(String studyId, String patientId) {
        BaseMeta baseMeta = new BaseMeta();
        baseMeta.setTotalCount(sampleRepository.getAllSamplesOfPatientInStudy(studyId, patientId, Projection.ID.name(), null, null, null, null).size());
        return baseMeta;
    }

    @Override
    public List<Sample> getAllSamplesOfPatientsInStudy(String studyId, List<String> patientIds, String projection) {
        return getAllSamplesInStudy(studyId, projection, null, null, null, null).stream()
            .filter(sample -> patientIds.contains(sample.getPatientStableId()))
            .toList();
    }

    @Override
    public List<Sample> getSamplesOfPatientsInMultipleStudies(List<String> studyIds, List<String> patientIds, String projection) {
        return getAllSamplesInStudies(studyIds, projection, null, null, null, null).stream()
            .filter(sample -> patientIds.contains(sample.getPatientStableId()))
            .toList();
    }

    @Override
    public List<Sample> fetchSamples(List<String> studyIds, List<String> sampleIds, String projection) {
        List<VirtualStudy> virtualStudies = virtualStudyService.getPublishedVirtualStudies();
        Set<String> virtualStudyIds = virtualStudies.stream().map(VirtualStudy::getId).collect(Collectors.toSet());
        if (Collections.disjoint(studyIds, virtualStudyIds)) {
            return sampleRepository.fetchSamples(studyIds, sampleIds, projection);
        }

        //TODO To continue hit http://localhost:8080/study/summary?id=682f42bab3068c45cf69e028 
        // mvn compile spring-boot:run -Dspring-boot.run.arguments="--security.cors.allowed-origins='*' --spring.datasource.url=jdbc:mysql://localhost:3306/cbioportal?useSSL=false&allowPublicKeyRetrieval=true --authenticate=false --dynamic_study_export_mode=true --spring.datasource.username=cbio_user --spring.datasource.password=somepassword --vs_mode=true --session.endpoint.publisher-api-key=TEST" -Dspring-boot.run.jvmArguments="-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5005"
        Map<String, Set<String>> studyToSample = new LinkedHashMap<>();
        if (studyIds.size() == sampleIds.size()) {
            for (int i = 0; i < studyIds.size(); i++) {
                String studyId = studyIds.get(i);
                String sampleId = sampleIds.get(i);
                if (!studyToSample.containsKey(studyId)) {
                    studyToSample.put(studyId, new LinkedHashSet<>());
                }
                studyToSample.get(studyId).add(sampleId);
            }
        } else {
            throw new IllegalArgumentException("Can't interpret the combination of the studyIds (" + String.join(", ", studyIds) + ") and sampleIds (" + String.join(", ", sampleIds) + ").");
        }

        Map<String, Set<String>> studyToSample2 = new LinkedHashMap<>();
        for (Map.Entry<String, Set<String>> entry : studyToSample.entrySet()) {
            String studyId = entry.getKey();
            Set<String> studySampleIds = entry.getValue();
            if (virtualStudyIds.contains(studyId)) {
                VirtualStudy virtualStudy = virtualStudies.stream().filter(vs -> vs.getId().equals(studyId)).toList().getFirst();
                virtualStudy.getData().getStudies().forEach((virtualStudySamples -> {
                    virtualStudySamples.getId();
                }));

            } else {
                studyToSample2.put(studyId, studySampleIds);
            }
        }

        return List.of();
    }

    @Override
    public List<Sample> fetchSamplesBySampleListIds(List<String> sampleListIds, String projection) {
        return sampleRepository.fetchSamplesBySampleListIds(sampleListIds, projection);
    }

    @Override
    public List<Sample> fetchSampleBySampleListId(String sampleListIds, String projection) {
        return sampleRepository.fetchSampleBySampleListId(sampleListIds, projection);
    }

    @Override
    public BaseMeta fetchMetaSamples(List<String> studyIds, List<String> sampleIds) {
        BaseMeta baseMeta = new BaseMeta();
        baseMeta.setTotalCount(sampleRepository.fetchSamples(studyIds, sampleIds, Projection.ID.name()).size());
        return baseMeta;
    }

    @Override
    public BaseMeta fetchMetaSamples(List<String> sampleListIds) {
        return sampleRepository.fetchMetaSamples(sampleListIds);
    }

    @Override
    public List<Sample> getSamplesByInternalIds(List<Integer> internalIds) {
        return sampleRepository.getSamplesByInternalIds(internalIds);
    }
}
