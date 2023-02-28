package org.cbioportal.service.impl;

import org.cbioportal.model.ReferenceGenomeGene;
import org.cbioportal.service.StaticDataTimestampService;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

@RunWith(MockitoJUnitRunner.class)
public class GeneMemoizerServiceTest {
    @Mock
    private StaticDataTimestampService timestampService;

    @InjectMocks
    private GeneMemoizerServiceImpl geneMemoizerService;

    private static final String GENOME = "hg19";
    private static final List<ReferenceGenomeGene> GENES = Arrays.asList(
        gene("BRAF"),
        gene("NRAS"),
        gene("KRAS"),
        gene("SUGT1P4-STRA6LP-CCDC180")
    );
    
    private static ReferenceGenomeGene gene(String name) {
        ReferenceGenomeGene referenceGenomeGene = new ReferenceGenomeGene();
        referenceGenomeGene.setHugoGeneSymbol(name);
        return referenceGenomeGene;
    }
    
    @Test
    public void shouldReturnNullWhenUncached() throws Exception {
        initializeTimestamps(new Date(), new Date());

        List<ReferenceGenomeGene> actual = geneMemoizerService.fetchGenes("hg19");
        
        Assert.assertEquals(null, actual);
    }

    @Test
    public void shouldReturnCachedWhenNeitherExpired() throws Exception {
        initializeTimestamps(new Date(0L), new Date(0L));
        geneMemoizerService.cacheGenes(GENES, GENOME);
        
        List<ReferenceGenomeGene> actual = geneMemoizerService.fetchGenes("hg19");

        Assert.assertEquals(GENES, actual);
    }

    @Test
    public void shouldReturnNullWhenGeneExpired() throws Exception {
        initializeTimestamps(new Date(Long.MAX_VALUE), new Date(0L));
        geneMemoizerService.cacheGenes(GENES, GENOME);

        List<ReferenceGenomeGene> actual = geneMemoizerService.fetchGenes("hg19");

        Assert.assertEquals(null, actual);
    }

    @Test
    public void shouldReturnNullWhenGenomeExpired() throws Exception {
        initializeTimestamps(new Date(0L), new Date(Long.MAX_VALUE));
        geneMemoizerService.cacheGenes(GENES, GENOME);

        List<ReferenceGenomeGene> actual = geneMemoizerService.fetchGenes("hg19");

        Assert.assertEquals(null, actual);
    }

    private void initializeTimestamps(Date gene, Date referenceGenomeGene) {
        HashMap<String, Date> timestamps = new HashMap<>();
        timestamps.put("gene", gene);
        timestamps.put("reference_genome_gene", referenceGenomeGene);
        Mockito.when(timestampService.getTimestampsAsDates(Mockito.anyList()))
            .thenReturn(timestamps);
    }
}