package org.cbioportal.service.impl;

import org.cbioportal.model.CopyNumberCount;
import org.cbioportal.model.CopyNumberSampleCountByGene;
import org.cbioportal.model.GeneticProfile;
import org.cbioportal.service.CopyNumberCountService;
import org.cbioportal.service.DiscreteCopyNumberService;
import org.cbioportal.service.GeneticDataService;
import org.cbioportal.service.GeneticProfileService;
import org.cbioportal.service.exception.GeneticProfileNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class CopyNumberCountServiceImpl implements CopyNumberCountService {

    @Autowired
    private GeneticDataService geneticDataService;
    @Autowired
    private DiscreteCopyNumberService discreteCopyNumberService;
    @Autowired
    private GeneticProfileService geneticProfileService;

    @Override
    public List<CopyNumberCount> fetchCopyNumberCounts(String geneticProfileId, List<Integer> entrezGeneIds,
                                                       List<Integer> alterations)
        throws GeneticProfileNotFoundException {

        validateGeneticProfile(geneticProfileId);

        Integer numberOfSamplesInGeneticProfile = geneticDataService.getNumberOfSamplesInGeneticProfile(
            geneticProfileId);
        List<CopyNumberSampleCountByGene> copyNumberSampleCountByGeneList = discreteCopyNumberService
            .getSampleCountByGeneAndAlteration(geneticProfileId, entrezGeneIds, alterations);

        List<CopyNumberCount> copyNumberCounts = new ArrayList<>();
        for (int i = 0; i < alterations.size(); i++) {
            Integer alteration = alterations.get(i);
            Integer entrezGeneId = entrezGeneIds.get(i);

            CopyNumberCount copyNumberCount = new CopyNumberCount();
            copyNumberCount.setGeneticProfileId(geneticProfileId);
            copyNumberCount.setEntrezGeneId(entrezGeneId);
            copyNumberCount.setAlteration(alteration);
            copyNumberCount.setNumberOfSamples(numberOfSamplesInGeneticProfile);

            Optional<CopyNumberSampleCountByGene> copyNumberSampleCountByGene = copyNumberSampleCountByGeneList.stream()
                .filter(p -> p.getEntrezGeneId().equals(entrezGeneId) && p.getAlteration().equals(alteration))
                .findFirst();
            copyNumberSampleCountByGene.ifPresent(m -> copyNumberCount.setNumberOfSamplesWithAlterationInGene(m
                .getSampleCount()));
            
            copyNumberCounts.add(copyNumberCount);
        }

        return copyNumberCounts;
    }

    private void validateGeneticProfile(String geneticProfileId) throws GeneticProfileNotFoundException {

        GeneticProfile geneticProfile = geneticProfileService.getGeneticProfile(geneticProfileId);

        if (!geneticProfile.getGeneticAlterationType()
            .equals(GeneticProfile.GeneticAlterationType.COPY_NUMBER_ALTERATION)) {

            throw new GeneticProfileNotFoundException(geneticProfileId);
        }
    }
}
