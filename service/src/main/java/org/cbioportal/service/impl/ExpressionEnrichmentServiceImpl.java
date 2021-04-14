package org.cbioportal.service.impl;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.cbioportal.model.EnrichmentType;
import org.cbioportal.model.Gene;
import org.cbioportal.model.GeneMolecularAlteration;
import org.cbioportal.model.GenericAssayEnrichment;
import org.cbioportal.model.GenericAssayMolecularAlteration;
import org.cbioportal.model.GenomicEnrichment;
import org.cbioportal.model.MolecularProfile;
import org.cbioportal.model.MolecularProfile.MolecularAlterationType;
import org.cbioportal.model.MolecularProfileCaseIdentifier;
import org.cbioportal.model.Sample;
import org.cbioportal.model.meta.GenericAssayMeta;
import org.cbioportal.persistence.MolecularDataRepository;
import org.cbioportal.service.ExpressionEnrichmentService;
import org.cbioportal.service.GeneService;
import org.cbioportal.service.GenericAssayService;
import org.cbioportal.service.MolecularProfileService;
import org.cbioportal.service.SampleService;
import org.cbioportal.service.exception.MolecularProfileNotFoundException;
import org.cbioportal.service.util.ExpressionEnrichmentUtil;
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
                .getGeneMolecularAlterationsIterable(molecularProfile.getStableId(), null, "SUMMARY");

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
    public List<GenericAssayEnrichment> getGenericAssayEnrichments(String molecularProfileId,
            Map<String, List<MolecularProfileCaseIdentifier>> molecularProfileCaseSets, EnrichmentType enrichmentType)
            throws MolecularProfileNotFoundException {

        MolecularProfile molecularProfile = molecularProfileService.getMolecularProfile(molecularProfileId);

        validateMolecularProfile(molecularProfile, Arrays.asList(MolecularAlterationType.GENERIC_ASSAY));

        Iterable<GenericAssayMolecularAlteration> maItr = molecularDataRepository
                .getGenericAssayMolecularAlterationsIterable(molecularProfile.getStableId(), null, "SUMMARY");

        Map<String, List<MolecularProfileCaseIdentifier>> filteredMolecularProfileCaseSets;
        if (molecularProfile.getPatientLevel() != null && molecularProfile.getPatientLevel() == true) {
            // Build sampleIdToPatientIdMap to quick find if a sample has shared patientId with other samples
            List<String> sampleIds = molecularProfileCaseSets.values().stream().flatMap(x -> x.stream()).map(MolecularProfileCaseIdentifier::getCaseId).collect(Collectors.toList());
            List<String> studyIds = new ArrayList<>();
            studyIds.add(molecularProfile.getCancerStudyIdentifier());
            List<Sample> samples = sampleService.fetchSamples(studyIds, sampleIds, "ID");
            Map<String, Integer> sampleIdToPatientIdMap = new HashMap<>();
            for (int i = 0; i < samples.size(); i++) {
                sampleIdToPatientIdMap.put(samples.get(i).getStableId(), samples.get(i).getPatientId());
            }
            // Build filteredMolecularProfileCaseSets
            filteredMolecularProfileCaseSets = new HashMap<>();
            for (Map.Entry<String, List<MolecularProfileCaseIdentifier>> pair : molecularProfileCaseSets.entrySet()) {
                Set<Integer> patientSet = new HashSet<Integer>();
                List<MolecularProfileCaseIdentifier> identifierListUniqueByPatientId = new ArrayList<>();
                for (MolecularProfileCaseIdentifier caseIdentifier : pair.getValue()) {
                    if (patientSet.contains(sampleIdToPatientIdMap.get(caseIdentifier.getCaseId()))) {
                        continue;
                    } else {
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


    private void validateMolecularProfile(MolecularProfile molecularProfile,
            List<MolecularAlterationType> validMolecularAlterationTypes) throws MolecularProfileNotFoundException {
        if (!validMolecularAlterationTypes.contains(molecularProfile.getMolecularAlterationType())) {
            throw new MolecularProfileNotFoundException(molecularProfile.getStableId());
        }
    }

}
