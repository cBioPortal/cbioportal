package org.cbioportal.service.impl.vs;

import org.cbioportal.model.CancerStudy;
import org.cbioportal.model.CancerStudyTags;
import org.cbioportal.model.meta.BaseMeta;
import org.cbioportal.service.ReadPermissionService;
import org.cbioportal.service.StudyService;
import org.cbioportal.service.exception.StudyNotFoundException;
import org.cbioportal.service.util.SessionServiceRequestHandler;
import org.cbioportal.utils.security.AccessLevel;
import org.cbioportal.web.parameter.VirtualStudy;
import org.cbioportal.web.parameter.VirtualStudyData;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.Authentication;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Function;
import java.util.stream.Stream;

public class VSAwareStudyServiceImpl implements StudyService {
    
    private final StudyService studyService;

    private final SessionServiceRequestHandler sessionServiceRequestHandler;
    
    private final ReadPermissionService readPermissionService;
    private final Executor asyncExecutor;

    public VSAwareStudyServiceImpl(StudyService studyService, SessionServiceRequestHandler sessionServiceRequestHandler, ReadPermissionService readPermissionService, Executor asyncExecutor) {
        this.studyService = studyService;
        this.sessionServiceRequestHandler = sessionServiceRequestHandler;
        this.readPermissionService = readPermissionService;
        this.asyncExecutor = asyncExecutor;
    }
    
    @Override
    public List<CancerStudy> getAllStudies(String keyword, String projection, Integer pageSize, Integer pageNumber, String sortBy, String direction, Authentication authentication, AccessLevel accessLevel) {
        CompletableFuture<List<CancerStudy>> materialisedStudies = getMaterialisedStudiesAsync(keyword, projection, authentication, accessLevel);
        CompletableFuture<List<CancerStudy>> virtualStudies = getVirtualStudiesAsync(keyword);

        Stream<CancerStudy> resultStream = Stream.concat(
            materialisedStudies.join().stream(),
            virtualStudies.join().stream() 
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

    @Async
    private CompletableFuture<List<CancerStudy>> getVirtualStudiesAsync(String keyword) {
        return CompletableFuture.supplyAsync(() -> sessionServiceRequestHandler.getVirtualStudiesAccessibleToUser("*").stream()
                .map(VSAwareStudyServiceImpl::toCancerStudy).filter(cs -> shouldSelect(cs, keyword)).toList(), asyncExecutor);
    }

    @Async
    private CompletableFuture<List<CancerStudy>> getMaterialisedStudiesAsync(String keyword, String projection, Authentication authentication, AccessLevel accessLevel) {
        return CompletableFuture.supplyAsync(() -> studyService.getAllStudies(keyword, projection, null, null, null, null, authentication, accessLevel), asyncExecutor);
    }

    private static CancerStudy toCancerStudy(VirtualStudy vs) {
        VirtualStudyData vsd = vs.getData();
        CancerStudy cs = new CancerStudy();
        cs.setCancerStudyIdentifier(vs.getId());
        cs.setName(vsd.getName());
        cs.setDescription(vsd.getDescription());
        cs.setPmid(vsd.getPmid());
        return cs;
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

    private static boolean shouldSelect(CancerStudy cs, String keyword) {
        //TODO improve the search. The keyword can be also sent to mongo to search for virtual studies
        return cs.getName().toLowerCase().contains(keyword.toLowerCase());
    }

    @Override
    public BaseMeta getMetaStudies(String keyword) {
        BaseMeta baseMeta = new BaseMeta();
        baseMeta.setTotalCount(getAllStudies(keyword, "ID", null, null, null, null, null, AccessLevel.READ).size());
        return baseMeta;
    }

    @Override
    public CancerStudy getStudy(String studyId) throws StudyNotFoundException {
        CompletableFuture<Optional<CancerStudy>> materialisedStudy = getMaterialisedStudyAsync(studyId);
        CompletableFuture<Optional<CancerStudy>> virtualStudy = getVirtualStudyAsync(studyId);
        return firstPresent(materialisedStudy, virtualStudy).join().orElseThrow(() -> new StudyNotFoundException(studyId));
    }

    @Async
    private CompletableFuture<Optional<CancerStudy>> getVirtualStudyAsync(String studyId) {
        return CompletableFuture.supplyAsync(() -> Optional.ofNullable(sessionServiceRequestHandler.getVirtualStudyById(studyId)).map(VSAwareStudyServiceImpl::toCancerStudy), asyncExecutor);
    }

    @Async
    private CompletableFuture<Optional<CancerStudy>> getMaterialisedStudyAsync(String studyId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return Optional.of(studyService.getStudy(studyId));
            } catch (StudyNotFoundException e) {
                return Optional.empty();
            }
        }, asyncExecutor);
    }

    private static <T> CompletableFuture<Optional<T>> firstPresent(
        CompletableFuture<Optional<T>> f1,
        CompletableFuture<Optional<T>> f2
    ) {
        return CompletableFuture.anyOf(
            f1.thenApply(optional -> optional.isPresent() ? optional : null),
            f2.thenApply(optional -> optional.isPresent() ? optional : null)
        ).thenCompose(result -> {
            if (result != null) {
                return CompletableFuture.completedFuture((Optional<T>) result);
            }
            // If the first completed result was empty, wait for the other
            return f1.isDone() ? f2 : f1;
        });
    }

    @Override
    public List<CancerStudy> fetchStudies(List<String> studyIds, String projection) {
        List<CancerStudy> materialisedStudies = studyService.fetchStudies(studyIds, projection);
        List<String> notFoundStudyIds = materialisedStudies.stream().map(CancerStudy::getCancerStudyIdentifier).filter(studyId -> !studyIds.contains(studyId)).toList();
        //TODO implement using completable futures. It must be better for IO bound operations
        List<CancerStudy> virtualStudies = notFoundStudyIds.parallelStream().map(sessionServiceRequestHandler::getVirtualStudyById).map(VSAwareStudyServiceImpl::toCancerStudy).toList();
        return Stream.concat(materialisedStudies.stream(), virtualStudies.stream()).toList();
    }

    @Override
    public BaseMeta fetchMetaStudies(List<String> studyIds) {
        BaseMeta baseMeta = new BaseMeta();
        baseMeta.setTotalCount(fetchStudies(studyIds, "ID").size());
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
