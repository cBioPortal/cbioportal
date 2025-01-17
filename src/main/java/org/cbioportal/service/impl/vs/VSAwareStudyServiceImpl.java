package org.cbioportal.service.impl.vs;

import org.cbioportal.model.CancerStudy;
import org.cbioportal.model.CancerStudyTags;
import org.cbioportal.model.meta.BaseMeta;
import org.cbioportal.persistence.PersistenceConstants;
import org.cbioportal.service.ReadPermissionService;
import org.cbioportal.service.StudyService;
import org.cbioportal.service.exception.StudyNotFoundException;
import org.cbioportal.utils.security.AccessLevel;
import org.springframework.security.core.Authentication;

import java.util.Comparator;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

public class VSAwareStudyServiceImpl implements StudyService {

    private final StudyService studyService;

    private final ReadPermissionService readPermissionService;
    private final PublishedVirtualStudyService publishedVirtualStudyService;

    public VSAwareStudyServiceImpl(StudyService studyService, PublishedVirtualStudyService publishedVirtualStudyService, ReadPermissionService readPermissionService) {
        this.studyService = studyService;
        this.publishedVirtualStudyService = publishedVirtualStudyService;
        this.readPermissionService = readPermissionService;
    }

    @Override
    public List<CancerStudy> getAllStudies(String keyword, String projection, Integer pageSize, Integer pageNumber, String sortBy, String direction, Authentication authentication, AccessLevel accessLevel) {
        List<CancerStudy> materialisedStudies = studyService.getAllStudies(keyword, projection, null, null, null, null, authentication, accessLevel);
        List<CancerStudy> virtualStudies = publishedVirtualStudyService.getPublishedVirtualStudiesByKeyword(keyword).stream().map(publishedVirtualStudyService::toCancerStudy).toList();

        Stream<CancerStudy> resultStream = Stream.concat(
            materialisedStudies.stream(),
            virtualStudies.stream()
        );

        if (sortBy != null) {
            resultStream = resultStream.sorted(buildComparator(sortBy, direction));
        }

        if (pageSize != null && pageNumber != null) {
            resultStream = resultStream.skip((long) pageSize * pageNumber).limit(pageSize);
        }

        List<CancerStudy> result = resultStream.toList();
        readPermissionService.setReadPermission(result, authentication);
        return result;
    }

    private static Comparator<CancerStudy> buildComparator(String sortBy, String direction) {
        Function<CancerStudy, Comparable> getValue;
        //TODO add more fields to sort by
        if (sortBy.equalsIgnoreCase("name")) {
            getValue = CancerStudy::getName;
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
    public BaseMeta getMetaStudies(String keyword) {
        BaseMeta baseMeta = new BaseMeta();
        baseMeta.setTotalCount(getAllStudies(keyword, PersistenceConstants.ID_PROJECTION, null, null, null, null, null, AccessLevel.READ).size());
        return baseMeta;
    }

    @Override
    public CancerStudy getStudy(String studyId) throws StudyNotFoundException {
        List<CancerStudy> foundStudies = fetchStudies(List.of(studyId), PersistenceConstants.DETAILED_PROJECTION);
        if (foundStudies.isEmpty()) {
            throw new StudyNotFoundException(studyId);
        }
        return foundStudies.getFirst();
    }

    @Override
    public List<CancerStudy> fetchStudies(List<String> studyIds, String projection) {
        List<CancerStudy> materialisedStudies = studyService.fetchStudies(studyIds, projection);
        List<String> notFoundStudyIds = materialisedStudies.stream().map(CancerStudy::getCancerStudyIdentifier).filter(studyId -> !studyIds.contains(studyId)).toList();
        List<CancerStudy> virtualStudies = publishedVirtualStudyService.getAllPublishedVirtualStudies().stream()
            .filter(vs -> notFoundStudyIds.contains(vs.getId()))
            .map(publishedVirtualStudyService::toCancerStudy)
            .toList(); 
        return Stream.concat(materialisedStudies.stream(), virtualStudies.stream()).toList();
    }

    @Override
    public BaseMeta fetchMetaStudies(List<String> studyIds) {
        BaseMeta baseMeta = new BaseMeta();
        baseMeta.setTotalCount(fetchStudies(studyIds, PersistenceConstants.ID_PROJECTION).size());
        return baseMeta;
    }

    @Override
    public CancerStudyTags getTags(String studyId, AccessLevel accessLevel) {
        //TODO do virtual studies inherit tags from the queried studies?
        return studyService.getTags(studyId, accessLevel);
    }

    @Override
    public List<CancerStudyTags> getTagsForMultipleStudies(List<String> studyIds) {
        //TODO do virtual studies inherit tags from the queried studies?
        return studyService.getTagsForMultipleStudies(studyIds);
    }
}
