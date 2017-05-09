package org.cbioportal.service.impl;

import org.cbioportal.model.DiscreteCopyNumberSampleCountByGene;
import org.cbioportal.model.DiscreteCopyNumberData;
import org.cbioportal.model.GeneticData;
import org.cbioportal.model.GeneticProfile;
import org.cbioportal.model.meta.BaseMeta;
import org.cbioportal.persistence.DiscreteCopyNumberRepository;
import org.cbioportal.service.DiscreteCopyNumberService;
import org.cbioportal.service.GeneticDataService;
import org.cbioportal.service.GeneticProfileService;
import org.cbioportal.service.exception.GeneticProfileNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class DiscreteCopyNumberServiceImpl implements DiscreteCopyNumberService {

    @Autowired
    private DiscreteCopyNumberRepository discreteCopyNumberRepository;
    @Autowired
    private GeneticDataService geneticDataService;
    @Autowired
    private GeneticProfileService geneticProfileService;

    @Override
    @PreAuthorize("hasPermission(#geneticProfileId, 'GeneticProfile', 'read')")
    public List<DiscreteCopyNumberData> getDiscreteCopyNumbersInGeneticProfileBySampleListId(
        String geneticProfileId,
        String sampleListId,
        List<Integer> entrezGeneIds,
        List<Integer> alterations,
        String projection)
        throws GeneticProfileNotFoundException {

        validateGeneticProfile(geneticProfileId);
        if (isHomdelOrAmpOnly(alterations)) {

            return discreteCopyNumberRepository.getDiscreteCopyNumbersInGeneticProfileBySampleListId(geneticProfileId,
                sampleListId, entrezGeneIds, alterations, projection);
        }

        return geneticDataService.getGeneticData(geneticProfileId, sampleListId, entrezGeneIds, projection).stream()
            .filter(g -> isValidAlteration(alterations, g)).map(this::convert).collect(Collectors.toList());
    }

    @Override
    @PreAuthorize("hasPermission(#geneticProfileId, 'GeneticProfile', 'read')")
    public BaseMeta getMetaDiscreteCopyNumbersInGeneticProfileBySampleListId(String geneticProfileId,
                                                                             String sampleListId,
                                                                             List<Integer> entrezGeneIds,
                                                                             List<Integer> alterations)
        throws GeneticProfileNotFoundException {

        validateGeneticProfile(geneticProfileId);
        if (isHomdelOrAmpOnly(alterations)) {
            return discreteCopyNumberRepository.getMetaDiscreteCopyNumbersInGeneticProfileBySampleListId(
                geneticProfileId, sampleListId, entrezGeneIds, alterations);
        }

        long totalCount = geneticDataService.getGeneticData(geneticProfileId, sampleListId, entrezGeneIds, "ID")
            .stream().filter(g -> isValidAlteration(alterations, g)).count();

        BaseMeta baseMeta = new BaseMeta();
        baseMeta.setTotalCount(Math.toIntExact(totalCount));

        return baseMeta;
    }

    @Override
    @PreAuthorize("hasPermission(#geneticProfileId, 'GeneticProfile', 'read')")
    public List<DiscreteCopyNumberData> fetchDiscreteCopyNumbersInGeneticProfile(String geneticProfileId,
                                                                                 List<String> sampleIds,
                                                                                 List<Integer> entrezGeneIds,
                                                                                 List<Integer> alterations,
                                                                                 String projection)
        throws GeneticProfileNotFoundException {

        validateGeneticProfile(geneticProfileId);
        if (isHomdelOrAmpOnly(alterations)) {
            return discreteCopyNumberRepository.fetchDiscreteCopyNumbersInGeneticProfile(geneticProfileId, sampleIds,
                entrezGeneIds, alterations, projection);
        }

        return geneticDataService.fetchGeneticData(geneticProfileId, sampleIds, entrezGeneIds, projection).stream()
            .filter(g -> isValidAlteration(alterations, g)).map(this::convert).collect(Collectors.toList());
    }

    @Override
    @PreAuthorize("hasPermission(#geneticProfileId, 'GeneticProfile', 'read')")
    public BaseMeta fetchMetaDiscreteCopyNumbersInGeneticProfile(String geneticProfileId,
                                                                 List<String> sampleIds,
                                                                 List<Integer> entrezGeneIds,
                                                                 List<Integer> alterations)
        throws GeneticProfileNotFoundException {

        validateGeneticProfile(geneticProfileId);
        if (isHomdelOrAmpOnly(alterations)) {
            return discreteCopyNumberRepository.fetchMetaDiscreteCopyNumbersInGeneticProfile(geneticProfileId,
                sampleIds, entrezGeneIds, alterations);
        }

        long totalCount = geneticDataService.fetchGeneticData(geneticProfileId, sampleIds, entrezGeneIds, "ID").stream()
            .filter(g -> isValidAlteration(alterations, g)).count();

        BaseMeta baseMeta = new BaseMeta();
        baseMeta.setTotalCount(Math.toIntExact(totalCount));

        return baseMeta;
    }

    @Override
    @PreAuthorize("hasPermission(#geneticProfileId, 'GeneticProfile', 'read')")
    public List<DiscreteCopyNumberSampleCountByGene> getSampleCountByGeneAndAlterationAndSampleListId(
        String geneticProfileId,
        String sampleListId,
        List<Integer> entrezGeneIds,
        List<Integer> alterations) {

        return discreteCopyNumberRepository.getSampleCountByGeneAndAlterationAndSampleListId(geneticProfileId,
            sampleListId, entrezGeneIds, alterations);
    }

    @Override
    @PreAuthorize("hasPermission(#geneticProfileId, 'GeneticProfile', 'read')")
    public List<DiscreteCopyNumberSampleCountByGene> getSampleCountByGeneAndAlterationAndSampleIds(
        String geneticProfileId,
        List<String> sampleIds,
        List<Integer> entrezGeneIds,
        List<Integer> alterations) {

        return discreteCopyNumberRepository.getSampleCountByGeneAndAlterationAndSampleIds(geneticProfileId, sampleIds,
            entrezGeneIds, alterations);
    }

    private DiscreteCopyNumberData convert(GeneticData geneticData) {

        DiscreteCopyNumberData discreteCopyNumberData = new DiscreteCopyNumberData();
        discreteCopyNumberData.setGeneticProfileId(geneticData.getGeneticProfileId());
        discreteCopyNumberData.setSampleId(geneticData.getSampleId());
        discreteCopyNumberData.setEntrezGeneId(geneticData.getEntrezGeneId());
        discreteCopyNumberData.setGene(geneticData.getGene());
        discreteCopyNumberData.setAlteration(Integer.parseInt(geneticData.getValue()));

        return discreteCopyNumberData;
    }

    private boolean isHomdelOrAmpOnly(List<Integer> alterations) {

        return !alterations.contains(-1) && !alterations.contains(0) && !alterations.contains(1);
    }

    private boolean isValidAlteration(List<Integer> alterations, GeneticData geneticData) {

        boolean result;
        try {
            result = alterations.contains(Integer.parseInt(geneticData.getValue()));
        } catch (NumberFormatException ex) {
            result = false;
        }
        return result;
    }

    private void validateGeneticProfile(String geneticProfileId) throws GeneticProfileNotFoundException {

        GeneticProfile geneticProfile = geneticProfileService.getGeneticProfile(geneticProfileId);

        if (!geneticProfile.getGeneticAlterationType()
            .equals(GeneticProfile.GeneticAlterationType.COPY_NUMBER_ALTERATION) ||
            !geneticProfile.getDatatype().equals("DISCRETE")) {

            throw new GeneticProfileNotFoundException(geneticProfileId);
        }
    }
}
