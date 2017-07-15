package org.cbioportal.service.impl;

import org.cbioportal.model.CopyNumberCount;
import org.cbioportal.model.CopyNumberSampleCountByGene;
import org.cbioportal.model.DiscreteCopyNumberData;
import org.cbioportal.model.GeneGeneticData;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
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
        List<Integer> alterationTypes,
        String projection)
        throws GeneticProfileNotFoundException {

        validateGeneticProfile(geneticProfileId);
        if (isHomdelOrAmpOnly(alterationTypes)) {

            return discreteCopyNumberRepository.getDiscreteCopyNumbersInGeneticProfileBySampleListId(geneticProfileId,
                sampleListId, entrezGeneIds, alterationTypes, projection);
        }

        return geneticDataService.getGeneticData(geneticProfileId, sampleListId, entrezGeneIds, projection).stream()
            .filter(g -> isValidAlteration(alterationTypes, g)).map(this::convert).collect(Collectors.toList());
    }

    @Override
    @PreAuthorize("hasPermission(#geneticProfileId, 'GeneticProfile', 'read')")
    public BaseMeta getMetaDiscreteCopyNumbersInGeneticProfileBySampleListId(String geneticProfileId,
                                                                             String sampleListId,
                                                                             List<Integer> entrezGeneIds,
                                                                             List<Integer> alterationTypes)
        throws GeneticProfileNotFoundException {

        validateGeneticProfile(geneticProfileId);
        if (isHomdelOrAmpOnly(alterationTypes)) {
            return discreteCopyNumberRepository.getMetaDiscreteCopyNumbersInGeneticProfileBySampleListId(
                geneticProfileId, sampleListId, entrezGeneIds, alterationTypes);
        }

        long totalCount = geneticDataService.getGeneticData(geneticProfileId, sampleListId, entrezGeneIds, "ID")
            .stream().filter(g -> isValidAlteration(alterationTypes, g)).count();

        BaseMeta baseMeta = new BaseMeta();
        baseMeta.setTotalCount(Math.toIntExact(totalCount));

        return baseMeta;
    }

    @Override
    @PreAuthorize("hasPermission(#geneticProfileId, 'GeneticProfile', 'read')")
    public List<DiscreteCopyNumberData> fetchDiscreteCopyNumbersInGeneticProfile(String geneticProfileId,
                                                                                 List<String> sampleIds,
                                                                                 List<Integer> entrezGeneIds,
                                                                                 List<Integer> alterationTypes,
                                                                                 String projection)
        throws GeneticProfileNotFoundException {

        validateGeneticProfile(geneticProfileId);
        if (isHomdelOrAmpOnly(alterationTypes)) {
            return discreteCopyNumberRepository.fetchDiscreteCopyNumbersInGeneticProfile(geneticProfileId, sampleIds,
                entrezGeneIds, alterationTypes, projection);
        }

        return geneticDataService.fetchGeneticData(geneticProfileId, sampleIds, entrezGeneIds, projection).stream()
            .filter(g -> isValidAlteration(alterationTypes, g)).map(this::convert).collect(Collectors.toList());
    }

    @Override
    @PreAuthorize("hasPermission(#geneticProfileId, 'GeneticProfile', 'read')")
    public BaseMeta fetchMetaDiscreteCopyNumbersInGeneticProfile(String geneticProfileId,
                                                                 List<String> sampleIds,
                                                                 List<Integer> entrezGeneIds,
                                                                 List<Integer> alterationTypes)
        throws GeneticProfileNotFoundException {

        validateGeneticProfile(geneticProfileId);
        if (isHomdelOrAmpOnly(alterationTypes)) {
            return discreteCopyNumberRepository.fetchMetaDiscreteCopyNumbersInGeneticProfile(geneticProfileId,
                sampleIds, entrezGeneIds, alterationTypes);
        }

        long totalCount = geneticDataService.fetchGeneticData(geneticProfileId, sampleIds, entrezGeneIds, "ID").stream()
            .filter(g -> isValidAlteration(alterationTypes, g)).count();

        BaseMeta baseMeta = new BaseMeta();
        baseMeta.setTotalCount(Math.toIntExact(totalCount));

        return baseMeta;
    }

    @Override
    @PreAuthorize("hasPermission(#geneticProfileId, 'GeneticProfile', 'read')")
    public List<CopyNumberSampleCountByGene> getSampleCountByGeneAndAlterationAndSampleListId(
        String geneticProfileId,
        String sampleListId,
        List<Integer> entrezGeneIds,
        List<Integer> alterations) {

        return discreteCopyNumberRepository.getSampleCountByGeneAndAlterationAndSampleListId(geneticProfileId,
            sampleListId, entrezGeneIds, alterations);
    }

    @Override
    @PreAuthorize("hasPermission(#geneticProfileId, 'GeneticProfile', 'read')")
    public List<CopyNumberSampleCountByGene> getSampleCountByGeneAndAlterationAndSampleIds(
        String geneticProfileId,
        List<String> sampleIds,
        List<Integer> entrezGeneIds,
        List<Integer> alterations) {

        return discreteCopyNumberRepository.getSampleCountByGeneAndAlterationAndSampleIds(geneticProfileId, sampleIds,
            entrezGeneIds, alterations);
    }

    @Override
    @PreAuthorize("hasPermission(#geneticProfileId, 'GeneticProfile', 'read')")
    public List<CopyNumberCount> fetchCopyNumberCounts(String geneticProfileId, List<Integer> entrezGeneIds,
                                                       List<Integer> alterations)
        throws GeneticProfileNotFoundException {

        validateGeneticProfile(geneticProfileId);

        Integer numberOfSamplesInGeneticProfile = geneticDataService.getNumberOfSamplesInGeneticProfile(
            geneticProfileId);
        List<CopyNumberSampleCountByGene> copyNumberSampleCountByGeneList = 
            getSampleCountByGeneAndAlterationAndSampleIds(geneticProfileId, null, entrezGeneIds, alterations);

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

    private DiscreteCopyNumberData convert(GeneGeneticData geneticData) {

        DiscreteCopyNumberData discreteCopyNumberData = new DiscreteCopyNumberData();
        discreteCopyNumberData.setGeneticProfileId(geneticData.getGeneticProfileId());
        discreteCopyNumberData.setSampleId(geneticData.getSampleId());
        discreteCopyNumberData.setEntrezGeneId(geneticData.getEntrezGeneId());
        discreteCopyNumberData.setGene(geneticData.getGene());
        discreteCopyNumberData.setAlteration(Integer.parseInt(geneticData.getValue()));

        return discreteCopyNumberData;
    }

    private boolean isHomdelOrAmpOnly(List<Integer> alterationTypes) {

        return !alterationTypes.contains(-1) && !alterationTypes.contains(0) && !alterationTypes.contains(1);
    }

    private boolean isValidAlteration(List<Integer> alterationTypes, GeneGeneticData geneticData) {

        boolean result;
        try {
            result = alterationTypes.contains(Integer.parseInt(geneticData.getValue()));
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
