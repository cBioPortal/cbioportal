package org.cbioportal.service.impl;

import org.cbioportal.model.CopyNumberCount;
import org.cbioportal.model.CopyNumberCountByGene;
import org.cbioportal.model.DiscreteCopyNumberData;
import org.cbioportal.model.GeneMolecularData;
import org.cbioportal.model.MolecularProfile;
import org.cbioportal.model.meta.BaseMeta;
import org.cbioportal.persistence.DiscreteCopyNumberRepository;
import org.cbioportal.service.DiscreteCopyNumberService;
import org.cbioportal.service.MolecularDataService;
import org.cbioportal.service.MolecularProfileService;
import org.cbioportal.service.exception.MolecularProfileNotFoundException;
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
    private MolecularDataService molecularDataService;
    @Autowired
    private MolecularProfileService molecularProfileService;

    @Override
    @PreAuthorize("hasPermission(#molecularProfileId, 'MolecularProfile', 'read')")
    public List<DiscreteCopyNumberData> getDiscreteCopyNumbersInMolecularProfileBySampleListId(
        String molecularProfileId,
        String sampleListId,
        List<Integer> entrezGeneIds,
        List<Integer> alterationTypes,
        String projection)
        throws MolecularProfileNotFoundException {

        validateMolecularProfile(molecularProfileId);
        if (isHomdelOrAmpOnly(alterationTypes)) {

            return discreteCopyNumberRepository.getDiscreteCopyNumbersInMolecularProfileBySampleListId(molecularProfileId,
                sampleListId, entrezGeneIds, alterationTypes, projection);
        }

        return molecularDataService.getMolecularData(molecularProfileId, sampleListId, entrezGeneIds, projection).stream()
            .filter(g -> isValidAlteration(alterationTypes, g)).map(this::convert).collect(Collectors.toList());
    }

    @Override
    @PreAuthorize("hasPermission(#molecularProfileId, 'MolecularProfile', 'read')")
    public BaseMeta getMetaDiscreteCopyNumbersInMolecularProfileBySampleListId(String molecularProfileId,
                                                                               String sampleListId,
                                                                               List<Integer> entrezGeneIds,
                                                                               List<Integer> alterationTypes)
        throws MolecularProfileNotFoundException {

        validateMolecularProfile(molecularProfileId);
        if (isHomdelOrAmpOnly(alterationTypes)) {
            return discreteCopyNumberRepository.getMetaDiscreteCopyNumbersInMolecularProfileBySampleListId(
                molecularProfileId, sampleListId, entrezGeneIds, alterationTypes);
        }

        long totalCount = molecularDataService.getMolecularData(molecularProfileId, sampleListId, entrezGeneIds, "ID")
            .stream().filter(g -> isValidAlteration(alterationTypes, g)).count();

        BaseMeta baseMeta = new BaseMeta();
        baseMeta.setTotalCount(Math.toIntExact(totalCount));

        return baseMeta;
    }

    @Override
    @PreAuthorize("hasPermission(#molecularProfileId, 'MolecularProfile', 'read')")
    public List<DiscreteCopyNumberData> fetchDiscreteCopyNumbersInMolecularProfile(String molecularProfileId,
                                                                                   List<String> sampleIds,
                                                                                   List<Integer> entrezGeneIds,
                                                                                   List<Integer> alterationTypes,
                                                                                   String projection)
        throws MolecularProfileNotFoundException {

        validateMolecularProfile(molecularProfileId);
        if (isHomdelOrAmpOnly(alterationTypes)) {
            return discreteCopyNumberRepository.fetchDiscreteCopyNumbersInMolecularProfile(molecularProfileId, 
                sampleIds, entrezGeneIds, alterationTypes, projection);
        }

        return molecularDataService.fetchMolecularData(molecularProfileId, sampleIds, entrezGeneIds, projection)
            .stream().filter(g -> isValidAlteration(alterationTypes, g))
            .map(this::convert).collect(Collectors.toList());
    }

    @Override
    @PreAuthorize("hasPermission(#molecularProfileId, 'MolecularProfile', 'read')")
    public BaseMeta fetchMetaDiscreteCopyNumbersInMolecularProfile(String molecularProfileId,
                                                                   List<String> sampleIds,
                                                                   List<Integer> entrezGeneIds,
                                                                   List<Integer> alterationTypes)
        throws MolecularProfileNotFoundException {

        validateMolecularProfile(molecularProfileId);
        if (isHomdelOrAmpOnly(alterationTypes)) {
            return discreteCopyNumberRepository.fetchMetaDiscreteCopyNumbersInMolecularProfile(molecularProfileId,
                sampleIds, entrezGeneIds, alterationTypes);
        }

        long totalCount = molecularDataService.fetchMolecularData(molecularProfileId, sampleIds, entrezGeneIds, "ID")
            .stream().filter(g -> isValidAlteration(alterationTypes, g)).count();

        BaseMeta baseMeta = new BaseMeta();
        baseMeta.setTotalCount(Math.toIntExact(totalCount));

        return baseMeta;
    }

    @Override
    @PreAuthorize("hasPermission(#molecularProfileId, 'MolecularProfile', 'read')")
    public List<CopyNumberCountByGene> getSampleCountByGeneAndAlterationAndSampleIds(
        String molecularProfileId,
        List<String> sampleIds,
        List<Integer> entrezGeneIds,
        List<Integer> alterations) {

        return discreteCopyNumberRepository.getSampleCountByGeneAndAlterationAndSampleIds(molecularProfileId, sampleIds, 
            entrezGeneIds, alterations);
    }

    @Override
    public List<CopyNumberCountByGene> getPatientCountByGeneAndAlterationAndPatientIds(String molecularProfileId, 
                                                                                       List<String> patientIds, 
                                                                                       List<Integer> entrezGeneIds, 
                                                                                       List<Integer> alterations) {

        return discreteCopyNumberRepository.getPatientCountByGeneAndAlterationAndPatientIds(molecularProfileId, 
            patientIds, entrezGeneIds, alterations);
    }


    @Override
    @PreAuthorize("hasPermission(#molecularProfileId, 'MolecularProfile', 'read')")
    public List<CopyNumberCount> fetchCopyNumberCounts(String molecularProfileId, List<Integer> entrezGeneIds,
                                                       List<Integer> alterations)
        throws MolecularProfileNotFoundException {

        validateMolecularProfile(molecularProfileId);

        Integer numberOfSamplesInMolecularProfile = molecularDataService.getNumberOfSamplesInMolecularProfile(
            molecularProfileId);
        List<CopyNumberCountByGene> copyNumberSampleCountByGeneList =
            getSampleCountByGeneAndAlterationAndSampleIds(molecularProfileId, null, entrezGeneIds, alterations);

        List<CopyNumberCount> copyNumberCounts = new ArrayList<>();
        for (int i = 0; i < alterations.size(); i++) {
            Integer alteration = alterations.get(i);
            Integer entrezGeneId = entrezGeneIds.get(i);

            CopyNumberCount copyNumberCount = new CopyNumberCount();
            copyNumberCount.setMolecularProfileId(molecularProfileId);
            copyNumberCount.setEntrezGeneId(entrezGeneId);
            copyNumberCount.setAlteration(alteration);
            copyNumberCount.setNumberOfSamples(numberOfSamplesInMolecularProfile);

            Optional<CopyNumberCountByGene> copyNumberSampleCountByGene = copyNumberSampleCountByGeneList.stream()
                .filter(p -> p.getEntrezGeneId().equals(entrezGeneId) && p.getAlteration().equals(alteration))
                .findFirst();
            copyNumberSampleCountByGene.ifPresent(m -> copyNumberCount.setNumberOfSamplesWithAlterationInGene(m
                .getCount()));

            copyNumberCounts.add(copyNumberCount);
        }

        return copyNumberCounts;
    }

    private DiscreteCopyNumberData convert(GeneMolecularData molecularData) {

        DiscreteCopyNumberData discreteCopyNumberData = new DiscreteCopyNumberData();
        discreteCopyNumberData.setMolecularProfileId(molecularData.getMolecularProfileId());
        discreteCopyNumberData.setSampleId(molecularData.getSampleId());
        discreteCopyNumberData.setEntrezGeneId(molecularData.getEntrezGeneId());
        discreteCopyNumberData.setGene(molecularData.getGene());
        discreteCopyNumberData.setAlteration(Integer.parseInt(molecularData.getValue()));

        return discreteCopyNumberData;
    }

    private boolean isHomdelOrAmpOnly(List<Integer> alterationTypes) {

        return !alterationTypes.contains(-1) && !alterationTypes.contains(0) && !alterationTypes.contains(1);
    }

    private boolean isValidAlteration(List<Integer> alterationTypes, GeneMolecularData molecularData) {

        boolean result;
        try {
            result = alterationTypes.contains(Integer.parseInt(molecularData.getValue()));
        } catch (NumberFormatException ex) {
            result = false;
        }
        return result;
    }

    private void validateMolecularProfile(String molecularProfileId) throws MolecularProfileNotFoundException {

        MolecularProfile molecularProfile = molecularProfileService.getMolecularProfile(molecularProfileId);

        if (!molecularProfile.getMolecularAlterationType()
            .equals(MolecularProfile.MolecularAlterationType.COPY_NUMBER_ALTERATION) ||
            !molecularProfile.getDatatype().equals("DISCRETE")) {

            throw new MolecularProfileNotFoundException(molecularProfileId);
        }
    }
}
