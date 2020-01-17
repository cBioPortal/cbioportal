package org.cbioportal.service.impl;

import org.cbioportal.model.ReferenceGenomeGene;
import org.cbioportal.service.GeneMemoizerService;
import org.cbioportal.service.StaticDataTimestampService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class GeneMemoizerServiceImpl implements GeneMemoizerService {
    @Autowired
    private StaticDataTimestampService timestampService;
    
    private static final List<String> TABLES = Arrays.asList("gene", "reference_genome_gene");

    private static final Map<String, List<ReferenceGenomeGene>> memoization = new HashMap<>();
    private static final Map<String, Date> expiry = new HashMap<>();
    private static final Object lock = new Object();    
    
    @Override
    public List<ReferenceGenomeGene> fetchGenes(String genomeName) {
        synchronized (lock) {
            if (memoization.containsKey(genomeName) && allTablesUpToDate(expiry.get(genomeName))) {
                return memoization.get(genomeName);
            }
        }
        
        return null;
    }
    
    private boolean allTablesUpToDate(Date expiration) {
        Map<String, Date> timestamps = timestampService.getTimestampsAsDates(TABLES);
        return TABLES.stream()
            .map((table) -> timestamps.containsKey(table) && timestamps.get(table).before(expiration))
            .reduce((all, next) -> all && next)
            .orElse(false);
    }
    
    @Override
    public void cacheGenes(List<ReferenceGenomeGene> genes, String genomeName) {
        synchronized (lock) {
            expiry.put(genomeName, new Date());
            memoization.put(genomeName, genes);
        }
    }
}
