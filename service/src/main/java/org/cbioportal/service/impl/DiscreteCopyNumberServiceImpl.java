package org.cbioportal.service.impl;

import org.cbioportal.model.CopyNumberSampleCountByGene;
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
    public List<DiscreteCopyNumberData> getDiscreteCopyNumbersInGeneticProfile(String geneticProfileId, String sampleId,
                                                                               List<Integer> alterations,
                                                                               String projection)
        throws GeneticProfileNotFoundException {

        validateGeneticProfile(geneticProfileId);
        if (isHomdelOrAmpOnly(alterations)) {

            return discreteCopyNumberRepository.getDiscreteCopyNumbersInGeneticProfile(geneticProfileId, sampleId,
                alterations, projection);
        }

        return geneticDataService.getGeneticData(geneticProfileId, sampleId, null, projection).stream()
            .filter(g -> isValidAlteration(alterations, g)).map(this::convert)
            .collect(Collectors.toList());
    }

    @Override
    @PreAuthorize("hasPermission(#geneticProfileId, 'GeneticProfile', 'read')")
    public BaseMeta getMetaDiscreteCopyNumbersInGeneticProfile(String geneticProfileId, String sampleId,
                                                               List<Integer> alterations)
        throws GeneticProfileNotFoundException {

        validateGeneticProfile(geneticProfileId);
        if (isHomdelOrAmpOnly(alterations)) {
            return discreteCopyNumberRepository.getMetaDiscreteCopyNumbersInGeneticProfile(geneticProfileId, sampleId,
                alterations);
        }

        long totalCount = geneticDataService.getGeneticData(geneticProfileId, sampleId, null, "ID").stream()
            .filter(g -> isValidAlteration(alterations, g)).count();

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
            .filter(g -> isValidAlteration(alterations, g)).map(this::convert)
            .collect(Collectors.toList());
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
    public List<CopyNumberSampleCountByGene> getSampleCountByGeneAndAlteration(String geneticProfileId,
                                                                               List<Integer> entrezGeneIds,
                                                                               List<Integer> alterations) {

        return discreteCopyNumberRepository.getSampleCountByGeneAndAlteration(geneticProfileId, entrezGeneIds,
            alterations);
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
