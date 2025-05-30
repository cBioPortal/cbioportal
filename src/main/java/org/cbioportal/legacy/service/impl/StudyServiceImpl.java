package org.cbioportal.legacy.service.impl;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.cbioportal.legacy.model.CancerStudy;
import org.cbioportal.legacy.model.CancerStudyTags;
import org.cbioportal.legacy.model.ResourceCount;
import org.cbioportal.legacy.model.TypeOfCancer;
import org.cbioportal.legacy.model.meta.BaseMeta;
import org.cbioportal.legacy.persistence.StudyRepository;
import org.cbioportal.legacy.service.CancerTypeService;
import org.cbioportal.legacy.service.ReadPermissionService;
import org.cbioportal.legacy.service.StudyService;
import org.cbioportal.legacy.service.exception.StudyNotFoundException;
import org.cbioportal.legacy.utils.security.AccessLevel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PostFilter;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
public class StudyServiceImpl implements StudyService {

  @Autowired private StudyRepository studyRepository;

  @Autowired private CancerTypeService cancerTypeService;

  @Autowired private ReadPermissionService readPermissionService;

  @Override
  @PostFilter("hasPermission(filterObject,#accessLevel)")
  public List<CancerStudy> getAllStudies(
      String keyword,
      String projection,
      Integer pageSize,
      Integer pageNumber,
      String sortBy,
      String direction,
      Authentication authentication,
      AccessLevel accessLevel) {

    List<CancerStudy> allStudies =
        studyRepository.getAllStudies(keyword, projection, pageSize, pageNumber, sortBy, direction);

    List<ResourceCount> resources = studyRepository.getResourcesForAllStudies();
    Map<String, CancerStudy> studyMap = allStudies.stream()
        .collect(Collectors.toMap(CancerStudy::getCancerStudyIdentifier, Function.identity()));

    for (CancerStudy study : allStudies) {
        study.setResources(new ArrayList<>());
    }
  
    for (ResourceCount rc : resources) {
        String cancerStudyIdentifier = rc.getCancerStudyIdentifier();
        CancerStudy study = studyMap.get(cancerStudyIdentifier);
        if (study != null) {
            study.getResources().add(rc);
        }
    }
  
    Map<String, CancerStudy> sortedAllStudiesByCancerStudyIdentifier =
        allStudies.stream()
            .collect(
                Collectors.toMap(
                    c -> c.getCancerStudyIdentifier(), c -> c, (e1, e2) -> e2, LinkedHashMap::new));
    if (keyword != null && (pageSize == null || allStudies.size() < pageSize)) {
      List<CancerStudy> primarySiteMatchingStudies = findPrimarySiteMatchingStudies(keyword);
      for (CancerStudy cancerStudy : primarySiteMatchingStudies) {
        if (!sortedAllStudiesByCancerStudyIdentifier.containsKey(
            cancerStudy.getCancerStudyIdentifier())) {
          sortedAllStudiesByCancerStudyIdentifier.put(
              cancerStudy.getCancerStudyIdentifier(), cancerStudy);
        }
        if (pageSize != null && sortedAllStudiesByCancerStudyIdentifier.size() == pageSize) {
          break;
        }
      }
    }

    // For authenticated portals it is essential to make a new list, such
    // that @PostFilter does not taint the list stored in the mybatis
    // second-level cache. When making changes to this make sure to copy the
    // allStudies list at least for the AUTHENTICATE.equals("true") case
    List<CancerStudy> returnedStudyObjects =
        sortedAllStudiesByCancerStudyIdentifier.values().stream().collect(Collectors.toList());

    // When using prop. 'skin.home_page.show_unauthorized_studies' this endpoint
    // returns the full list of studies, some of which can be accessed by the user.
    readPermissionService.setReadPermission(returnedStudyObjects, authentication);

    return returnedStudyObjects;
  }

  @Override
  public BaseMeta getMetaStudies(String keyword) {
    if (keyword == null) {
      return studyRepository.getMetaStudies(keyword);
    } else {
      BaseMeta baseMeta = new BaseMeta();
      baseMeta.setTotalCount(
          getAllStudies(keyword, "SUMMARY", null, null, null, null, null, AccessLevel.READ).size());
      return baseMeta;
    }
  }

  @Override
  public CancerStudy getStudy(String studyId) throws StudyNotFoundException {

    CancerStudy cancerStudy = studyRepository.getStudy(studyId, "DETAILED");
    if (cancerStudy == null) {
      throw new StudyNotFoundException(studyId);
    }
    
    List<ResourceCount> resources = studyRepository.getResources(studyId);
    cancerStudy.setResources(resources);

    return cancerStudy;
  }

  @Override
  public List<CancerStudy> fetchStudies(List<String> studyIds, String projection) {

    return studyRepository.fetchStudies(studyIds, projection);
  }

  @Override
  public BaseMeta fetchMetaStudies(List<String> studyIds) {

    return studyRepository.fetchMetaStudies(studyIds);
  }

  @Override
  @PreAuthorize("hasPermission(#studyId, 'CancerStudyId', #accessLevel)")
  public CancerStudyTags getTags(String studyId, AccessLevel accessLevel) {

    return studyRepository.getTags(studyId);
  }

  @Override
  public List<CancerStudyTags> getTagsForMultipleStudies(List<String> studyIds) {

    return studyRepository.getTagsForMultipleStudies(studyIds);
  }

  private List<CancerStudy> findPrimarySiteMatchingStudies(String keyword) {

    List<CancerStudy> matchingStudies = new ArrayList<>();

    List<String> matchingCancerTypes = new ArrayList<>();
    for (Map.Entry<String, TypeOfCancer> entry : cancerTypeService.getPrimarySiteMap().entrySet()) {
      if (entry.getValue().getTypeOfCancerId().toLowerCase().contains(keyword.toLowerCase())
          || entry.getValue().getName().toLowerCase().contains(keyword.toLowerCase())) {
        matchingCancerTypes.add(entry.getKey());
      }
    }
    if (!matchingCancerTypes.isEmpty()) {
      List<CancerStudy> allUnfilteredStudies =
          studyRepository.getAllStudies(null, "SUMMARY", null, null, null, null);
      for (CancerStudy cancerStudy : allUnfilteredStudies) {
        if (matchingCancerTypes.contains(cancerStudy.getTypeOfCancerId())) {
          matchingStudies.add(cancerStudy);
        }
      }
    }
    return matchingStudies;
  }
}
