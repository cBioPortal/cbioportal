package org.cbioportal.service.impl;

import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.math3.stat.ranking.NaNStrategy;
import org.apache.commons.math3.stat.ranking.NaturalRanking;
import org.apache.commons.math3.stat.ranking.TiesStrategy;
import org.cbioportal.model.GeneGeneticData;
import org.cbioportal.model.GeneticProfile;
import org.cbioportal.model.MrnaPercentile;
import org.cbioportal.service.GeneticDataService;
import org.cbioportal.service.GeneticProfileService;
import org.cbioportal.service.MrnaPercentileService;
import org.cbioportal.service.exception.GeneticProfileNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class MrnaPercentileServiceImpl implements MrnaPercentileService {

    @Autowired
    private GeneticDataService geneticDataService;
    @Autowired
    private GeneticProfileService geneticProfileService;

    @Override
    @PreAuthorize("hasPermission(#geneticProfileId, 'GeneticProfile', 'read')")
    public List<MrnaPercentile> fetchMrnaPercentile(String geneticProfileId, String sampleId,
                                                    List<Integer> entrezGeneIds)
        throws GeneticProfileNotFoundException {

        validateGeneticProfile(geneticProfileId);

        List<GeneGeneticData> allGeneticDataList = geneticDataService.fetchGeneticData(
            geneticProfileId, null, entrezGeneIds, "SUMMARY");

        List<GeneGeneticData> geneticDataList = allGeneticDataList.stream().filter(g -> g.getSampleId().equals(sampleId))
            .collect(Collectors.toList());

        List<MrnaPercentile> mrnaPercentileList = new ArrayList<>();
        for (GeneGeneticData geneticData : geneticDataList) {
            if (NumberUtils.isNumber(geneticData.getValue())) {
                MrnaPercentile mrnaPercentile = new MrnaPercentile();
                mrnaPercentile.setEntrezGeneId(geneticData.getEntrezGeneId());
                mrnaPercentile.setSampleId(sampleId);
                mrnaPercentile.setGeneticProfileId(geneticProfileId);
                mrnaPercentile.setzScore(new BigDecimal(geneticData.getValue()));

                List<GeneGeneticData> geneticDataListOfGene = allGeneticDataList.stream().filter(g ->
                    g.getEntrezGeneId().equals(geneticData.getEntrezGeneId()) && NumberUtils.isNumber(g.getValue()))
                    .collect(Collectors.toList());

                double[] values = geneticDataListOfGene.stream().mapToDouble(g -> Double.parseDouble(g.getValue()))
                    .toArray();
                
                NaturalRanking naturalRanking = new NaturalRanking(NaNStrategy.REMOVED, TiesStrategy.MAXIMUM);
                double[] ranks = naturalRanking.rank(values);
                double rank = ranks[geneticDataListOfGene.indexOf(geneticData)];
                double percentile = (rank / ranks.length) * 100;
                mrnaPercentile.setPercentile(BigDecimal.valueOf(percentile).setScale(2, BigDecimal.ROUND_HALF_UP));
                mrnaPercentileList.add(mrnaPercentile);
            }
        }
        
        return mrnaPercentileList;
    }

    private void validateGeneticProfile(String geneticProfileId) throws GeneticProfileNotFoundException {
        
        GeneticProfile geneticProfile = geneticProfileService.getGeneticProfile(geneticProfileId);

        if (!geneticProfile.getGeneticAlterationType()
            .equals(GeneticProfile.GeneticAlterationType.MRNA_EXPRESSION)) {

            throw new GeneticProfileNotFoundException(geneticProfileId);
        }
    }
}
