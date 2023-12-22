package org.cbioportal.service.impl;

import jakarta.annotation.PostConstruct;
import org.cbioportal.model.Gene;
import org.cbioportal.model.GeneAlias;
import org.cbioportal.model.meta.BaseMeta;
import org.cbioportal.persistence.GeneRepository;
import org.cbioportal.service.GeneService;
import org.cbioportal.service.exception.GeneNotFoundException;
import org.cbioportal.service.exception.GeneWithMultipleEntrezIdsException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import static java.util.stream.Collectors.*;

@Service
public class GeneServiceImpl implements GeneService {

    public static final String ENTREZ_GENE_ID_GENE_ID_TYPE = "ENTREZ_GENE_ID";
    
    @Autowired
    private GeneRepository geneRepository;

    private Map<Integer, List<String>> geneAliasMap = new HashMap<>();

    @PostConstruct
    public void init() {
        // query all genes so they would be cached
        getAllGenes(null, null, "SUMMARY", null, null, null, null);

        geneAliasMap = geneRepository.getAllAliases().stream().collect(Collectors.groupingBy(
            GeneAlias::getEntrezGeneId, Collectors.mapping(GeneAlias::getGeneAlias, Collectors.toList())));
    }

	@Override
    public List<Gene> getAllGenes(String keyword, String alias, String projection, Integer pageSize, Integer pageNumber, String sortBy,
                                  String direction) {

        List<Gene> geneList = geneRepository.getAllGenes(keyword, alias, projection, pageSize, pageNumber, sortBy, direction);

        if (keyword != null && (pageSize == null || geneList.size() < pageSize)) {
            List<Gene> aliasMatchingGenes = findAliasMatchingGenes(keyword);
            if (pageSize != null) {
                int toIndex = aliasMatchingGenes.size() > pageSize - geneList.size() ? 
                    pageSize - geneList.size() : aliasMatchingGenes.size();
                aliasMatchingGenes = aliasMatchingGenes.subList(0, toIndex);
            }
            for (Gene gene : aliasMatchingGenes) {
                if (!geneList.stream().anyMatch(c -> c.getEntrezGeneId().equals(gene.getEntrezGeneId()))) {
                    geneList.add(gene);
                }
            }
        }

        return filterGenesWithMultipleEntrezIds(geneList);
    }

    @Override
    public BaseMeta getMetaGenes(String keyword, String alias) {

        if (keyword == null) {
            return geneRepository.getMetaGenes(keyword, alias);
        }
        else {
            BaseMeta baseMeta = new BaseMeta();
            baseMeta.setTotalCount(getAllGenes(keyword, null, "SUMMARY", null, null, null, null).size());
            return baseMeta;
        }
    }

    @Override
    public Gene getGeneByGeneticEntityId(Integer geneticEntityId) throws GeneNotFoundException {

        Gene gene;
        gene = geneRepository.getGeneByGeneticEntityId(geneticEntityId);
        if (gene == null) throw new GeneNotFoundException(Integer.toString(geneticEntityId));
        return gene;
    }  

    @Override
    public Gene getGene(String geneId) throws GeneNotFoundException, GeneWithMultipleEntrezIdsException {

        Gene gene;

        if (isInteger(geneId)) {
            gene = geneRepository.getGeneByEntrezGeneId(Integer.valueOf(geneId));
        } else {
            gene = geneRepository.getGeneByHugoGeneSymbol(geneId);
        }
        if (gene == null) {
            throw new GeneNotFoundException(geneId);
        }
        return gene;

    }

    @Override
    public List<String> getAliasesOfGene(String geneId) throws GeneNotFoundException, GeneWithMultipleEntrezIdsException {
        
        getGene(geneId);

        if (isInteger(geneId)) {
            return geneRepository.getAliasesOfGeneByEntrezGeneId(Integer.valueOf(geneId));
        } else {
            return geneRepository.getAliasesOfGeneByHugoGeneSymbol(geneId);
        }
    }

    @Override
    public List<Gene> fetchGenes(List<String> geneIds, String geneIdType, String projection) {
        
        List<Gene> geneList;
        
        if (geneIdType.equals(ENTREZ_GENE_ID_GENE_ID_TYPE)) {
            geneList = geneRepository.fetchGenesByEntrezGeneIds(geneIds.stream().filter(this::isInteger)
                .map(Integer::valueOf).collect(Collectors.toList()), projection);
        } else {
            geneList = geneRepository.fetchGenesByHugoGeneSymbols(geneIds, projection);
        }

        return filterGenesWithMultipleEntrezIds(geneList);
    }

    @Override
    public BaseMeta fetchMetaGenes(List<String> geneIds, String geneIdType) {

        BaseMeta baseMeta;

        if (geneIdType.equals(ENTREZ_GENE_ID_GENE_ID_TYPE)) {
            baseMeta = geneRepository.fetchMetaGenesByEntrezGeneIds(geneIds.stream().filter(this::isInteger)
                .map(Integer::valueOf).collect(Collectors.toList()));
        } else {
            baseMeta = geneRepository.fetchMetaGenesByHugoGeneSymbols(geneIds);
        }
        
        return baseMeta;
    }

    private boolean isInteger(String geneId) {
        return geneId.matches("^-?\\d+$");
    }

    private List<Gene> findAliasMatchingGenes(String keyword) {

        List<Gene> matchingGenes = new ArrayList<>();

        List<String> matchingEntrezGeneIds = new ArrayList<>();
        for (Map.Entry<Integer, List<String>> entry : geneAliasMap.entrySet()) {
            if (entry.getValue().contains(keyword.toLowerCase())) {
                matchingEntrezGeneIds.add(String.valueOf(entry.getKey()));
            }
        }
        if (!matchingEntrezGeneIds.isEmpty()) {
            matchingGenes = fetchGenes(matchingEntrezGeneIds, ENTREZ_GENE_ID_GENE_ID_TYPE, "SUMMARY");
        }
        return matchingGenes;
    }
    
    private List<Gene> filterGenesWithMultipleEntrezIds(List<Gene> geneList) {
        return geneList
            .stream()
            .collect(groupingBy(Gene::getHugoGeneSymbol))
            .values()
            .stream()
            .filter(groupedGenes -> groupedGenes.size() == 1) // filter out genes having duplicate hugoGeneSymbol
            .map(groupedGenes -> groupedGenes.get(0))
            .collect(toList());
    }
}
