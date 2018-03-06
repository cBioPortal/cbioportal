package org.cbioportal.service.impl;

import org.cbioportal.model.*;
import org.cbioportal.model.meta.MutationMeta;
import org.cbioportal.persistence.GeneRepository;
import org.cbioportal.persistence.MutationRepository;
import org.cbioportal.persistence.SampleListRepository;
import org.cbioportal.persistence.mybatis.util.OffsetCalculator;
import org.cbioportal.service.GenePanelService;
import org.cbioportal.service.MolecularProfileService;
import org.cbioportal.service.MutationService;
import org.cbioportal.service.exception.MolecularProfileNotFoundException;
import org.cbioportal.service.util.ChromosomeCalculator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@Service
public class MutationServiceImpl implements MutationService {

    @Autowired
    private MutationRepository mutationRepository;
    @Autowired
    private MolecularProfileService molecularProfileService;
    @Autowired
    private ChromosomeCalculator chromosomeCalculator;
    @Autowired
    private GenePanelService genePanelService;
    @Autowired
    private SampleListRepository sampleListRepository;
    @Autowired
    private GeneRepository geneRepository;
    @Autowired
    private OffsetCalculator offsetCalculator;

    @Override
    public List<Mutation> getMutationsInMolecularProfileBySampleListId(String molecularProfileId, String sampleListId,
                                                                       List<Integer> entrezGeneIds, Boolean snpOnly,
                                                                       Boolean includeNonMutated, String projection, 
                                                                       Integer pageSize, Integer pageNumber, 
                                                                       String sortBy, String direction)
        throws MolecularProfileNotFoundException {

        validateMolecularProfile(molecularProfileId);

        List<Mutation> mutationList = mutationRepository.getMutationsInMolecularProfileBySampleListId(molecularProfileId,
            sampleListId, entrezGeneIds, snpOnly, projection, pageSize, pageNumber, sortBy, direction);

        List<String> sampleIds = sampleListRepository.getAllSampleIdsInSampleList(sampleListId);
        List<String> molecularProfileIds = new ArrayList<>();
        sampleIds.forEach(s -> molecularProfileIds.add(molecularProfileId));

        return createMutationData(mutationList, molecularProfileIds, sampleIds, entrezGeneIds, includeNonMutated, 
            projection, pageSize, pageNumber);
    }

    @Override
    public MutationMeta getMetaMutationsInMolecularProfileBySampleListId(String molecularProfileId, 
                                                                         String sampleListId, 
                                                                         List<Integer> entrezGeneIds,
                                                                         Boolean includeNonMutated)
        throws MolecularProfileNotFoundException {

        validateMolecularProfile(molecularProfileId);

        if (includeNonMutated != null && includeNonMutated) {
            MutationMeta mutationMeta = new MutationMeta();
            mutationMeta.setTotalCount(getMutationsInMolecularProfileBySampleListId(molecularProfileId, sampleListId, 
                entrezGeneIds, false, true, "ID", null, null, null, null).size());
            return mutationMeta;
        }

        return mutationRepository.getMetaMutationsInMolecularProfileBySampleListId(molecularProfileId, sampleListId,
            entrezGeneIds);
    }

    @Override
    public List<Mutation> getMutationsInMultipleMolecularProfiles(List<String> molecularProfileIds, 
                                                                  List<String> sampleIds, List<Integer> entrezGeneIds, 
                                                                  Boolean includeNonMutated, String projection, 
                                                                  Integer pageSize, Integer pageNumber, String sortBy, 
                                                                  String direction) {

        List<Mutation> mutationList = mutationRepository.getMutationsInMultipleMolecularProfiles(molecularProfileIds,
            sampleIds, entrezGeneIds, projection, pageSize, pageNumber, sortBy, direction);

        return createMutationData(mutationList, molecularProfileIds, sampleIds, entrezGeneIds, includeNonMutated, 
            projection, pageSize, pageNumber);
    }

    @Override
    public MutationMeta getMetaMutationsInMultipleMolecularProfiles(List<String> molecularProfileIds,
                                                                    List<String> sampleIds, 
                                                                    List<Integer> entrezGeneIds, 
                                                                    Boolean includeNonMutated) {

        if (includeNonMutated != null && includeNonMutated) {
            MutationMeta mutationMeta = new MutationMeta();
            mutationMeta.setTotalCount(getMutationsInMultipleMolecularProfiles(molecularProfileIds, sampleIds, 
                entrezGeneIds, true, "ID", null, null, null, null).size());
            return mutationMeta;
        }

        return mutationRepository.getMetaMutationsInMultipleMolecularProfiles(molecularProfileIds, sampleIds,
            entrezGeneIds);
    }

    @Override
    public List<Mutation> fetchMutationsInMolecularProfile(String molecularProfileId, List<String> sampleIds,
                                                           List<Integer> entrezGeneIds, Boolean snpOnly,
                                                           Boolean includeNonMutated, String projection, 
                                                           Integer pageSize, Integer pageNumber, String sortBy, 
                                                           String direction)
        throws MolecularProfileNotFoundException {

        validateMolecularProfile(molecularProfileId);

        List<Mutation> mutationList = mutationRepository.fetchMutationsInMolecularProfile(molecularProfileId, sampleIds,
            entrezGeneIds, snpOnly, projection, pageSize, pageNumber, sortBy, direction);

        List<String> molecularProfileIds = new ArrayList<>();
        sampleIds.forEach(s -> molecularProfileIds.add(molecularProfileId));

        return createMutationData(mutationList, molecularProfileIds, sampleIds, entrezGeneIds, includeNonMutated, 
            projection, pageSize, pageNumber);
    }

    @Override
    public MutationMeta fetchMetaMutationsInMolecularProfile(String molecularProfileId, List<String> sampleIds,
                                                             List<Integer> entrezGeneIds, Boolean includeNonMutated)
        throws MolecularProfileNotFoundException {

        validateMolecularProfile(molecularProfileId);

        if (includeNonMutated != null && includeNonMutated) {
            MutationMeta mutationMeta = new MutationMeta();
            mutationMeta.setTotalCount(fetchMutationsInMolecularProfile(molecularProfileId, sampleIds, 
                entrezGeneIds, false, true, "ID", null, null, null, null).size());
            return mutationMeta;
        }

        return mutationRepository.fetchMetaMutationsInMolecularProfile(molecularProfileId, sampleIds, entrezGeneIds);
    }

    @Override
    public List<MutationCountByGene> getSampleCountByEntrezGeneIdsAndSampleIds(String molecularProfileId,
                                                                               List<String> sampleIds,
                                                                               List<Integer> entrezGeneIds)
        throws MolecularProfileNotFoundException {

        validateMolecularProfile(molecularProfileId);

        return mutationRepository.getSampleCountByEntrezGeneIdsAndSampleIds(molecularProfileId, sampleIds, 
            entrezGeneIds);
    }

    @Override
    public List<MutationCountByGene> getPatientCountByEntrezGeneIdsAndSampleIds(String molecularProfileId,
                                                                                List<String> patientIds,
                                                                                List<Integer> entrezGeneIds)
        throws MolecularProfileNotFoundException {

        validateMolecularProfile(molecularProfileId);

        return mutationRepository.getPatientCountByEntrezGeneIdsAndSampleIds(molecularProfileId, patientIds, 
            entrezGeneIds);
    }

    @Override
    public List<MutationCount> getMutationCountsInMolecularProfileBySampleListId(String molecularProfileId,
                                                                                 String sampleListId)
        throws MolecularProfileNotFoundException {

        validateMolecularProfile(molecularProfileId);

        return mutationRepository.getMutationCountsInMolecularProfileBySampleListId(molecularProfileId, sampleListId);
    }

    @Override
    public List<MutationCount> fetchMutationCountsInMolecularProfile(String molecularProfileId, List<String> sampleIds)
        throws MolecularProfileNotFoundException {

        validateMolecularProfile(molecularProfileId);

        return mutationRepository.fetchMutationCountsInMolecularProfile(molecularProfileId, sampleIds);
    }

    @Override
    public List<MutationCountByPosition> fetchMutationCountsByPosition(List<Integer> entrezGeneIds,
                                                                       List<Integer> proteinPosStarts,
                                                                       List<Integer> proteinPosEnds) {

        List<MutationCountByPosition> mutationCountByPositionList = new ArrayList<>();
        for (int i = 0; i < entrezGeneIds.size(); i++) {

            MutationCountByPosition mutationCountByPosition = mutationRepository.getMutationCountByPosition(
                entrezGeneIds.get(i), proteinPosStarts.get(i), proteinPosEnds.get(i));
            mutationCountByPositionList.add(mutationCountByPosition);
        }

        return mutationCountByPositionList;
    }

    private void validateMolecularProfile(String molecularProfileId) throws MolecularProfileNotFoundException {

        MolecularProfile molecularProfile = molecularProfileService.getMolecularProfile(molecularProfileId);

        if (!molecularProfile.getMolecularAlterationType()
            .equals(MolecularProfile.MolecularAlterationType.MUTATION_EXTENDED)) {

            throw new MolecularProfileNotFoundException(molecularProfileId);
        }
    }

    private List<Mutation> createMutationData(List<Mutation> mutationList, List<String> molecularProfileIds, 
        List<String> sampleIds, List<Integer> entrezGeneIds, Boolean includeNonMutated, String projection, 
        Integer pageSize, Integer pageNumber) {

        Set<Integer> mutationKeys = new HashSet<>();

        mutationList.forEach(mutation -> {
            chromosomeCalculator.setChromosome(mutation.getGene());
            mutation.setSequenced(true);
            mutation.setWildType(false);
        });

        if (includeNonMutated != null && includeNonMutated && entrezGeneIds != null) {

            mutationList.forEach(mutation -> {
                mutationKeys.add(Objects.hash(mutation.getSampleId(), mutation.getMolecularProfileId(), 
                    mutation.getEntrezGeneId()));
            });

            List<GenePanelData> genePanelDataList = genePanelService.fetchGenePanelDataInMultipleMolecularProfiles(
                molecularProfileIds, sampleIds, entrezGeneIds);

            genePanelDataList.forEach(genePanelData -> {
                if (!genePanelData.getSequenced()) {
                    Mutation mutation = createMutationFromGenePanelData(genePanelData, projection);
                    mutation.setSequenced(false);
                    mutation.setWildType(false);
                    mutationList.add(mutation);
                } else if (!mutationKeys.contains(Objects.hash(genePanelData.getSampleId(), 
                    genePanelData.getMolecularProfileId(), genePanelData.getEntrezGeneId()))) {
                    Mutation mutation = createMutationFromGenePanelData(genePanelData, projection);
                    mutation.setWildType(true);
                    mutation.setSequenced(true);
                    mutationList.add(mutation);
                }
            });

            Integer offset = offsetCalculator.calculate(pageSize, pageNumber);
            if (offset != null && offset + pageSize <= mutationList.size()) {
                return mutationList.subList(offset, offset + pageSize);
            }
        }
        
        return mutationList;
    }

    private Mutation createMutationFromGenePanelData(GenePanelData genePanelData, String projection) {

        Mutation mutation = new Mutation();
        mutation.setStudyId(genePanelData.getStudyId());
        mutation.setMolecularProfileId(genePanelData.getMolecularProfileId());
        mutation.setSampleId(genePanelData.getSampleId());
        mutation.setPatientId(genePanelData.getPatientId());
        mutation.setEntrezGeneId(genePanelData.getEntrezGeneId());
        if (projection == "DETAILED") {
            mutation.setGene(geneRepository.getGeneByEntrezGeneId(mutation.getEntrezGeneId()));
        }
        return mutation;
    }
}
