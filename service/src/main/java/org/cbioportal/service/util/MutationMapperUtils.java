package org.cbioportal.service.util;

import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.lang3.EnumUtils;
import org.cbioportal.model.Gene;
import org.cbioportal.model.Mutation;
import org.cbioportal.model.MutationCountByGene;
import org.cbioportal.model.StructuralVariant;
import org.cbioportal.model.StructuralVariantCountByGene;
import org.cbioportal.service.GeneService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


@Component
public class MutationMapperUtils {
    

    @Autowired
    private GeneService geneService;

    public List<StructuralVariant> mapFusionsToStructuralVariants(List<Mutation> fusions,
            Map<String, String> molecularProfileIdReplaceMap) {

        
        // gene symbols containing "-" is not considered in the pattern
        String pattern = "^([a-zA-Z0-9_.]+)(?:-|_|\\s)([a-zA-Z0-9_.]+)(?:\\s+(\\w+))?$";
        Pattern r = Pattern.compile(pattern);
        
        return fusions.stream().map(fusion -> {
            StructuralVariant structuralVariant = new StructuralVariant();

            // Sample details
            structuralVariant.setPatientId(fusion.getPatientId());
            structuralVariant.setSampleId(fusion.getSampleId());
            structuralVariant.setStudyId(fusion.getStudyId());
            structuralVariant.setMolecularProfileId(molecularProfileIdReplaceMap
                    .getOrDefault(fusion.getMolecularProfileId(), fusion.getMolecularProfileId()));

            // Fusion details
            structuralVariant.setSite1EntrezGeneId(fusion.getEntrezGeneId());
            structuralVariant.setSite1HugoSymbol(fusion.getGene().getHugoGeneSymbol());
            structuralVariant.setSite1Chromosome(fusion.getChr());
            structuralVariant.setCenter(fusion.getCenter());
            structuralVariant.setSite1Position(fusion.getStartPosition().intValue());
            structuralVariant.setComments(fusion.getKeyword());
            structuralVariant.setNcbiBuild(fusion.getNcbiBuild());
            structuralVariant.setVariantClass(fusion.getMutationType());
            structuralVariant.setEventInfo(fusion.getProteinChange());
            if (fusion.getProteinChange() != null) {
                String proteinChange = fusion.getProteinChange();
                // only parse proteinChange when its not Fusion or SV
                if (!(proteinChange.equalsIgnoreCase("Fusion") || proteinChange.equalsIgnoreCase("SV"))) {

                    Matcher matcher = r.matcher(proteinChange);
                    String site1GeneSymbol = null;
                    String site2GeneSymbol = null;
                    VariantType variantType = null;

                    if (matcher.find()) {
                        if (EnumUtils.isValidEnum(VariantType.class, matcher.group(1).toUpperCase())) {
                            // if protein change contains only variant type. ex: INTRAGENIC
                            variantType = EnumUtils.getEnum(VariantType.class, matcher.group(1).toUpperCase());

                        } else if (EnumUtils.isValidEnum(VariantType.class, matcher.group(2).toUpperCase())) {
                            // this is in format of <gene>-<variant-type>. ex: TUFT1-intragenic
                            site1GeneSymbol = matcher.group(1);
                            variantType = EnumUtils.getEnum(VariantType.class, matcher.group(2).toUpperCase());
                        } else {
                            // this is in format of <gene1>-<gene2>-<optional variant-type>. ex.
                            // ZSWIM4-SLC1A6 or ZNF595-TERT fusion
                            site1GeneSymbol = matcher.group(1);
                            site2GeneSymbol = matcher.group(2);
							if (matcher.group(3) != null
									&& EnumUtils.isValidEnum(VariantType.class, matcher.group(3).toUpperCase())) {
								variantType = EnumUtils.getEnum(VariantType.class, matcher.group(3).toUpperCase());
							}
                        }

                        // only set site2Gene if its not null
                        if (site2GeneSymbol != null) {
                            if (site1GeneSymbol != null && site1GeneSymbol.equalsIgnoreCase(site2GeneSymbol)) {
                                structuralVariant.setSite2EntrezGeneId(fusion.getEntrezGeneId());
                                structuralVariant.setSite2HugoSymbol(fusion.getGene().getHugoGeneSymbol());
                            } else {
                                try {
                                    Gene site2Gene = geneService.getGene(site2GeneSymbol);
                                    if (site2Gene != null) {
                                        structuralVariant.setSite2EntrezGeneId(site2Gene.getGeneticEntityId());
                                        structuralVariant.setSite2HugoSymbol(site2Gene.getHugoGeneSymbol());
                                    }
                                } catch (Exception e) {
                                    // Site2 gene is not set when gene symbol is not found in database
                                }
                            }
                        }

                        if (variantType != null) {
                            structuralVariant.setVariantClass(variantType.name());
                        }
                    }
                }
            }
            return structuralVariant;
        }).collect(Collectors.toList());
    }

    public List<StructuralVariantCountByGene> mapFusionCountsToStructuralVariantCounts(
            List<MutationCountByGene> mutationCountByGenes) {

        return mutationCountByGenes.stream().map(fusion -> {
            StructuralVariantCountByGene structuralVariantCountByGene = new StructuralVariantCountByGene();
            structuralVariantCountByGene.setEntrezGeneId(fusion.getEntrezGeneId());
            structuralVariantCountByGene.setHugoGeneSymbol(fusion.getHugoGeneSymbol());
            structuralVariantCountByGene.setNumberOfAlteredCases(fusion.getNumberOfAlteredCases());
            structuralVariantCountByGene.setNumberOfProfiledCases(fusion.getNumberOfProfiledCases());
            structuralVariantCountByGene.setTotalCount(fusion.getTotalCount());
            structuralVariantCountByGene.setMatchingGenePanelIds(fusion.getMatchingGenePanelIds());
            return structuralVariantCountByGene;
        }).collect(Collectors.toList());
    }

}
