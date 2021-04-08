package org.cbioportal.persistence.mybatis;

import org.cbioportal.model.Mutation;
import org.cbioportal.model.MutationCountByPosition;
import org.cbioportal.model.MutationCountByGene;
import org.cbioportal.model.meta.MutationMeta;
import org.cbioportal.persistence.MutationRepository;
import org.cbioportal.persistence.mybatis.util.OffsetCalculator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.stream.Collectors;

@Repository
public class MutationMyBatisRepository implements MutationRepository {

    @Autowired
    private MutationMapper mutationMapper;
    @Autowired
    private OffsetCalculator offsetCalculator;

    @Override
    public List<Mutation> getMutationsInMolecularProfileBySampleListId(String molecularProfileId, String sampleListId,
                                                                       List<Integer> entrezGeneIds, Boolean snpOnly,
                                                                       String projection, Integer pageSize,
                                                                       Integer pageNumber, String sortBy,
                                                                       String direction) {

        return mutationMapper.getMutationsBySampleListId(molecularProfileId, sampleListId, entrezGeneIds, snpOnly,
            projection, pageSize, offsetCalculator.calculate(pageSize, pageNumber), sortBy, direction);
    }

    @Override
    public MutationMeta getMetaMutationsInMolecularProfileBySampleListId(String molecularProfileId, String sampleListId,
                                                                         List<Integer> entrezGeneIds) {

        return mutationMapper.getMetaMutationsBySampleListId(molecularProfileId, sampleListId, entrezGeneIds, null);
    }

    @Override
    public List<Mutation> getMutationsInMultipleMolecularProfiles(List<String> molecularProfileIds, 
                                                                  List<String> sampleIds, List<Integer> entrezGeneIds, 
                                                                  String projection, Integer pageSize, 
                                                                  Integer pageNumber, String sortBy, String direction) {

        return getGroupedCasesByMolecularProfileId(molecularProfileIds, sampleIds)
            .entrySet()
            .stream()
            .flatMap(entry -> mutationMapper.getMutationsBySampleIds(entry.getKey(),
                entry.getValue(),
                entrezGeneIds,
                null,
                projection,
                pageSize,
                offsetCalculator.calculate(pageSize, pageNumber),
                sortBy,
                direction).stream())
            .collect(Collectors.toList());
    }

    private Map<String,Set<String>> getGroupedCasesByMolecularProfileId(List<String> molecularProfileIds, List<String> caseIds) {
        Map<String,Set<String>> groupMolecularProfileSamples = new HashMap<>();

        for(int i = 0; i< molecularProfileIds.size(); i++) {
            String molecularProfileId = molecularProfileIds.get(i);
            String caseId = caseIds.get(i);
            if(!groupMolecularProfileSamples.containsKey(molecularProfileId)) {
                Set<String> filteredCaseIds = new HashSet<>();
                filteredCaseIds.add(caseId);
                groupMolecularProfileSamples.put(molecularProfileId,filteredCaseIds);
            } else {
                groupMolecularProfileSamples.get(molecularProfileId).add(caseId);
            }
        }
        return groupMolecularProfileSamples;
    }

    @Override
    public MutationMeta getMetaMutationsInMultipleMolecularProfiles(List<String> molecularProfileIds,
                                                                    List<String> sampleIds, 
                                                                    List<Integer> entrezGeneIds) {

        return mutationMapper.getMetaMutationsInMultipleMolecularProfiles(molecularProfileIds, sampleIds, entrezGeneIds,
            null);
    }

    @Override
    public List<Mutation> fetchMutationsInMolecularProfile(String molecularProfileId, List<String> sampleIds,
                                                           List<Integer> entrezGeneIds, Boolean snpOnly,
                                                           String projection, Integer pageSize, Integer pageNumber,
                                                           String sortBy, String direction) {

        return mutationMapper.getMutationsBySampleIds(molecularProfileId, new HashSet<>(sampleIds), entrezGeneIds, snpOnly, projection,
            pageSize, offsetCalculator.calculate(pageSize, pageNumber), sortBy, direction);
    }

    @Override
    public MutationMeta fetchMetaMutationsInMolecularProfile(String molecularProfileId, List<String> sampleIds,
                                                             List<Integer> entrezGeneIds) {

        return mutationMapper.getMetaMutationsBySampleIds(molecularProfileId, sampleIds, entrezGeneIds, null);
    }

    @Override
    public MutationCountByPosition getMutationCountByPosition(Integer entrezGeneId, Integer proteinPosStart,
                                                              Integer proteinPosEnd) {

        return mutationMapper.getMutationCountByPosition(entrezGeneId, proteinPosStart, proteinPosEnd);
    }

    // TODO: cleanup once fusion/structural data is fixed in database
    @Override
    public List<Mutation> getFusionsInMultipleMolecularProfiles(List<String> molecularProfileIds,
            List<String> sampleIds, List<Integer> entrezGeneIds, String projection, Integer pageSize,
            Integer pageNumber, String sortBy, String direction) {

        return getGroupedCasesByMolecularProfileId(molecularProfileIds, sampleIds)
            .entrySet()
            .stream()
            .flatMap(entry -> mutationMapper.getFusionsInMultipleMolecularProfiles(Arrays.asList(entry.getKey()),
                new ArrayList<>(entry.getValue()),
                entrezGeneIds,
                null,
                projection,
                pageSize,
                offsetCalculator.calculate(pageSize, pageNumber),
                sortBy,
                direction).stream())
            .collect(Collectors.toList());
    }
    // TODO: cleanup once fusion/structural data is fixed in database

}
