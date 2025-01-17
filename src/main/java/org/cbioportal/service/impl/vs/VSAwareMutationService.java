package org.cbioportal.service.impl.vs;

import org.cbioportal.model.GeneFilterQuery;
import org.cbioportal.model.GenomicDataCountItem;
import org.cbioportal.model.Mutation;
import org.cbioportal.model.MutationCountByPosition;
import org.cbioportal.model.meta.MutationMeta;
import org.cbioportal.service.MutationService;
import org.cbioportal.service.exception.MolecularProfileNotFoundException;

import java.util.List;

public class VSAwareMutationService implements MutationService {

    private final MutationService mutationService;
    private final PublishedVirtualStudyService publishedVirtualStudyService;

    public VSAwareMutationService(MutationService mutationService, PublishedVirtualStudyService publishedVirtualStudyService) {
        this.mutationService = mutationService;
        this.publishedVirtualStudyService = publishedVirtualStudyService;
    }
    
    @Override
    public List<Mutation> getMutationsInMolecularProfileBySampleListId(String molecularProfileId, String sampleListId, List<Integer> entrezGeneIds, boolean snpOnly, String projection, Integer pageSize, Integer pageNumber, String sortBy, String direction) throws MolecularProfileNotFoundException {
        return List.of();
    }

    @Override
    public MutationMeta getMetaMutationsInMolecularProfileBySampleListId(String molecularProfileId, String sampleListId, List<Integer> entrezGeneIds) throws MolecularProfileNotFoundException {
        return null;
    }

    @Override
    public List<Mutation> getMutationsInMultipleMolecularProfiles(List<String> molecularProfileIds, List<String> sampleIds, List<Integer> entrezGeneIds, String projection, Integer pageSize, Integer pageNumber, String sortBy, String direction) {
        return List.of();
    }

    @Override
    public List<Mutation> getMutationsInMultipleMolecularProfilesByGeneQueries(List<String> molecularProfileIds, List<String> sampleIds, List<GeneFilterQuery> geneQueries, String projection, Integer pageSize, Integer pageNumber, String sortBy, String direction) {
        return List.of();
    }

    @Override
    public MutationMeta getMetaMutationsInMultipleMolecularProfiles(List<String> molecularProfileIds, List<String> sampleIds, List<Integer> entrezGeneIds) {
        return null;
    }

    @Override
    public List<Mutation> fetchMutationsInMolecularProfile(String molecularProfileId, List<String> sampleIds, List<Integer> entrezGeneIds, boolean snpOnly, String projection, Integer pageSize, Integer pageNumber, String sortBy, String direction) throws MolecularProfileNotFoundException {
        return List.of();
    }

    @Override
    public MutationMeta fetchMetaMutationsInMolecularProfile(String molecularProfileId, List<String> sampleIds, List<Integer> entrezGeneIds) throws MolecularProfileNotFoundException {
        return null;
    }

    @Override
    public List<MutationCountByPosition> fetchMutationCountsByPosition(List<Integer> entrezGeneIds, List<Integer> proteinPosStarts, List<Integer> proteinPosEnds) {
        return List.of();
    }

    @Override
    public GenomicDataCountItem getMutationCountsByType(List<String> molecularProfileIds, List<String> sampleIds, List<Integer> entrezGeneIds, String profileType) {
        return null;
    }
}
