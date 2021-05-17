package org.cbioportal.service.util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.EnumUtils;
import org.cbioportal.model.Gene;
import org.cbioportal.model.Mutation;
import org.cbioportal.model.StructuralVariant;
import org.cbioportal.service.GeneService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


@Component
public class MutationMapperUtils {
    

    @Autowired
    private GeneService geneService;

    public List<StructuralVariant> mapFusionsToStructuralVariants(List<Mutation> fusions,
            Map<String, String> molecularProfileIdReplaceMap, Boolean filterByProteinChange) {

        
        // gene symbols containing "-" is not considered in the pattern
        String pattern = "^([a-zA-Z0-9_.]+)(?:-|_|\\s)([a-zA-Z0-9_.]+)(?:\\s+(\\w+))?$";
        Pattern r = Pattern.compile(pattern);

        List<Mutation> filteredFusions = fusions;

        if (filterByProteinChange) {
            Map<String, Mutation> uniqueFusionMap = new HashMap<String, Mutation>();
            fusions.forEach(fusion -> {
                String uniqueKey = fusion.getStudyId() + fusion.getSampleId() + fusion.getProteinChange();
                // Only consider unique protein changes for the study-sample and
                // also first preference for fusions where protein change starts
                // with the gene as there are duplicate records for both the genes in protein changes.
                if (!uniqueFusionMap.containsKey(uniqueKey)
                        || fusion.getProteinChange().toUpperCase().startsWith(fusion.getGene().getHugoGeneSymbol())) {
                    uniqueFusionMap.put(uniqueKey, fusion);
                }
            });
            filteredFusions = uniqueFusionMap.values().stream().collect(Collectors.toList());
        }

        return filteredFusions.stream().map(fusion -> {
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
                            if(variantType != null && variantType.equals(VariantType.INTRAGENIC)) {
                                //convert it to oncokb deletion type
                                variantType = VariantType.DELETION;
                                site2GeneSymbol = site1GeneSymbol;
                            }
                        } else {
                            // this is in format of <gene1>-<gene2>-<optional variant-type>. ex.
                            // ZSWIM4-SLC1A6 or ZNF595-TERT fusion
                            site1GeneSymbol = matcher.group(1);
                            site2GeneSymbol = matcher.group(2);
							if (matcher.group(3) != null) {
								variantType = EnumUtils.getEnum(VariantType.class, matcher.group(3).toUpperCase());
							}
                        }

                        // only set site2Gene if its not null
                        if (site2GeneSymbol != null) {
                            if (fusion.getGene().getHugoGeneSymbol().equalsIgnoreCase(site1GeneSymbol) ||
                                fusion.getGene().getHugoGeneSymbol().equalsIgnoreCase(site2GeneSymbol)) {
                                if (site1GeneSymbol.equalsIgnoreCase(site2GeneSymbol)) {
                                    structuralVariant.setSite2EntrezGeneId(fusion.getEntrezGeneId());
                                    structuralVariant.setSite2HugoSymbol(fusion.getGene().getHugoGeneSymbol());
                                } else {
                                    Gene site1Gene = getGene(site1GeneSymbol);
                                    // we need to pick the right gene since the fusion gene has been assigned to structural variant site1
                                    Gene pickedGene = null;
                                    if (site1Gene != null && !site1Gene.getEntrezGeneId().equals(fusion.getEntrezGeneId())) {
                                        pickedGene = site1Gene;
                                    } else {
                                        Gene site2Gene = getGene(site2GeneSymbol);
                                        if (site2Gene != null && !site2Gene.getEntrezGeneId().equals(fusion.getEntrezGeneId())) {
                                            pickedGene = site2Gene;
                                        }
                                    }
                                    if (pickedGene != null) {
                                        structuralVariant.setSite2EntrezGeneId(pickedGene.getEntrezGeneId());
                                        structuralVariant.setSite2HugoSymbol(pickedGene.getHugoGeneSymbol());
                                    }
                                }
                            } else { // if the queried gene is as alias gene in protein change
                                Gene site1Gene = getGene(site1GeneSymbol);
                                Gene site2Gene = getGene(site2GeneSymbol);
                                if (site1Gene != null && site2Gene != null) { // set site2Gene when both genes are valid
                                    // and one of the site genes should match fusion gene
                                    if (fusion.getGene().getHugoGeneSymbol().equalsIgnoreCase(site1Gene.getHugoGeneSymbol())) {
                                        structuralVariant.setSite2EntrezGeneId(site2Gene.getEntrezGeneId());
                                        structuralVariant.setSite2HugoSymbol(site2Gene.getHugoGeneSymbol());
                                    } else if (fusion.getGene().getHugoGeneSymbol().equalsIgnoreCase(site2Gene.getHugoGeneSymbol())) {
                                        structuralVariant.setSite2EntrezGeneId(site1Gene.getEntrezGeneId());
                                        structuralVariant.setSite2HugoSymbol(site1Gene.getHugoGeneSymbol());
                                    }
                                }
                            }
                        }

                        if (variantType != null) {
                            structuralVariant.setVariantClass(variantType.getVariantType());
                        }
                    }
                }
            }
            return structuralVariant;
        }).collect(Collectors.toList());
    }

    private Gene getGene(String geneSymbol) {
        Gene gene = null;
        try {
            gene = geneService.getGene(geneSymbol);
        } catch (Exception e) {
            // Site2 gene is not set when gene symbol is not found in database. Check if it is an alias
            List<Gene> aliasGenes = geneService.getAllGenes(null, geneSymbol, "SUMMARY",
                null, null, null, null);

            if (CollectionUtils.isNotEmpty(aliasGenes)) {
                gene = aliasGenes.get(0);
            }
        }
        return gene;
    }

}
