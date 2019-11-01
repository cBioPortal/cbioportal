package org.cbioportal.service.util;

import java.math.BigDecimal;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.math3.stat.inference.ChiSquareTest;
import org.cbioportal.model.*;
import org.cbioportal.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class AlterationEnrichmentUtil {

    @Autowired
    private FisherExactTestCalculator fisherExactTestCalculator;
    @Autowired
    private GeneService geneService;
    @Autowired
    private MolecularProfileService molecularProfileService;
    @Autowired
    private ProfiledCasesCounter profiledCasesCounter;
    @Autowired
    private SampleService sampleService;

    public List<AlterationEnrichment> createAlterationEnrichments(
            Map<String, List<? extends AlterationCountByGene>> mutationCountsbyGroup,
            Map<String, List<MolecularProfileCaseIdentifier>> molecularProfileCaseSets,
            String enrichmentType) {
        
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
        
        return genes.stream().map(gene -> {
            AlterationEnrichment alterationEnrichment = new AlterationEnrichment();
            alterationEnrichment.setEntrezGeneId(gene.getEntrezGeneId());
            alterationEnrichment.setHugoGeneSymbol(gene.getHugoGeneSymbol());
            List<CountSummary> counts = groups
                    .stream()
                    .map(group -> {
                        CountSummary groupCasesCount = new CountSummary();
                        AlterationCountByGene mutationCountByGene = mutationCountsbyEntrezGeneIdAndGroup
                                .getOrDefault(group, new HashMap<Integer, AlterationCountByGene>()).get(gene.getEntrezGeneId());
                      
                        Integer alteredCount = mutationCountByGene != null ? mutationCountByGene.getNumberOfAlteredCases() : 0;
                        Integer profiledCount = mutationCountByGene != null ? mutationCountByGene.getNumberOfProfiledCases() : molecularProfileCaseSets.get(group).size();
                        groupCasesCount.setName(group);
                        groupCasesCount.setAlteredCount(alteredCount);
                        groupCasesCount.setProfiledCount(profiledCount);
                        return groupCasesCount;
                    })
                    .collect(Collectors.toList());

            List<CountSummary> filteredCounts = counts.stream()
                    .filter(groupCasesCount -> groupCasesCount.getProfiledCount() > 0).collect(Collectors.toList());

            // calculate p-value only if more than one group have profile cases count
            // greater than 0
            if (filteredCounts.size() > 1) {
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
            List<? extends AlterationCountByGene> alterationCountByGenes) {
        
        profiledCasesCounter.calculate(molecularProfileIds, sampleIds, alterationCountByGenes, false);
        
    }
    
    public void includeFrequencyForPatients(List<String> molecularProfileIds,
            List<String> patientIds,
            List<? extends AlterationCountByGene> alterationCountByGenes) {
        
        List<MolecularProfile> molecularProfiles = molecularProfileService
                .getMolecularProfiles(molecularProfileIds, "SUMMARY");
        
        Map<String, MolecularProfile> molecularProfileMap = molecularProfiles.stream()
                .collect(Collectors.toMap(MolecularProfile::getStableId, Function.identity()));

        List<String> studyIds = new ArrayList<String>();

        Map<String, String> patientIdMolecularProfileIdMap = new HashMap<String, String>();

        for (int index = 0; index < patientIds.size(); index++) {
            String studyId = molecularProfileMap.get(molecularProfileIds.get(index)).getCancerStudyIdentifier();
            studyIds.add(studyId);
            patientIdMolecularProfileIdMap.put(patientIds.get(index), molecularProfileIds.get(index));
        }

        List<Sample> samples = sampleService.getSamplesOfPatientsInMultipleStudies(studyIds, patientIds,
                "SUMMARY");

        List<String> molecularProfileIdsofSampleIds = samples
                .stream()
                .map(sample -> patientIdMolecularProfileIdMap.get(sample.getPatientStableId()))
                .collect(Collectors.toList());

        List<String> sampleIds = samples
                .stream()
                .map(Sample::getStableId)
                .collect(Collectors.toList());

        profiledCasesCounter.calculate(molecularProfileIdsofSampleIds, sampleIds, alterationCountByGenes, true);
    }

}
