package org.cbioportal.infrastructure.repository.clickhouse.mutation;

import org.cbioportal.legacy.model.Mutation;
import org.cbioportal.legacy.model.meta.MutationMeta;

import java.util.List;

public interface ClickhouseMutationMapper {

    List<Mutation> getMutationsInMultipleMolecularProfiles(
            List<String> molecularProfileIds,
            List<String> sampleIds,
            List<Integer> entrezGeneIds,
            boolean snpOnly, // Currently hardcoded to false due to how the legacy worked 
            String projection,
            Integer limit,
            Integer offset,
            String sortBy,
            String direction);
    
    List<Mutation> getSummaryMutationsInMultipleMolecularProfiles(
            List<String> molecularProfileIds,
            List<String> sampleIds,
            List<Integer> entrezGeneIds,
            boolean snpOnly, // Currently hardcoded to false due to how the legacy worked 
            String projection,
            Integer limit,
            Integer offset,
            String sortBy,
            String direction);
    
    List<Mutation> getDetailedMutationsInMultipleMolecularProfiles(
            List<String> molecularProfileIds,
            List<String> sampleIds,
            List<Integer> entrezGeneIds,
            boolean snpOnly, // Currently hardcoded to false due to how the legacy worked 
            String projection,
            Integer limit,
            Integer offset,
            String sortBy,
            String direction);

    MutationMeta getMetaMutationsInMultipleMolecularProfiles(List<String> molecularProfileIds, 
                                                                    List<String> sampleIds, 
                                                                    List<Integer> entrezGeneIds, 
                                                                    boolean snpOnly // Currently hardcoded to false due to how the legacy worked 
                                                                    );
}
