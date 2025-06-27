package org.cbioportal.legacy.persistence.virtualstudy;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.cbioportal.legacy.model.CancerStudy;
import org.cbioportal.legacy.model.ClinicalAttribute;
import org.cbioportal.legacy.model.ClinicalAttributeCount;
import org.cbioportal.legacy.model.meta.BaseMeta;
import org.cbioportal.legacy.persistence.ClinicalAttributeRepository;
import org.cbioportal.legacy.service.VirtualStudyService;
import org.cbioportal.legacy.web.parameter.Direction;
import org.cbioportal.legacy.web.parameter.Projection;
import org.cbioportal.legacy.web.parameter.sort.ClinicalAttributeSortBy;

public class VSAwareClinicalAttributeRepository implements ClinicalAttributeRepository {

  private final VirtualStudyService virtualStudyService;
  private final ClinicalAttributeRepository clinicalAttributeRepository;

  public VSAwareClinicalAttributeRepository(
      VirtualStudyService virtualStudyService,
      ClinicalAttributeRepository clinicalAttributeRepository) {
    this.virtualStudyService = virtualStudyService;
    this.clinicalAttributeRepository = clinicalAttributeRepository;
  }

  @Override
  public List<ClinicalAttribute> getAllClinicalAttributes(
      String projection, Integer pageSize, Integer pageNumber, String sortBy, String direction) {
    List<ClinicalAttribute> materialisedClinicalAttributes =
        clinicalAttributeRepository.getAllClinicalAttributes(projection, null, null, null, null);
    Map<String, List<ClinicalAttribute>> materialisedClinicalAttributesByAttrId =
        materialisedClinicalAttributes.stream()
            .collect(Collectors.groupingBy(ClinicalAttribute::getAttrId));
    List<ClinicalAttribute> virtualClinicalAttributes =
        virtualStudyService.getPublishedVirtualStudies().stream()
            .flatMap(
                virtualStudy -> {
                  CancerStudy virtualCancerStudy = virtualStudyService.toCancerStudy(virtualStudy);
                  List<String> studyIds =
                      virtualStudy.getData().getStudies().stream()
                          .flatMap(s -> s.getSamples().stream().map(s1 -> s.getId()))
                          .collect(Collectors.toList());
                  List<String> sampleIds =
                      virtualStudy.getData().getStudies().stream()
                          .flatMap(s -> s.getSamples().stream())
                          .collect(Collectors.toList());
                  if (studyIds.size() != sampleIds.size()) {
                    throw new IllegalStateException(
                        "Virtual study "
                            + virtualStudy.getId()
                            + " has different number of study ids and sample ids");
                  }
                  return clinicalAttributeRepository
                      // we drop clinical attributes that have no values here
                      .getClinicalAttributeCountsBySampleIds(studyIds, sampleIds)
                      .stream()
                      .filter(c -> c.getCount() > 0)
                      .map(
                          c -> {
                            List<ClinicalAttribute> clinicalAttributes =
                                materialisedClinicalAttributesByAttrId.get(c.getAttrId()).stream()
                                    .filter(ca -> studyIds.contains(ca.getCancerStudyIdentifier()))
                                    .toList();
                            return mergeClinicalAttribute(clinicalAttributes, virtualCancerStudy);
                          });
                })
            .toList();

    Stream<ClinicalAttribute> resultStream =
        Stream.concat(materialisedClinicalAttributes.stream(), virtualClinicalAttributes.stream());

    if (sortBy != null) {
      resultStream = resultStream.sorted(composeComparator(sortBy, direction));
    }

    if (pageSize != null && pageNumber != null) {
      resultStream = resultStream.skip((long) pageSize * pageNumber).limit(pageSize);
    }

    List<ClinicalAttribute> clinicalAttributes = resultStream.toList();
    return clinicalAttributes;
  }

  private Comparator<ClinicalAttribute> composeComparator(String sortBy, String direction) {
    ClinicalAttributeSortBy ca = ClinicalAttributeSortBy.valueOf(sortBy);
    Comparator<ClinicalAttribute> result =
        switch (ca) {
          case studyId -> Comparator.comparing(ClinicalAttribute::getCancerStudyIdentifier);
          case clinicalAttributeId -> Comparator.comparing(ClinicalAttribute::getAttrId);
          case displayName -> Comparator.comparing(ClinicalAttribute::getDisplayName);
          case description -> Comparator.comparing(ClinicalAttribute::getDescription);
          case datatype -> Comparator.comparing(ClinicalAttribute::getDatatype);
          case patientAttribute -> Comparator.comparing(ClinicalAttribute::getPatientAttribute);
          case priority -> Comparator.comparing(ClinicalAttribute::getPriority);
        };
    if (direction == null) {
      return result;
    } else {
      Direction d = Direction.valueOf(direction.toUpperCase());
      return d == Direction.ASC ? result : result.reversed();
    }
  }

  // TODO can we solve this and not fail? This can easily happen
  private static void ensureAllClinicalAttributesAreCompatible(
      List<ClinicalAttribute> clinicalAttributes) {
    for (int i = 1; i < clinicalAttributes.size(); i++) {
      if (!clinicalAttributes
          .get(i)
          .getDatatype()
          .equals(clinicalAttributes.get(1).getDatatype())) {
        throw new IllegalStateException(
            "Incompatible clinical attributes (attrId="
                + clinicalAttributes.getFirst().getAttrId()
                + ") by datatype"
                + clinicalAttributes.get(i).getDatatype()
                + " and "
                + clinicalAttributes.getFirst().getDatatype());
      }
      if (!clinicalAttributes
          .get(i)
          .getPatientAttribute()
          .equals(clinicalAttributes.get(1).getPatientAttribute())) {
        throw new IllegalStateException(
            "Incompatible clinical attributes (attrId="
                + clinicalAttributes.getFirst().getAttrId()
                + ") by patient attribute"
                + clinicalAttributes.get(i).getPatientAttribute()
                + " and "
                + clinicalAttributes.getFirst().getPatientAttribute());
      }
    }
  }

  private static ClinicalAttribute mergeClinicalAttribute(
      List<ClinicalAttribute> clinicalAttributes, CancerStudy virtualCancerStudy) {
    ensureAllClinicalAttributesAreCompatible(clinicalAttributes);
    ClinicalAttribute virtualClinicalAttribute = new ClinicalAttribute();
    virtualClinicalAttribute.setPatientAttribute(
        clinicalAttributes.getFirst().getPatientAttribute());
    virtualClinicalAttribute.setDatatype(clinicalAttributes.getFirst().getDatatype());
    virtualClinicalAttribute.setDisplayName(clinicalAttributes.getFirst().getDisplayName());
    virtualClinicalAttribute.setDescription(clinicalAttributes.getFirst().getDescription());
    virtualClinicalAttribute.setAttrId(clinicalAttributes.getFirst().getAttrId());
    virtualClinicalAttribute.setCancerStudyIdentifier(
        virtualCancerStudy.getCancerStudyIdentifier());
    virtualClinicalAttribute.setPriority(clinicalAttributes.getFirst().getPriority());
    return virtualClinicalAttribute;
  }

  @Override
  public BaseMeta getMetaClinicalAttributes() {
    BaseMeta meta = new BaseMeta();
    meta.setTotalCount(
        getAllClinicalAttributes(Projection.ID.name(), null, null, null, null).size());
    return meta;
  }

  @Override
  public ClinicalAttribute getClinicalAttribute(String studyId, String clinicalAttributeId) {
    // TODO Optimize!
    return getAllClinicalAttributes(Projection.DETAILED.name(), null, null, null, null).stream()
        .filter(
            clinicalAttribute ->
                clinicalAttribute.getAttrId().equals(clinicalAttributeId)
                    && clinicalAttribute.getCancerStudyIdentifier().equals(studyId))
        .findFirst()
        .orElse(null);
  }

  @Override
  public List<ClinicalAttribute> getAllClinicalAttributesInStudy(
      String studyId,
      String projection,
      Integer pageSize,
      Integer pageNumber,
      String sortBy,
      String direction) {
    // TODO Optimize!
    return getAllClinicalAttributes(projection, pageSize, pageNumber, sortBy, direction).stream()
        .filter(clinicalAttribute -> clinicalAttribute.getCancerStudyIdentifier().equals(studyId))
        .toList();
  }

  @Override
  public BaseMeta getMetaClinicalAttributesInStudy(String studyId) {
    BaseMeta meta = new BaseMeta();
    meta.setTotalCount(
        getAllClinicalAttributesInStudy(studyId, Projection.ID.name(), null, null, null, null)
            .size());
    return meta;
  }

  @Override
  public List<ClinicalAttribute> fetchClinicalAttributes(List<String> studyIds, String projection) {
    // TODO Optimize!
    return getAllClinicalAttributes(projection, null, null, null, null).stream()
        .filter(
            clinicalAttribute -> studyIds.contains(clinicalAttribute.getCancerStudyIdentifier()))
        .toList();
  }

  @Override
  public BaseMeta fetchMetaClinicalAttributes(List<String> studyIds) {
    BaseMeta meta = new BaseMeta();
    meta.setTotalCount(fetchClinicalAttributes(studyIds, Projection.ID.name()).size());
    return meta;
  }

  @Override
  public List<ClinicalAttributeCount> getClinicalAttributeCountsBySampleIds(
      List<String> studyIds, List<String> sampleIds) {
    // TODO implement this
    /*List<String> vStudyIds = virtualStudyService.getPublishedVirtualStudies().stream().filter(vs -> studyIds.contains(vs.getId())).flatMap(virtualStudy ->
        virtualStudy.getData().getStudies().stream().filter(vss -> new HashSet<>(vss.getSamples()).retainAll(sampleIds)).map(VirtualStudySamples::getId)
    ).toList();
    List<String> allStudyIds = Stream.concat(studyIds.stream(), vStudyIds.stream()).collect(Collectors.toSet()).stream().toList();
    return clinicalAttributeRepository.getClinicalAttributeCountsBySampleIds(allStudyIds, sampleIds);*/
    return clinicalAttributeRepository.getClinicalAttributeCountsBySampleIds(studyIds, sampleIds);
  }

  @Override
  public List<ClinicalAttributeCount> getClinicalAttributeCountsBySampleListId(
      String sampleListId) {
    return clinicalAttributeRepository.getClinicalAttributeCountsBySampleListId(sampleListId);
  }

  @Override
  public List<ClinicalAttribute> getClinicalAttributesByStudyIdsAndAttributeIds(
      List<String> studyIds, List<String> attributeIds) {
    // TODO Optimize!
    return getAllClinicalAttributes(Projection.DETAILED.name(), null, null, null, null).stream()
        .filter(
            clinicalAttribute ->
                attributeIds.contains(clinicalAttribute.getAttrId())
                    && studyIds.contains(clinicalAttribute.getCancerStudyIdentifier()))
        .toList();
  }
}
