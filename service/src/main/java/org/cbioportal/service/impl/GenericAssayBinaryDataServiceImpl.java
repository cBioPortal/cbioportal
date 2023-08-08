package org.cbioportal.service.impl;

import org.apache.commons.lang3.BooleanUtils;
import org.cbioportal.model.*;
import org.cbioportal.model.meta.GenericAssayMeta;
import org.cbioportal.persistence.MolecularDataRepository;
import org.cbioportal.service.GenericAssayBinaryDataService;
import org.cbioportal.service.GenericAssayService;
import org.cbioportal.service.MolecularProfileService;
import org.cbioportal.service.SampleService;
import org.cbioportal.service.exception.MolecularProfileNotFoundException;
import org.cbioportal.service.util.ExpressionEnrichmentUtil;
import org.cbioportal.service.util.FisherExactTestCalculator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.Map;

import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class GenericAssayBinaryDataServiceImpl implements GenericAssayBinaryDataService {

    @Autowired
    private MolecularDataRepository molecularDataRepository;

    @Autowired
    private SampleService sampleService;

    @Autowired
    private MolecularProfileService molecularProfileService;

    @Autowired
    private FisherExactTestCalculator fisherExactTestCalculator = new FisherExactTestCalculator();

    @Autowired
    private ExpressionEnrichmentUtil expressionEnrichmentUtil;
    @Autowired
    private GenericAssayService genericAssayService;

    @Override
    @Transactional(readOnly = true)
    public List<GenericAssayBinaryEnrichment> getGenericAssayBinaryEnrichments(
        String molecularProfileId,
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
            Map<String, Integer> sampleIdToPatientIdMap = samples.stream().collect(Collectors.toMap(Sample::getStableId, Sample::getPatientId));

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

        List<GenericAssayBinaryEnrichment> genericAssayBinaryEnrichments = expressionEnrichmentUtil.getGenericAssayBinaryEnrichments(molecularProfile,
            filteredMolecularProfileCaseSets, enrichmentType, maItr);

        Collections.sort(genericAssayBinaryEnrichments, GenericAssayEnrichment::compare);
        // Extract pValues and calculate qValues.
        BigDecimal[] pValues = genericAssayBinaryEnrichments.stream().map(GenericAssayBinaryEnrichment ::getpValue).toArray(BigDecimal[]::new);
        BigDecimal[] qValues = fisherExactTestCalculator.calcqValue(pValues);

        // Assign qValues back to the objects.
        for (int i = 0; i < genericAssayBinaryEnrichments.size(); i++) {
            genericAssayBinaryEnrichments.get(i).setqValue(qValues[i]);
        }

        List<String> getGenericAssayStableIds = genericAssayBinaryEnrichments.stream()
            .map(GenericAssayEnrichment::getStableId).collect(Collectors.toList());

        Map<String, GenericAssayMeta> genericAssayMetaByStableId = genericAssayService
            .getGenericAssayMetaByStableIdsAndMolecularIds(getGenericAssayStableIds,
                getGenericAssayStableIds.stream().map(stableId -> molecularProfileId)
                    .collect(Collectors.toList()),
                "SUMMARY")
            .stream().collect(Collectors.toMap(GenericAssayMeta::getStableId, Function.identity()));

        return genericAssayBinaryEnrichments.stream().map(enrichmentDatum -> {
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
        System.out.println(molecularProfile.getGenericAssayType());
        if(!molecularProfile.getGenericAssayType().equals("BINARY")) {
            throw new MolecularProfileNotFoundException(molecularProfile.getStableId());
        }
    }
    
}
