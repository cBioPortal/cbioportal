package org.cbioportal.service.impl;
import java.math.BigDecimal;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.lang3.BooleanUtils;
import org.cbioportal.model.MolecularProfile.MolecularAlterationType;
import org.cbioportal.model.meta.GenericAssayMeta;
import org.cbioportal.model.EnrichmentType;
import org.cbioportal.model.GenericAssayBinaryEnrichment;
import org.cbioportal.model.GenericAssayCategoricalEnrichment;
import org.cbioportal.model.GenericAssayMolecularAlteration;
import org.cbioportal.model.MolecularProfileCaseIdentifier;
import org.cbioportal.model.MolecularProfile;
import org.cbioportal.model.Sample;
import org.cbioportal.model.GenericAssayEnrichment;
import org.cbioportal.model.GenomicEnrichment;
import org.cbioportal.model.Gene;
import org.cbioportal.model.GeneMolecularAlteration;
import org.cbioportal.persistence.MolecularDataRepository;
import org.cbioportal.service.ExpressionEnrichmentService;
import org.cbioportal.service.GeneService;
import org.cbioportal.service.GenericAssayService;
import org.cbioportal.service.MolecularProfileService;
import org.cbioportal.service.SampleService;
import org.cbioportal.service.exception.MolecularProfileNotFoundException;
import org.cbioportal.service.util.ExpressionEnrichmentUtil;
import org.cbioportal.service.util.FisherExactTestCalculator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ExpressionEnrichmentServiceImpl implements ExpressionEnrichmentService {

    @Autowired
    private MolecularProfileService molecularProfileService;
    @Autowired
    private MolecularDataRepository molecularDataRepository;
    @Autowired
    private GeneService geneService;
    @Autowired
    private ExpressionEnrichmentUtil expressionEnrichmentUtil;
    @Autowired
    private GenericAssayService genericAssayService;
    @Autowired
    private SampleService sampleService;
    @Autowired
    private FisherExactTestCalculator fisherExactTestCalculator = new FisherExactTestCalculator();
    @Override
    // transaction needs to be setup here in order to return Iterable from
    // molecularDataService in fetchCoExpressions
    @Transactional(readOnly = true)
    public List<GenomicEnrichment> getGenomicEnrichments(String molecularProfileId,
                                                         Map<String, List<MolecularProfileCaseIdentifier>> molecularProfileCaseSets, EnrichmentType enrichmentType)
        throws MolecularProfileNotFoundException {
        MolecularProfile molecularProfile = molecularProfileService.getMolecularProfile(molecularProfileId);
        List<MolecularAlterationType> validGenomicMolecularAlterationTypes = Arrays.asList(
            MolecularAlterationType.MICRO_RNA_EXPRESSION, MolecularAlterationType.MRNA_EXPRESSION,
            MolecularAlterationType.MRNA_EXPRESSION_NORMALS, MolecularAlterationType.RNA_EXPRESSION,
            MolecularAlterationType.METHYLATION, MolecularAlterationType.METHYLATION_BINARY,
            MolecularAlterationType.PHOSPHORYLATION, MolecularAlterationType.PROTEIN_LEVEL,
            MolecularAlterationType.PROTEIN_ARRAY_PROTEIN_LEVEL,
            MolecularAlterationType.PROTEIN_ARRAY_PHOSPHORYLATION);
        validateMolecularProfile(molecularProfile, validGenomicMolecularAlterationTypes);
        Iterable<GeneMolecularAlteration> maItr = molecularDataRepository
            .getGeneMolecularAlterationsIterableFast(molecularProfile.getStableId());
        List<GenomicEnrichment> expressionEnrichments = expressionEnrichmentUtil.getEnrichments(molecularProfile,
            molecularProfileCaseSets, enrichmentType, maItr);
        List<Integer> entrezGeneIds = expressionEnrichments.stream().map(GenomicEnrichment::getEntrezGeneId)
            .collect(Collectors.toList());
        Map<Integer, List<Gene>> geneMapByEntrezId = geneService
            .fetchGenes(entrezGeneIds.stream().map(Object::toString).collect(Collectors.toList()), "ENTREZ_GENE_ID",
                "SUMMARY")
            .stream().collect(Collectors.groupingBy(Gene::getEntrezGeneId));
        return expressionEnrichments.stream()
            // filter Enrichments having no gene reference object(this
            // happens when multiple
            // entrez ids map to same hugo gene symbol)
            .filter(expressionEnrichment -> geneMapByEntrezId.containsKey(expressionEnrichment.getEntrezGeneId()))
            .map(expressionEnrichment -> {
                Gene gene = geneMapByEntrezId.get(expressionEnrichment.getEntrezGeneId()).get(0);
                expressionEnrichment.setHugoGeneSymbol(gene.getHugoGeneSymbol());
                return expressionEnrichment;
            }).collect(Collectors.toList());
    }
    @Override
    // transaction needs to be setup here in order to return Iterable from
    // molecularDataRepository in getGenericAssayMolecularAlterationsIterable
    @Transactional(readOnly = true)
    public List<GenericAssayEnrichment> getGenericAssayNumericalEnrichments(String molecularProfileId,
                                                                   Map<String, List<MolecularProfileCaseIdentifier>> molecularProfileCaseSets, EnrichmentType enrichmentType)
        throws MolecularProfileNotFoundException {
        MolecularProfile molecularProfile = molecularProfileService.getMolecularProfile(molecularProfileId);
        validateMolecularProfile(molecularProfile, Arrays.asList(MolecularAlterationType.GENERIC_ASSAY));
        Iterable<GenericAssayMolecularAlteration> maItr = molecularDataRepository
            .getGenericAssayMolecularAlterationsIterable(molecularProfile.getStableId(), null, "SUMMARY");
        Map<String, List<MolecularProfileCaseIdentifier>> filteredMolecularProfileCaseSets;
        if (BooleanUtils.isTrue(molecularProfile.getPatientLevel())) {
            // Build sampleIdToPatientIdMap to quick find if a sample has shared patientId with other samples
            List<String> sampleIds = molecularProfileCaseSets.values().stream().flatMap(Collection::stream).map(MolecularProfileCaseIdentifier::getCaseId).collect(Collectors.toList());
            List<String> studyIds = Collections.nCopies(sampleIds.size(), molecularProfile.getCancerStudyIdentifier());
            List<Sample> samples = sampleService.fetchSamples(studyIds, sampleIds, "SUMMARY");
            Map<String, Integer> sampleIdToPatientIdMap = samples.stream().collect(Collectors.toMap(Sample::getStableId, Sample::getPatientId));
            // Build filteredMolecularProfileCaseSets
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
        List<GenericAssayEnrichment> genericAssayEnrichments = expressionEnrichmentUtil.getEnrichments(molecularProfile,
            filteredMolecularProfileCaseSets, enrichmentType, maItr);
        List<String> getGenericAssayStableIds = genericAssayEnrichments.stream()
            .map(GenericAssayEnrichment::getStableId).collect(Collectors.toList());
        Map<String, GenericAssayMeta> genericAssayMetaByStableId = genericAssayService
            .getGenericAssayMetaByStableIdsAndMolecularIds(getGenericAssayStableIds,
                getGenericAssayStableIds.stream().map(stableId -> molecularProfileId)
                    .collect(Collectors.toList()),
                "SUMMARY")
            .stream().collect(Collectors.toMap(GenericAssayMeta::getStableId, Function.identity()));
        return genericAssayEnrichments.stream().map(enrichmentDatum -> {
            enrichmentDatum.setGenericEntityMetaProperties(
                genericAssayMetaByStableId.get(enrichmentDatum.getStableId()).getGenericEntityMetaProperties());
            return enrichmentDatum;
        }).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<GenericAssayBinaryEnrichment> getGenericAssayBinaryEnrichments(
        String molecularProfileId,
        Map<String, List<MolecularProfileCaseIdentifier>> molecularProfileCaseSets, EnrichmentType enrichmentType)
        throws MolecularProfileNotFoundException {

        // Validate and fetch molecular profile
        MolecularProfile molecularProfile = getAndValidateMolecularProfile(molecularProfileId, "BINARY");

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

        MolecularProfile molecularProfile = getAndValidateMolecularProfile(molecularProfileId, "CATEGORICAL");

        Iterable<GenericAssayMolecularAlteration> maItr = molecularDataRepository
            .getGenericAssayMolecularAlterationsIterable(molecularProfile.getStableId(), null, "SUMMARY");

        Map<String, List<MolecularProfileCaseIdentifier>> filteredMolecularProfileCaseSets = filterMolecularProfileCaseSets(molecularProfile, molecularProfileCaseSets);

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

    private MolecularProfile getAndValidateMolecularProfile(String molecularProfileId, String dataType) throws MolecularProfileNotFoundException {
        MolecularProfile molecularProfile = molecularProfileService.getMolecularProfile(molecularProfileId);
        validateMolecularProfile(molecularProfile, Arrays.asList(MolecularProfile.MolecularAlterationType.GENERIC_ASSAY), dataType);
        return molecularProfile;
    }

    private void validateMolecularProfile(MolecularProfile molecularProfile,
                                          List<MolecularProfile.MolecularAlterationType> validMolecularAlterationTypes,
                                          String dataType) throws MolecularProfileNotFoundException {
        if (!validMolecularAlterationTypes.contains(molecularProfile.getMolecularAlterationType())) {
            // Check alteration type
            throw new MolecularProfileNotFoundException(molecularProfile.getStableId());
        }
        // Check datatype for binary or categorical
        if(molecularProfile.getMolecularAlterationType().equals(MolecularProfile.MolecularAlterationType.GENERIC_ASSAY) &&
            !molecularProfile.getDatatype().equals(dataType))
            throw new MolecularProfileNotFoundException(molecularProfile.getStableId());
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
    private void validateMolecularProfile(MolecularProfile molecularProfile,
                                          List<MolecularAlterationType> validMolecularAlterationTypes) throws MolecularProfileNotFoundException {
        if (!validMolecularAlterationTypes.contains(molecularProfile.getMolecularAlterationType())) {
            throw new MolecularProfileNotFoundException(molecularProfile.getStableId());
        }
    }
}
