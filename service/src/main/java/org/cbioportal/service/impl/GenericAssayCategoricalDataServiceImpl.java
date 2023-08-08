package org.cbioportal.service.impl;

import org.apache.commons.lang3.BooleanUtils;
import org.cbioportal.model.*;
import org.cbioportal.model.meta.GenericAssayMeta;
import org.cbioportal.persistence.GenericAssayRepository;
import org.cbioportal.persistence.MolecularDataRepository;
import org.cbioportal.persistence.SampleListRepository;
import org.cbioportal.service.*;
import org.cbioportal.service.exception.MolecularProfileNotFoundException;
import org.cbioportal.service.util.ExpressionEnrichmentUtil;
import org.cbioportal.service.util.FisherExactTestCalculator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class GenericAssayCategoricalDataServiceImpl implements GenericAssayCategoricalDataService {

    @Autowired
    private MolecularDataRepository molecularDataRepository;

    @Autowired
    private SampleService sampleService;

    @Autowired
    private MolecularProfileService molecularProfileService;

    @Autowired
    private FisherExactTestCalculator fisherExactTestCalculator = new FisherExactTestCalculator();

    @Autowired
    private ExpressionEnrichmentUtil expressionEnrichmentUtil = new ExpressionEnrichmentUtil();
    @Autowired
    private GenericAssayService genericAssayService;

    @Override
    @Transactional(readOnly = true)
    public List<GenericAssayCategoricalEnrichment> getGenericAssayCategoricalEnrichments(String molecularProfileId, 
          Map<String, List<MolecularProfileCaseIdentifier>> molecularProfileCaseSets, EnrichmentType enrichmentType) 
        throws MolecularProfileNotFoundException {

            MolecularProfile molecularProfile = molecularProfileService.getMolecularProfile(molecularProfileId);
            validateMolecularProfile(molecularProfile, Arrays.asList(MolecularProfile.MolecularAlterationType.GENERIC_ASSAY));

            Iterable<GenericAssayMolecularAlteration> maItr = molecularDataRepository
                .getGenericAssayMolecularAlterationsIterable(molecularProfile.getStableId(), null, "SUMMARY");

            Map<String, List<MolecularProfileCaseIdentifier>> filteredMolecularProfileCaseSets;
            if (BooleanUtils.isTrue(molecularProfile.getPatientLevel())) {
                List<String> sampleIds = molecularProfileCaseSets.values().stream().flatMap(Collection::stream).map(MolecularProfileCaseIdentifier::getCaseId).collect(Collectors.toList());
                List<String> studyIds = Collections.nCopies(sampleIds.size(), molecularProfile.getCancerStudyIdentifier());
                List<Sample> samples = sampleService.fetchSamples(studyIds, sampleIds, "ID");

                Map<String, Integer> sampleIdToPatientIdMap = samples.stream()
                    .filter(sample -> sample != null && sample.getStableId() != null && sample.getPatientId() != null)
                    .collect(Collectors.toMap(Sample::getStableId, Sample::getPatientId));

                filteredMolecularProfileCaseSets = new HashMap<>();
                for (Map.Entry<String, List<MolecularProfileCaseIdentifier>> pair : molecularProfileCaseSets.entrySet()) {
                    Set<Integer> patientSet = new HashSet<Integer>();
                    List<MolecularProfileCaseIdentifier> identifierListUniqueByPatientId = new ArrayList<>();
                    for (MolecularProfileCaseIdentifier caseIdentifier : pair.getValue()) {
                        if (!patientSet.contains(sampleIdToPatientIdMap.get(caseIdentifier.getCaseId()))) {
                            identifierListUniqueByPatientId.add(caseIdentifier);
                            patientSet.add(sampleIdToPatientIdMap.get(caseIdentifier.getCaseId()));
                        }
                    }
                    filteredMolecularProfileCaseSets.put(pair.getKey(), identifierListUniqueByPatientId);
                }
            } else {
                filteredMolecularProfileCaseSets = molecularProfileCaseSets;
            }
            
            List<GenericAssayCategoricalEnrichment> genericAssayCategoricalEnrichments = expressionEnrichmentUtil.getGenericAssayCategoricalEnrichments(molecularProfile,
                filteredMolecularProfileCaseSets, enrichmentType, maItr);

            // Sort the list based on pValue.
            Collections.sort(genericAssayCategoricalEnrichments, GenericAssayEnrichment::compare);
    
            // Extract pValues and calculate qValues.
            BigDecimal[] pValues = genericAssayCategoricalEnrichments.stream().map(a -> a.getpValue()).toArray(BigDecimal[]::new);
            BigDecimal[] qValues = fisherExactTestCalculator.calcqValue(pValues);
    
            // Assign qValues back to the objects.
            for (int i = 0; i < genericAssayCategoricalEnrichments.size(); i++) {
                genericAssayCategoricalEnrichments.get(i).setqValue(qValues[i]);
            }
            
            List<String> getGenericAssayStableIds = genericAssayCategoricalEnrichments.stream()
                .map(GenericAssayEnrichment::getStableId).collect(Collectors.toList());
            
            Map<String, GenericAssayMeta> genericAssayMetaByStableId = genericAssayService
                .getGenericAssayMetaByStableIdsAndMolecularIds(getGenericAssayStableIds,
                    getGenericAssayStableIds.stream().map(stableId -> molecularProfileId)
                        .collect(Collectors.toList()),
                    "SUMMARY")
                .stream().collect(Collectors.toMap(GenericAssayMeta::getStableId, Function.identity()));
            
            return genericAssayCategoricalEnrichments.stream().map(enrichmentDatum -> {
                enrichmentDatum.setGenericEntityMetaProperties(
                    genericAssayMetaByStableId.get(enrichmentDatum.getStableId()).getGenericEntityMetaProperties());
                return enrichmentDatum;
            }).collect(Collectors.toList());
    }

    private void validateMolecularProfile(MolecularProfile molecularProfile,
                                          List<MolecularProfile.MolecularAlterationType> validMolecularAlterationTypes) throws MolecularProfileNotFoundException {
        if (!validMolecularAlterationTypes.contains(molecularProfile.getMolecularAlterationType())) {
            throw new MolecularProfileNotFoundException(molecularProfile.getStableId());
        }
        if(!molecularProfile.getGenericAssayType().equals("CATEGORICAL")) {
            throw new MolecularProfileNotFoundException(molecularProfile.getStableId());
        }
    }

}
