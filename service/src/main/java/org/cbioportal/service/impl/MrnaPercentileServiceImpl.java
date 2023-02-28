package org.cbioportal.service.impl;

import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.math3.stat.ranking.NaNStrategy;
import org.apache.commons.math3.stat.ranking.NaturalRanking;
import org.apache.commons.math3.stat.ranking.TiesStrategy;
import org.cbioportal.model.GeneMolecularData;
import org.cbioportal.model.MolecularProfile;
import org.cbioportal.model.MrnaPercentile;
import org.cbioportal.service.MolecularDataService;
import org.cbioportal.service.MolecularProfileService;
import org.cbioportal.service.MrnaPercentileService;
import org.cbioportal.service.exception.MolecularProfileNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class MrnaPercentileServiceImpl implements MrnaPercentileService {

    @Autowired
    private MolecularDataService molecularDataService;
    @Autowired
    private MolecularProfileService molecularProfileService;
    
    private NaturalRanking naturalRanking = new NaturalRanking(NaNStrategy.REMOVED, TiesStrategy.MAXIMUM);

    @Override
    public List<MrnaPercentile> fetchMrnaPercentile(String molecularProfileId, String sampleId,
                                                    List<Integer> entrezGeneIds)
        throws MolecularProfileNotFoundException {

        validateMolecularProfile(molecularProfileId);

        List<GeneMolecularData> allMolecularDataList = molecularDataService.fetchMolecularData(molecularProfileId, null, 
            entrezGeneIds, "SUMMARY");

        List<GeneMolecularData> molecularDataList = allMolecularDataList.stream().filter(g -> g.getSampleId()
            .equals(sampleId)).collect(Collectors.toList());

        List<MrnaPercentile> mrnaPercentileList = new ArrayList<>();
        for (GeneMolecularData molecularData : molecularDataList) {
            if (NumberUtils.isNumber(molecularData.getValue())) {
                MrnaPercentile mrnaPercentile = new MrnaPercentile();
                mrnaPercentile.setEntrezGeneId(molecularData.getEntrezGeneId());
                mrnaPercentile.setSampleId(sampleId);
                mrnaPercentile.setPatientId(molecularData.getPatientId());
                mrnaPercentile.setStudyId(molecularData.getStudyId());
                mrnaPercentile.setMolecularProfileId(molecularProfileId);
                mrnaPercentile.setzScore(new BigDecimal(molecularData.getValue()));

                List<GeneMolecularData> molecularDataListOfGene = allMolecularDataList.stream().filter(g ->
                    g.getEntrezGeneId().equals(molecularData.getEntrezGeneId()) && NumberUtils.isNumber(g.getValue()))
                    .collect(Collectors.toList());

                double[] values = molecularDataListOfGene.stream().mapToDouble(g -> Double.parseDouble(g.getValue()))
                    .toArray();
                double[] ranks = naturalRanking.rank(values);
                double rank = ranks[molecularDataListOfGene.indexOf(molecularData)];
                double percentile = (rank / ranks.length) * 100;
                mrnaPercentile.setPercentile(BigDecimal.valueOf(percentile).setScale(2, BigDecimal.ROUND_HALF_UP));
                mrnaPercentileList.add(mrnaPercentile);
            }
        }
        
        return mrnaPercentileList;
    }

    private void validateMolecularProfile(String molecularProfileId) throws MolecularProfileNotFoundException {
        
        MolecularProfile molecularProfile = molecularProfileService.getMolecularProfile(molecularProfileId);

        if (!molecularProfile.getMolecularAlterationType()
            .equals(MolecularProfile.MolecularAlterationType.MRNA_EXPRESSION)) {

            throw new MolecularProfileNotFoundException(molecularProfileId);
        }
    }
}
