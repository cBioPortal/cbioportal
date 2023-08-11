package org.cbioportal.service.impl;

import org.apache.commons.lang3.BooleanUtils;
import org.cbioportal.model.EnrichmentType;
import org.cbioportal.model.GenericAssayBinaryEnrichment;
import org.cbioportal.model.GenericAssayCategoricalEnrichment;
import org.cbioportal.model.GenericAssayMolecularAlteration;
import org.cbioportal.model.MolecularProfileCaseIdentifier;
import org.cbioportal.model.meta.GenericAssayMeta;
import org.cbioportal.model.MolecularProfile;
import org.cbioportal.model.Sample;
import org.cbioportal.model.GenericAssayEnrichment;
import org.cbioportal.persistence.MolecularDataRepository;
import org.cbioportal.service.GenericAssayEnrichmentService;
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
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;


@Service
public class GenericAssayEnrichmentServiceImpl implements GenericAssayEnrichmentService {

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

        // Validate and fetch molecular profile
        MolecularProfile molecularProfile = getAndValidateMolecularProfile(molecularProfileId, true);

        // Get the molecular alterations for the provided profile
        Iterable<GenericAssayMolecularAlteration> maItr = molecularDataRepository
            .getGenericAssayMolecularAlterationsIterable(molecularProfile.getStableId(), null, "SUMMARY");

        // Filter the case sets based on molecular profile
        Map<String, List<MolecularProfileCaseIdentifier>> filteredMolecularProfileCaseSets = filterMolecularProfileCaseSets(molecularProfile, molecularProfileCaseSets);

        // Obtain binary enrichments from the utility
        List<GenericAssayBinaryEnrichment> genericAssayBinaryEnrichments = expressionEnrichmentUtil.getGenericAssayBinaryEnrichments(molecularProfile,
            filteredMolecularProfileCaseSets, enrichmentType, maItr);

        // Calculate q-values for enrichments
        calcQValues(genericAssayBinaryEnrichments);

        // Extract stable IDs from binary enrichments
        List<String> getGenericAssayStableIds = genericAssayBinaryEnrichments.stream()
            .map(GenericAssayEnrichment::getStableId).collect(Collectors.toList());

        // Fetch metadata of generic assays by their stable IDs
        Map<String, GenericAssayMeta> genericAssayMetaByStableId = getGenericAssayMetaByStableId(getGenericAssayStableIds, molecularProfileId);

        // Assign meta properties to each enrichment
        return genericAssayBinaryEnrichments.stream().map(enrichmentDatum -> {
            enrichmentDatum.setGenericEntityMetaProperties(
                genericAssayMetaByStableId.get(enrichmentDatum.getStableId()).getGenericEntityMetaProperties());
            return enrichmentDatum;
        }).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<GenericAssayCategoricalEnrichment> getGenericAssayCategoricalEnrichments(String molecularProfileId,
                                                                                         Map<String, List<MolecularProfileCaseIdentifier>> molecularProfileCaseSets, EnrichmentType enrichmentType)
        throws MolecularProfileNotFoundException {

        MolecularProfile molecularProfile = getAndValidateMolecularProfile(molecularProfileId, false);

        Iterable<GenericAssayMolecularAlteration> maItr = molecularDataRepository
            .getGenericAssayMolecularAlterationsIterable(molecularProfile.getStableId(), null, "SUMMARY");

        Map<String, List<MolecularProfileCaseIdentifier>> filteredMolecularProfileCaseSets;
        filteredMolecularProfileCaseSets = filterMolecularProfileCaseSets(molecularProfile, molecularProfileCaseSets);

        List<GenericAssayCategoricalEnrichment> genericAssayCategoricalEnrichments = expressionEnrichmentUtil.getGenericAssayCategoricalEnrichments(molecularProfile,
            filteredMolecularProfileCaseSets, enrichmentType, maItr);

        calcQValues(genericAssayCategoricalEnrichments);

        List<String> getGenericAssayStableIds = genericAssayCategoricalEnrichments.stream()
            .map(GenericAssayEnrichment::getStableId).collect(Collectors.toList());
        Map<String, GenericAssayMeta> genericAssayMetaByStableId = getGenericAssayMetaByStableId(getGenericAssayStableIds, molecularProfileId);

        return genericAssayCategoricalEnrichments.stream().map(enrichmentDatum -> {
            enrichmentDatum.setGenericEntityMetaProperties(
                genericAssayMetaByStableId.get(enrichmentDatum.getStableId()).getGenericEntityMetaProperties());
            return enrichmentDatum;
        }).collect(Collectors.toList());
    }

    private MolecularProfile getAndValidateMolecularProfile(String molecularProfileId, boolean isBinary) throws MolecularProfileNotFoundException {
        MolecularProfile molecularProfile = molecularProfileService.getMolecularProfile(molecularProfileId);
        validateMolecularProfile(molecularProfile, Arrays.asList(MolecularProfile.MolecularAlterationType.GENERIC_ASSAY), isBinary);
        return molecularProfile;
    }

    private void validateMolecularProfile(MolecularProfile molecularProfile,
                                          List<MolecularProfile.MolecularAlterationType> validMolecularAlterationTypes, 
                                          boolean isBinary) throws MolecularProfileNotFoundException {
        if (!validMolecularAlterationTypes.contains(molecularProfile.getMolecularAlterationType())) {
            // Check alteration type
            throw new MolecularProfileNotFoundException(molecularProfile.getStableId());
        }
        // Check datatype for binary or categorical
        if(isBinary) {
            if(!molecularProfile.getDatatype().equals("BINARY")) {
                throw new MolecularProfileNotFoundException(molecularProfile.getStableId());
            }            
        } else {
            if(!molecularProfile.getDatatype().equals("CATEGORICAL")) {
                throw new MolecularProfileNotFoundException(molecularProfile.getStableId());
            }            
        }
    }

    private Map<String, List<MolecularProfileCaseIdentifier>> filterMolecularProfileCaseSets(MolecularProfile molecularProfile, Map<String, List<MolecularProfileCaseIdentifier>> molecularProfileCaseSets) {
        if (BooleanUtils.isTrue(molecularProfile.getPatientLevel())) {
            // If patient level, filter duplicates by patient id
            // For now we only support sample level for samples
            List<String> sampleIds = molecularProfileCaseSets.values().stream().flatMap(Collection::stream).map(MolecularProfileCaseIdentifier::getCaseId).collect(Collectors.toList());
            List<String> studyIds = Collections.nCopies(sampleIds.size(), molecularProfile.getCancerStudyIdentifier());
            List<Sample> samples = sampleService.fetchSamples(studyIds, sampleIds, "ID");
            Map<String, Integer> sampleIdToPatientIdMap = samples.stream().collect(Collectors.toMap(Sample::getStableId, Sample::getPatientId));

            Map<String, List<MolecularProfileCaseIdentifier>> filteredMolecularProfileCaseSets = new HashMap<>();
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
            return filteredMolecularProfileCaseSets;
        } else {
            return molecularProfileCaseSets;
        }
    }

    private Map<String, GenericAssayMeta> getGenericAssayMetaByStableId(List<String> stableIds, String molecularProfileId) {
        return genericAssayService.getGenericAssayMetaByStableIdsAndMolecularIds(stableIds, stableIds.stream().map(sid -> molecularProfileId)
            .collect(Collectors.toList()), "SUMMARY").stream()
            .collect(Collectors.toMap(GenericAssayMeta::getStableId, Function.identity()));
    }

    private <T extends GenericAssayEnrichment> void calcQValues(List<T> enrichments) {
        // Sort enrichments by pValue
        Collections.sort(enrichments, GenericAssayEnrichment::compare);
        BigDecimal[] pValues = enrichments.stream().map(T::getpValue).toArray(BigDecimal[]::new);
        BigDecimal[] qValues = fisherExactTestCalculator.calcqValue(pValues);
        // Assign q-values to enrichments
        for (int i = 0; i < enrichments.size(); i++) {
            enrichments.get(i).setqValue(qValues[i]);
        }
    }
}
