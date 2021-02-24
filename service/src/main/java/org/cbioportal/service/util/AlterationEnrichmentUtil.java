package org.cbioportal.service.util;

import org.apache.commons.math3.stat.inference.ChiSquareTest;
import org.cbioportal.model.AlterationCountByGene;
import org.cbioportal.model.AlterationEnrichment;
import org.cbioportal.model.CountSummary;
import org.cbioportal.model.EnrichmentType;
import org.cbioportal.model.Gene;
import org.cbioportal.model.MolecularProfile;
import org.cbioportal.model.MolecularProfileCaseIdentifier;
import org.cbioportal.model.Sample;
import org.cbioportal.service.GeneService;
import org.cbioportal.service.MolecularProfileService;
import org.cbioportal.service.SampleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class AlterationEnrichmentUtil<T extends AlterationCountByGene> {

    @Autowired
    private FisherExactTestCalculator fisherExactTestCalculator;
    @Autowired
    private GeneService geneService;
    @Autowired
    private MolecularProfileService molecularProfileService;
    @Autowired
    private ProfiledCasesCounter<T> profiledCasesCounter;
    @Autowired
    private SampleService sampleService;

    public List<AlterationEnrichment> createAlterationEnrichments(
            Map<String, List<T>> mutationCountsbyGroup,
            Map<String, List<MolecularProfileCaseIdentifier>> molecularProfileCaseSets,
            EnrichmentType enrichmentType) {
        
        Map<String, Map<Integer, AlterationCountByGene>> mutationCountsbyEntrezGeneIdAndGroup = mutationCountsbyGroup
                    .entrySet()
                    .stream()
                    .collect(Collectors.toMap(
                            entry -> entry.getKey(),
                            entry -> {
                                //convert list of alterations to map with EntrezGeneId as key
                                return entry.getValue().stream()
                                        .collect(Collectors.toMap(AlterationCountByGene::getEntrezGeneId, c -> c));
                            }));

        Map<String, Long> profiledCaseCountsByGroup = profiledCasesCounter.getProfiledCaseCountsByGroup(molecularProfileCaseSets,
                enrichmentType);

        Set<Integer> allGeneIds = mutationCountsbyEntrezGeneIdAndGroup
                .values()
                .stream()
                .flatMap(x -> x.keySet().stream())
                .collect(Collectors.toSet());
        
        Set<String> groups = mutationCountsbyEntrezGeneIdAndGroup.keySet();

        List<Gene> genes = geneService.fetchGenes(
                allGeneIds
                    .stream()
                    .map(Object::toString)
                    .collect(Collectors.toList()),
                "ENTREZ_GENE_ID",
                "SUMMARY");
        
        return genes
                .stream()
                .filter(gene -> {
                    // filter genes where number of altered cases in all groups is 0
                    return groups.stream().filter(group -> {
                        AlterationCountByGene mutationCountByGene = mutationCountsbyEntrezGeneIdAndGroup
                                .getOrDefault(group, new HashMap<Integer, AlterationCountByGene>())
                                .get(gene.getEntrezGeneId());
                        return mutationCountByGene == null ? false : mutationCountByGene.getNumberOfAlteredCases() != 0;
                    }).count() > 0;
                })
                .map(gene -> {
                    AlterationEnrichment alterationEnrichment = new AlterationEnrichment();
                    alterationEnrichment.setEntrezGeneId(gene.getEntrezGeneId());
                    alterationEnrichment.setHugoGeneSymbol(gene.getHugoGeneSymbol());
                    List<CountSummary> counts = groups.stream().map(group -> {
                        CountSummary groupCasesCount = new CountSummary();
                        AlterationCountByGene mutationCountByGene = mutationCountsbyEntrezGeneIdAndGroup
                                .getOrDefault(group, new HashMap<Integer, AlterationCountByGene>())
                                .get(gene.getEntrezGeneId());

                        Integer alteredCount = mutationCountByGene != null ? mutationCountByGene.getNumberOfAlteredCases() : 0;
                        Integer profiledCount = mutationCountByGene != null ? mutationCountByGene.getNumberOfProfiledCases() : profiledCaseCountsByGroup.get(group).intValue();
                        groupCasesCount.setName(group);
                        groupCasesCount.setAlteredCount(alteredCount);
                        groupCasesCount.setProfiledCount(profiledCount);
                        return groupCasesCount;
                    }).collect(Collectors.toList());

                    List<CountSummary> filteredCounts = counts.stream()
                            .filter(groupCasesCount -> groupCasesCount.getProfiledCount() > 0)
                            .collect(Collectors.toList());

                    // groups where number of altered cases is greater than profiled cases.
                    // This is a temporary fix for https://github.com/cBioPortal/cbioportal/issues/7274
                    // and https://github.com/cBioPortal/cbioportal/issues/7418
                    long invalidDataGroups = filteredCounts
                            .stream()
                            .filter( groupCasesCount -> groupCasesCount.getAlteredCount() > groupCasesCount.getProfiledCount())
                            .count();

                    // calculate p-value only if more than one group have profile cases count
                    // greater than 0
                    if (filteredCounts.size() > 1 && invalidDataGroups == 0) {
                        double pValue;
                        // if groups size is two do Fisher Exact test else do Chi-Square test
                        if (groups.size() == 2) {

                            int alteredInNoneCount = counts.get(1).getProfiledCount() - counts.get(1).getAlteredCount();
                            int alteredOnlyInQueryGenesCount = counts.get(0).getProfiledCount()
                                    - counts.get(0).getAlteredCount();

                            pValue = fisherExactTestCalculator.getCumulativePValue(alteredInNoneCount,
                                    counts.get(1).getAlteredCount(), alteredOnlyInQueryGenesCount,
                                    counts.get(0).getAlteredCount());
                        } else {

                            long[][] array = counts.stream().map(count -> {
                                return new long[] { count.getAlteredCount(),
                                        count.getProfiledCount() - count.getAlteredCount() };
                            }).toArray(long[][]::new);

                            ChiSquareTest chiSquareTest = new ChiSquareTest();
                            pValue = chiSquareTest.chiSquareTest(array);

                            // set p-value to 1 when the cases in all groups are altered
                            if (Double.isNaN(pValue)) {
                                pValue = 1;
                            }
                        }
                        alterationEnrichment.setpValue(BigDecimal.valueOf(pValue));
                    }

                    alterationEnrichment.setCounts(counts);
                    return alterationEnrichment;
                }).collect(Collectors.toList());

    }
    
    public void includeFrequencyForSamples(List<String> molecularProfileIds,
            List<String> sampleIds,
            List<T> alterationCountByGenes,
            boolean includeMissingAlterationsFromGenePanel) {
        
        profiledCasesCounter.calculate(molecularProfileIds, sampleIds, alterationCountByGenes, false, includeMissingAlterationsFromGenePanel);
        
    }

    public void includeFrequencyForSamples(
        List<MolecularProfileCaseIdentifier> molecularProfileCaseIdentifiers,
        List<T> alterationCountByGenes,
        boolean includeMissingAlterationsFromGenePanel) {
        
        // Collect profile id and sample id arrays.
        // These are arrays of equal length, where every index
        // represents a sample id / profile id-combination
        List<String> sampleIds = new ArrayList<>();
        List<String> molecularProfileIds = new ArrayList<>();
        molecularProfileCaseIdentifiers.forEach(pair -> {
            sampleIds.add(pair.getCaseId());
            molecularProfileIds.add(pair.getMolecularProfileId());
        });

        includeFrequencyForSamples(molecularProfileIds, sampleIds, alterationCountByGenes, includeMissingAlterationsFromGenePanel);
    }
    
    public void includeFrequencyForPatients(List<String> molecularProfileIds,
            List<String> patientIds,
            List<T> alterationCountByGenes,
            boolean includeMissingAlterationsFromGenePanel) {

        Map<String, MolecularProfile> molecularProfileById = molecularProfileService
                .getMolecularProfiles(molecularProfileIds, "SUMMARY")
                .stream()
                .collect(Collectors.toMap(MolecularProfile::getStableId, Function.identity()));

        List<String> studyIds = molecularProfileIds
                .stream()
                .map(molecularProfileById::get)
                .map(MolecularProfile::getCancerStudyIdentifier)
                .collect(Collectors.toList());
        
        

        Map<String, List<Sample>> samplesByPatient = sampleService
                .getSamplesOfPatientsInMultipleStudies(studyIds, patientIds, "SUMMARY")
                .stream()
                .collect(Collectors.groupingBy(sample -> profiledCasesCounter.getUniquePatientId(sample)));

        List<String> sampleIdsToQuery = new ArrayList<>();
        List<String> molecularProfileIdsToQuery = new ArrayList<>();

        for (int index = 0; index < patientIds.size(); index++) {
            String patientId = patientIds.get(index);
            String molecularProfileId = molecularProfileIds.get(index);
            String studyId = molecularProfileById.get(molecularProfileIds.get(index)).getCancerStudyIdentifier();

            samplesByPatient
                    .getOrDefault(profiledCasesCounter.computeUniqueCaseId(studyId, patientId), new ArrayList<>())
                    .forEach(sample -> {
                        sampleIdsToQuery.add(sample.getStableId());
                        molecularProfileIdsToQuery.add(molecularProfileId);
                    });
        }

        profiledCasesCounter.calculate(molecularProfileIdsToQuery, sampleIdsToQuery, alterationCountByGenes, true, includeMissingAlterationsFromGenePanel);
    }

    public void includeFrequencyForPatients(List<MolecularProfileCaseIdentifier> molecularProfileCaseIdentifiers,
                                            List<T> alterationCountByGenes,
                                            boolean includeMissingAlterationsFromGenePanel) {

        // Collect profile id and patient id arrays.
        // These are arrays of equal length, where every index
        // represents a patient id / profile id-combination
        List<String> patientIds = new ArrayList<>();
        List<String> molecularProfileIds = new ArrayList<>();
        molecularProfileCaseIdentifiers.forEach(pair -> {
            patientIds.add(pair.getCaseId());
            molecularProfileIds.add(pair.getMolecularProfileId());
        });

        includeFrequencyForPatients(molecularProfileIds, patientIds, alterationCountByGenes, includeMissingAlterationsFromGenePanel);

    }

}
