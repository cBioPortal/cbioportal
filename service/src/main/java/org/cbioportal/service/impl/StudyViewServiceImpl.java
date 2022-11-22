package org.cbioportal.service.impl;

import org.apache.commons.collections4.map.MultiKeyMap;
import org.apache.commons.lang3.StringUtils;
import org.cbioportal.model.*;
import org.cbioportal.model.util.Select;
import org.cbioportal.persistence.AlterationRepository;
import org.cbioportal.service.*;
import org.cbioportal.service.exception.MolecularProfileNotFoundException;
import org.cbioportal.service.exception.StudyNotFoundException;
import org.cbioportal.service.util.MolecularProfileUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class StudyViewServiceImpl implements StudyViewService {
    private static final List<CNA> CNA_TYPES_AMP_AND_HOMDEL = Collections.unmodifiableList(Arrays.asList(CNA.AMP, CNA.HOMDEL));
    @Autowired
    private MolecularProfileService molecularProfileService;
    @Autowired
    private GenePanelService genePanelService;
    @Autowired
    private MolecularProfileUtil molecularProfileUtil;
    @Autowired
    private AlterationCountService alterationCountService;
    @Autowired
    private SignificantlyMutatedGeneService significantlyMutatedGeneService;
    @Autowired
    private SignificantCopyNumberRegionService significantCopyNumberRegionService;
    @Autowired
    private GenericAssayService genericAssayService;

    @Autowired
    private AlterationRepository alterationRepository;

    @Override
    public List<GenomicDataCount> getGenomicDataCounts(List<String> studyIds, List<String> sampleIds) {
        List<MolecularProfileCaseIdentifier> molecularProfileSampleIdentifiers =
            molecularProfileService.getMolecularProfileCaseIdentifiers(studyIds, sampleIds);

        List<MolecularProfile> molecularProfiles = molecularProfileService
            .getMolecularProfilesInStudies(new ArrayList<>(new HashSet<>(studyIds)),"SUMMARY");
        Map<String, MolecularProfile> molecularProfileMap = molecularProfiles
            .stream()
            .collect(Collectors.toMap(MolecularProfile::getStableId, Function.identity()));

        Map<String, Integer> molecularProfileCaseCountSet = genePanelService
            .fetchGenePanelDataInMultipleMolecularProfiles(molecularProfileSampleIdentifiers)
            .stream()
            .filter(GenePanelData::getProfiled)
            .collect(Collectors.groupingBy(GenePanelData::getMolecularProfileId))
            .entrySet()
            .stream()
            .collect(Collectors.toMap(entry -> entry.getKey(), entry -> (int)entry.getValue().stream().map(d -> molecularProfileMap.get(entry.getKey()).getPatientLevel() ? d.getPatientId() : d.getSampleId()).distinct().count()));
        
        return molecularProfileUtil
            .categorizeMolecularProfilesByStableIdSuffixes(molecularProfiles)
            .entrySet()
            .stream()
            .map(entry -> {
                GenomicDataCount dataCount = new GenomicDataCount();
                dataCount.setValue(entry.getKey());

                Integer count = entry
                    .getValue()
                    .stream()
                    .mapToInt(molecularProfile -> molecularProfileCaseCountSet.getOrDefault(molecularProfile.getStableId(), 0))
                    .sum();

                dataCount.setCount(count);
                dataCount.setLabel(entry.getValue().get(0).getName());
                return dataCount;
            })
            .filter(dataCount -> dataCount.getCount() > 0)
            .collect(Collectors.toList());
    }

    @Override
    public List<AlterationCountByGene> getMutationAlterationCountByGenes(List<String> studyIds,
                                                                         List<String> sampleIds,
                                                                         AlterationFilter alterationFilter)
        throws StudyNotFoundException {
        List<MolecularProfileCaseIdentifier> caseIdentifiers =
            molecularProfileService.getFirstMutationProfileCaseIdentifiers(studyIds, sampleIds);
        List<AlterationCountByGene> alterationCountByGenes = alterationCountService.getSampleMutationGeneCounts(
            caseIdentifiers,
            Select.all(),
            true,
            false,
            alterationFilter).getFirst();
        annotateDataWithQValue(studyIds, alterationCountByGenes);
        return alterationCountByGenes;
    }

    @Override
    public List<AlterationCountByGene> getStructuralVariantAlterationCountByGenes(List<String> studyIds,
                                                                                  List<String> sampleIds,
                                                                                  AlterationFilter alterationFilter)
        throws StudyNotFoundException {
        List<MolecularProfileCaseIdentifier> caseIdentifiers =
            molecularProfileService.getFirstStructuralVariantProfileCaseIdentifiers(studyIds, sampleIds);
        List<AlterationCountByGene> alterationCountByGenes = alterationCountService.getSampleStructuralVariantGeneCounts(
            caseIdentifiers,
            Select.all(),
            true,
            false,
            alterationFilter).getFirst();
        annotateDataWithQValue(studyIds, alterationCountByGenes);
        return alterationCountByGenes;
    }

    @Override
    public List<AlterationCountByStructuralVariant> getStructuralVariantAlterationCounts(List<String> studyIds,
                                                                                         List<String> sampleIds,
                                                                                         AlterationFilter annotationFilters) {
        List<MolecularProfileCaseIdentifier> caseIdentifiers =
            molecularProfileService.getFirstStructuralVariantProfileCaseIdentifiers(studyIds, sampleIds);
        return alterationCountService.getSampleStructuralVariantCounts(caseIdentifiers,
            annotationFilters).getFirst();
    }

    private void annotateDataWithQValue(List<String> studyIds, List<AlterationCountByGene> alterationCountByGenes)
        throws StudyNotFoundException {
        Set<String> distinctStudyIds = new HashSet<>(studyIds);
        if (!alterationCountByGenes.isEmpty() && distinctStudyIds.size() == 1) {
            Map<Integer, MutSig> mutSigMap =
                significantlyMutatedGeneService.getSignificantlyMutatedGenes(
                    studyIds.get(0),
                    "SUMMARY",
                    null,
                    null,
                    null,
                    null)
                    .stream()
                    .collect(Collectors.toMap(MutSig::getEntrezGeneId, Function.identity()));
            alterationCountByGenes.forEach(r -> {
                if (mutSigMap.containsKey(r.getEntrezGeneId())) {
                    r.setqValue(mutSigMap.get(r.getEntrezGeneId()).getqValue());
                }
            });
        }
    }

    @Override
    public List<CopyNumberCountByGene> getCNAAlterationCountByGenes(List<String> studyIds,
                                                                    List<String> sampleIds,
                                                                    AlterationFilter alterationFilter)
        throws StudyNotFoundException {
        List<MolecularProfileCaseIdentifier> caseIdentifiers =
            molecularProfileService.getFirstDiscreteCNAProfileCaseIdentifiers(studyIds, sampleIds);
        Select<CNA> cnaTypes = Select.byValues(CNA_TYPES_AMP_AND_HOMDEL);
        List<CopyNumberCountByGene> copyNumberCountByGenes = alterationCountService.getSampleCnaGeneCounts(
            caseIdentifiers,
            Select.all(),
            true,
            false,
            alterationFilter).getFirst();
        Set<String> distinctStudyIds = new HashSet<>(studyIds);
        if (distinctStudyIds.size() == 1 && !copyNumberCountByGenes.isEmpty()) {
            List<Gistic> gisticList = significantCopyNumberRegionService.getSignificantCopyNumberRegions(
                studyIds.get(0),
                "SUMMARY",
                null,
                null,
                null,
                null);
            MultiKeyMap gisticMap = new MultiKeyMap();
            gisticList.forEach(g -> g.getGenes().forEach(gene -> {
                Gistic gistic = (Gistic) gisticMap.get(gene.getEntrezGeneId(), g.getAmp());
                if (gistic == null || g.getqValue().compareTo(gistic.getqValue()) < 0) {
                    gisticMap.put(gene.getEntrezGeneId(), g.getAmp(), g);
                }
            }));
            copyNumberCountByGenes.forEach(r -> {
                if (gisticMap.containsKey(r.getEntrezGeneId(), r.getAlteration().equals(2))) {
                    r.setqValue(((Gistic) gisticMap.get(r.getEntrezGeneId(), r.getAlteration().equals(2))).getqValue());
                }
            });
        }
        return copyNumberCountByGenes;
    }

    @Override
    public List<GenericAssayDataCountItem> fetchGenericAssayDataCounts(List<String> sampleIds, List<String> studyIds,
                                                               List<String> stableIds, List<String> profileTypes) {
        if (stableIds.isEmpty()) {
            return new ArrayList<>();
        }

        // Get data from fetchGenericAssayData service
        List<MolecularProfile> molecularProfiles = molecularProfileService.getMolecularProfilesInStudies(studyIds,
            "SUMMARY");

        Map<String, List<MolecularProfile>> molecularProfileMap = molecularProfileUtil
            .categorizeMolecularProfilesByStableIdSuffixes(molecularProfiles);

        List<GenericAssayData> data = profileTypes.stream().flatMap(profileType -> {
            // We need to create a map for mapping from studyId to profileId
            Map<String, String> studyIdToMolecularProfileIdMap = molecularProfileMap
                .getOrDefault(profileType, new ArrayList<>())
                .stream().collect(Collectors.toMap(MolecularProfile::getCancerStudyIdentifier,
                    MolecularProfile::getStableId));

            List<String> mappedSampleIds = new ArrayList<>();
            List<String> mappedProfileIds = new ArrayList<>();

            for (int i = 0; i < sampleIds.size(); i++) {
                String studyId = studyIds.get(i);
                if (studyIdToMolecularProfileIdMap.containsKey(studyId)) {
                    mappedSampleIds.add(sampleIds.get(i));
                    mappedProfileIds.add(studyIdToMolecularProfileIdMap.get(studyId));
                }
            }

            try {
                return genericAssayService.fetchGenericAssayData(mappedProfileIds, mappedSampleIds, stableIds, "SUMMARY").stream();
            } catch (MolecularProfileNotFoundException e) {
                return new ArrayList<GenericAssayData>().stream();
            }
        }).collect(Collectors.toList());
        
        return data
            .stream()
            .filter(g -> StringUtils.isNotEmpty(g.getValue()) && !g.getValue().equals("NA"))
            .collect(Collectors.groupingBy(GenericAssayData::getGenericAssayStableId))
            .entrySet()
            .stream()
            .map(entry -> {
                int totalCount = entry.getValue().size();
                int naCount = sampleIds.size() - totalCount;
                List<GenericAssayDataCount> counts = entry.getValue()
                    .stream()
                    .collect(Collectors.groupingBy(GenericAssayData::getValue))
                    .entrySet()
                    .stream()
                    .map(datum -> {
                        GenericAssayDataCount dataCount = new GenericAssayDataCount();
                        dataCount.setValue(datum.getKey());
                        dataCount.setCount(datum.getValue().size());
                        return dataCount; 
                    })
                    .collect(Collectors.toList());
                
                if (naCount > 0) {
                    GenericAssayDataCount dataCount = new GenericAssayDataCount();
                    dataCount.setValue("NA");
                    dataCount.setCount(naCount);
                    counts.add(dataCount);
                }

                GenericAssayDataCountItem genericAssayDataCountItem = new GenericAssayDataCountItem();
                genericAssayDataCountItem.setStableId(entry.getKey());
                genericAssayDataCountItem.setCounts(counts);
                return genericAssayDataCountItem;
            })
            .collect(Collectors.toList());
    }

}
