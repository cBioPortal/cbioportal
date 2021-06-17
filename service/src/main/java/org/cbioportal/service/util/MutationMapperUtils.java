package org.cbioportal.service.util;

import org.apache.commons.lang3.EnumUtils;
import org.apache.commons.lang3.tuple.Triple;
import org.cbioportal.model.Gene;
import org.cbioportal.model.Mutation;
import org.cbioportal.model.StructuralVariant;
import org.cbioportal.service.GeneService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Component
public class MutationMapperUtils {

    // gene symbols containing "-" is not considered in the pattern
    private final String PROTEIN_CHANGE_REGEX = "^([a-zA-Z0-9_.]+)(?:(?:-|_|\\s)([a-zA-Z0-9_.]+)(?:\\s+(\\w+))?)?$";
    private final Pattern PROTEIN_CHANGE_PATTERN = Pattern.compile(PROTEIN_CHANGE_REGEX);
    @Autowired
    private GeneService geneService;

    public List<StructuralVariant> mapFusionsToStructuralVariants(List<Mutation> fusions,
                                                                  Map<String, String> molecularProfileIdReplaceMap, Boolean filterByProteinChange) {

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

        Set<String> geneSymbolsFromProteinChange = filteredFusions
            .stream()
            .filter(fusion -> fusion.getProteinChange() != null &&
                !fusion.getProteinChange().equalsIgnoreCase("Fusion") &&
                !fusion.getProteinChange().equalsIgnoreCase("SV"))
            .flatMap(fusion -> {
                Triple<String, String, VariantType> result = extractInformationFromProteinChange(fusion.getProteinChange());
                List<String> symbols = new ArrayList<>();
                if (result.getLeft() != null) {
                    symbols.add(result.getLeft()); // add gene1
                }
                if (result.getMiddle() != null) {
                    symbols.add(result.getMiddle()); // add gene2
                }
                return symbols.stream();
            })
            .collect(Collectors.toSet());

        Map<String, Gene> geneByHugoGeneSymbol = geneService
            .fetchGenes(new ArrayList<>(geneSymbolsFromProteinChange), "HUGO_GENE_SYMBOL", null)
            .stream()
            .collect(Collectors.toMap(gene -> gene.getHugoGeneSymbol().toUpperCase(), Function.identity()));

        geneSymbolsFromProteinChange
            .stream()
            .filter(geneSymbol -> !geneByHugoGeneSymbol.containsKey(geneSymbol))
            .forEach(geneSymbol -> {
                // check if geneSymbol is an alias
                List<Gene> aliasGenes = geneService.getAllGenes(null, geneSymbol, "null",
                    null, null, null, null);
                if (!aliasGenes.isEmpty()) {
                    geneByHugoGeneSymbol.put(geneSymbol, aliasGenes.get(0));
                }
            });

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

                    Matcher matcher = PROTEIN_CHANGE_PATTERN.matcher(proteinChange);
                    Triple<String, String, VariantType> result = extractInformationFromProteinChange(fusion.getProteinChange());
                    String site1GeneSymbol = result.getLeft();
                    String site2GeneSymbol = result.getMiddle();
                    VariantType variantType = result.getRight();

                    // only set site2Gene if its not null
                    if (site2GeneSymbol != null) {
                        if (fusion.getGene().getHugoGeneSymbol().equalsIgnoreCase(site1GeneSymbol) ||
                            fusion.getGene().getHugoGeneSymbol().equalsIgnoreCase(site2GeneSymbol)) {
                            if (site1GeneSymbol.equalsIgnoreCase(site2GeneSymbol)) {
                                structuralVariant.setSite2EntrezGeneId(fusion.getEntrezGeneId());
                                structuralVariant.setSite2HugoSymbol(fusion.getGene().getHugoGeneSymbol());
                            } else {
                                Gene site1Gene = geneByHugoGeneSymbol.get(site1GeneSymbol);
                                // we need to pick the right gene since the fusion gene has been assigned to structural variant site1
                                Gene pickedGene = null;
                                if (site1Gene != null && !site1Gene.getEntrezGeneId().equals(fusion.getEntrezGeneId())) {
                                    pickedGene = site1Gene;
                                } else {
                                    Gene site2Gene = geneByHugoGeneSymbol.get(site2GeneSymbol);
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
                            Gene site1Gene = geneByHugoGeneSymbol.get(site1GeneSymbol);
                            Gene site2Gene = geneByHugoGeneSymbol.get(site2GeneSymbol);
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
            return structuralVariant;
        }).collect(Collectors.toList());
    }

    public Triple<String, String, VariantType> extractInformationFromProteinChange(String proteinChange) {

        Matcher matcher = PROTEIN_CHANGE_PATTERN.matcher(proteinChange.toUpperCase());
        String site1GeneSymbol = null;
        String site2GeneSymbol = null;
        VariantType variantType = null;

        if (matcher.find()) {
            if (EnumUtils.isValidEnum(VariantType.class, matcher.group(1))) {
                // if protein change contains only variant type. ex: INTRAGENIC
                variantType = EnumUtils.getEnum(VariantType.class, matcher.group(1));

            } else if (EnumUtils.isValidEnum(VariantType.class, matcher.group(2))) {
                // this is in format of <gene>-<variant-type>. ex: TUFT1-intragenic
                site1GeneSymbol = matcher.group(1);
                variantType = EnumUtils.getEnum(VariantType.class, matcher.group(2));
            } else {
                // this is in format of <gene1>-<gene2>-<optional variant-type>. ex.
                // ZSWIM4-SLC1A6 or ZNF595-TERT fusion
                site1GeneSymbol = matcher.group(1);
                site2GeneSymbol = matcher.group(2);
                if (matcher.group(3) != null) {
                    variantType = EnumUtils.getEnum(VariantType.class, matcher.group(3));
                }
            }
        }
        return Triple.of(site1GeneSymbol, site2GeneSymbol, variantType);
    }

}
