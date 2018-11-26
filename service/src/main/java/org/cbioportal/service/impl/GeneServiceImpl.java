package org.cbioportal.service.impl;

import org.cbioportal.model.Gene;
import org.cbioportal.model.meta.BaseMeta;
import org.cbioportal.persistence.GeneRepository;
import org.cbioportal.service.GeneService;
import org.cbioportal.service.exception.GeneNotFoundException;
import org.cbioportal.service.util.ChromosomeCalculator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class GeneServiceImpl implements GeneService {

    public static final String ENTREZ_GENE_ID_GENE_ID_TYPE = "ENTREZ_GENE_ID";
    
    @Autowired
    private GeneRepository geneRepository;
    @Autowired
    private ChromosomeCalculator chromosomeCalculator;

    @PostConstruct
    public void init() {
        getAllGenes(null, null, "SUMMARY", null, null, null, null);
    }

    @Override
    public List<Gene> getAllGenes(String keyword, String alias, String projection, Integer pageSize, Integer pageNumber, String sortBy,
                                  String direction) {

        List<Gene> geneList = geneRepository.getAllGenes(keyword, alias, projection, pageSize, pageNumber, sortBy, direction);

        geneList.forEach(gene -> chromosomeCalculator.setChromosome(gene));
        return geneList;
    }

    @Override
    public BaseMeta getMetaGenes(String keyword, String alias) {

        return geneRepository.getMetaGenes(keyword, alias);
    }

    @Override
    public Gene getGene(String geneId) throws GeneNotFoundException {

        Gene gene;

        if (isInteger(geneId)) {
            gene = geneRepository.getGeneByEntrezGeneId(Integer.valueOf(geneId));
        } else {
            gene = geneRepository.getGeneByHugoGeneSymbol(geneId);
        }

        if (gene == null) {
            throw new GeneNotFoundException(geneId);
        }

        chromosomeCalculator.setChromosome(gene);
        return gene;
    }

    @Override
    public List<String> getAliasesOfGene(String geneId) throws GeneNotFoundException {
        
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

        geneList.forEach(gene -> chromosomeCalculator.setChromosome(gene));
        return geneList;
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
}
