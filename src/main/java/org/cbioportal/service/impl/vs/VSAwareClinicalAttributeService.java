package org.cbioportal.service.impl.vs;

import org.cbioportal.model.CancerStudy;
import org.cbioportal.model.ClinicalAttribute;
import org.cbioportal.model.ClinicalAttributeCount;
import org.cbioportal.model.SampleList;
import org.cbioportal.model.meta.BaseMeta;
import org.cbioportal.persistence.PersistenceConstants;
import org.cbioportal.service.ClinicalAttributeService;
import org.cbioportal.service.SampleListService;
import org.cbioportal.service.exception.ClinicalAttributeNotFoundException;
import org.cbioportal.service.exception.SampleListNotFoundException;
import org.cbioportal.service.exception.StudyNotFoundException;
import org.cbioportal.web.parameter.VirtualStudySamples;

import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class VSAwareClinicalAttributeService implements ClinicalAttributeService {

    private final ClinicalAttributeService clinicalAttributeService;
    private final PublishedVirtualStudyService publishedVirtualStudyService;
    private final SampleListService sampleListService;

    public VSAwareClinicalAttributeService(ClinicalAttributeService clinicalAttributeService, PublishedVirtualStudyService publishedVirtualStudyService, SampleListService sampleListService) {
        this.clinicalAttributeService = clinicalAttributeService;
        this.publishedVirtualStudyService = publishedVirtualStudyService;
        this.sampleListService = sampleListService;
    }

    @Override
    public List<ClinicalAttribute> getAllClinicalAttributes(String projection, Integer pageSize, Integer pageNumber, String sortBy, String direction) {
        List<ClinicalAttribute> materialisedClinicalAttributes = clinicalAttributeService.getAllClinicalAttributes(projection, null, null, null, null);
        Map<String,List<ClinicalAttribute>> materialisedClinicalAttributesByAttrId = materialisedClinicalAttributes.stream().collect(Collectors.groupingBy(ClinicalAttribute::getAttrId));
        List<ClinicalAttribute> virtualClinicalAttributes = publishedVirtualStudyService.getAllPublishedVirtualStudies().stream().flatMap(virtualStudy -> {
                CancerStudy virtualCancerStudy = publishedVirtualStudyService.toCancerStudy(virtualStudy);
                List<String> studyIds = virtualStudy.getData().getStudies().stream().map(VirtualStudySamples::getId).collect(Collectors.toList());
                List<String> sampleIds = virtualStudy.getData().getStudies().stream().flatMap(s -> s.getSamples().stream()).collect(Collectors.toList());
                return clinicalAttributeService.getClinicalAttributeCountsBySampleIds(studyIds, sampleIds).stream().filter(c -> c.getCount() > 0).map(c -> {
                    List<ClinicalAttribute> clinicalAttributes = materialisedClinicalAttributesByAttrId.get(c.getAttrId()).stream().filter(ca -> studyIds.contains(ca.getCancerStudyIdentifier())).toList();
                            return getClinicalAttribute(clinicalAttributes, virtualCancerStudy);
                });
        }).toList();

        Stream<ClinicalAttribute> resultStream = Stream.concat(
            materialisedClinicalAttributes.stream(),
            virtualClinicalAttributes.stream()
        );

        if (sortBy != null) {
            resultStream = resultStream.sorted(buildComparator(sortBy, direction));
        }

        if (pageSize != null && pageNumber != null) {
            resultStream = resultStream.skip((long) pageSize * pageNumber).limit(pageSize);
        }

        return resultStream.toList();

    }

    //TODO can we solve this and not fail? This can easily happen
    private static void ensureAllClinicalAttributesAreCompatible(List<ClinicalAttribute> clinicalAttributes) {
        for (int i = 1; i < clinicalAttributes.size(); i++) {
           if (!clinicalAttributes.get(i).getDatatype().equals(clinicalAttributes.get(1).getDatatype())) {
              throw new IllegalStateException("Incompatible clinical attributes (attrId=" + clinicalAttributes.getFirst().getAttrId() + ") by datatype" + clinicalAttributes.get(i).getDatatype() + " and " + clinicalAttributes.getFirst().getDatatype()); 
           }
           if (!clinicalAttributes.get(i).getPatientAttribute().equals(clinicalAttributes.get(1).getPatientAttribute())) {
               throw new IllegalStateException("Incompatible clinical attributes (attrId=" + clinicalAttributes.getFirst().getAttrId() + ") by patient attribute" + clinicalAttributes.get(i).getPatientAttribute() + " and " + clinicalAttributes.getFirst().getPatientAttribute());
           }
        }
    }

    private static ClinicalAttribute getClinicalAttribute(List<ClinicalAttribute> clinicalAttributes, CancerStudy virtualCancerStudy) {
        ensureAllClinicalAttributesAreCompatible(clinicalAttributes);
        ClinicalAttribute virtualClinicalAttribute = new ClinicalAttribute();
        virtualClinicalAttribute.setPatientAttribute(clinicalAttributes.getFirst().getPatientAttribute());
        virtualClinicalAttribute.setDatatype(clinicalAttributes.getFirst().getDatatype());
        virtualClinicalAttribute.setDisplayName(clinicalAttributes.getFirst().getDisplayName());
        virtualClinicalAttribute.setDescription(clinicalAttributes.getFirst().getDescription());
        virtualClinicalAttribute.setAttrId(clinicalAttributes.getFirst().getAttrId());
        virtualClinicalAttribute.setCancerStudyIdentifier(virtualCancerStudy.getCancerStudyIdentifier());
        virtualClinicalAttribute.setPriority(clinicalAttributes.getFirst().getPriority());
        return virtualClinicalAttribute;
    }

    private static Comparator<ClinicalAttribute> buildComparator(String sortBy, String direction) {
        Function<ClinicalAttribute, Comparable> getValue;
        //TODO add more fields to sort by
        if (sortBy.equalsIgnoreCase("name")) {
            getValue = ClinicalAttribute::getDisplayName;
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
    public BaseMeta getMetaClinicalAttributes() {
        return clinicalAttributeService.getMetaClinicalAttributes();
    }

    @Override
    public ClinicalAttribute getClinicalAttribute(String studyId, String clinicalAttributeId) throws ClinicalAttributeNotFoundException, StudyNotFoundException {
        return clinicalAttributeService.getClinicalAttribute(studyId, clinicalAttributeId);
    }

    @Override
    public List<ClinicalAttribute> getAllClinicalAttributesInStudy(String studyId, String projection, Integer pageSize, Integer pageNumber, String sortBy, String direction) throws StudyNotFoundException {
        return getAllClinicalAttributes(projection, pageSize, pageNumber, sortBy, direction).stream()
                .filter(c -> c.getCancerStudyIdentifier().equals(studyId)).toList();
    }

    @Override
    public BaseMeta getMetaClinicalAttributesInStudy(String studyId) {
        return fetchMetaClinicalAttributes(List.of(studyId));
    }

    @Override
    public List<ClinicalAttribute> fetchClinicalAttributes(List<String> studyIds, String projection) {
        return getAllClinicalAttributes(projection, null, null, null, null).stream()
                .filter(c -> studyIds.contains(c.getCancerStudyIdentifier())).toList();
    }

    @Override
    public BaseMeta fetchMetaClinicalAttributes(List<String> studyIds) {
        BaseMeta baseMeta = new BaseMeta();
        baseMeta.setTotalCount(fetchClinicalAttributes(studyIds, PersistenceConstants.ID_PROJECTION).size());
        return baseMeta;
    }

    @Override
    public List<ClinicalAttributeCount> getClinicalAttributeCountsBySampleIds(List<String> studyIds, List<String> sampleIds) {
        List<String> vStudyIds = publishedVirtualStudyService.getAllPublishedVirtualStudies().stream().filter(vs -> studyIds.contains(vs.getId())).flatMap(virtualStudy ->
            virtualStudy.getData().getStudies().stream().filter(vss -> new HashSet<>(vss.getSamples()).retainAll(sampleIds)).map(VirtualStudySamples::getId)
        ).toList();
        List<String> allStudyIds = Stream.concat(sampleIds.stream(), vStudyIds.stream()).collect(Collectors.toSet()).stream().toList();
        return clinicalAttributeService.getClinicalAttributeCountsBySampleIds(allStudyIds, sampleIds);
    }

    @Override
    public List<ClinicalAttributeCount> getClinicalAttributeCountsBySampleListId(String sampleListId) {
        try {
            SampleList sampleList = sampleListService.getSampleList(sampleListId);
            return this.getClinicalAttributeCountsBySampleIds(List.of(sampleList.getCancerStudyIdentifier()), sampleList.getSampleIds());
        } catch (SampleListNotFoundException e) {
            return List.of();
        }
    }

    @Override
    public List<ClinicalAttribute> getClinicalAttributesByStudyIdsAndAttributeIds(List<String> studyIds, List<String> attributeIds) {
        return getAllClinicalAttributes(PersistenceConstants.DETAILED_PROJECTION, null, null, null, null).stream()
                .filter(c -> studyIds.contains(c.getCancerStudyIdentifier()) && attributeIds.contains(c.getAttrId())).toList();
    }
}
