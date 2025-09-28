package org.cbioportal.infrastructure.repository.clickhouse.mutation;

import org.cbioportal.legacy.model.Mutation;
import org.cbioportal.legacy.model.meta.MutationMeta;

import java.util.List;



/**
 * Mapper interface for retrieving Mutation data from ClickHouse. This interface provides methods to
 * fetch Mutation counts and Mutation data for molecular profile,samples and entrez Gene Ids.
 */
public interface ClickhouseMutationMapper {


    /**
     * Retrieves mutation  with ID projection (minimal data set).
     * 
     * <p> Returns only essential identifiers: molecularProfileId, sampleId, patientId, entrezGeneId and 
     * studyId.
     *
     * @param allMolecularProfileIds list containing molecularProfile 
     * @param allSampleIds list containing sampleIds
     * @param entrezGeneIds list of entrez gene id
     * @param snpOnly 
     * @param projection level of detail for each mutation
     * @param limit 
     * @param offset
     * @param sortBy
     * @param direction
     * @return  list of mutation 
     */
    List<Mutation> getMutationsInMultipleMolecularProfilesId(
            List<String> allMolecularProfileIds,
            List<String> allSampleIds,
            List<Integer> entrezGeneIds,
            boolean snpOnly, // Currently hardcoded to false due to how the legacy worked 
            String projection,
            Integer limit,
            Integer offset,
            String sortBy,
            String direction);

    /**
     *  Retrieves mutation with SUMMARY projection (basic data with values).
     * 
     * <p>Returns basic mutation information, but without detailed mutation metadata.
     *
     * @param allMolecularProfileIds list containing molecularProfile 
     * @param allSampleIds list containing sampleIds
     * @param entrezGeneIds list of entrez gene id
     * @param snpOnly
     * @param projection level of detail for each mutation
     * @param limit
     * @param offset
     * @param sortBy
     * @param direction
     * @return  list of mutation 
     */
    List<Mutation> getSummaryMutationsInMultipleMolecularProfiles(
            List<String> allMolecularProfileIds,
            List<String> allSampleIds,
            List<Integer> entrezGeneIds,
            boolean snpOnly, // Currently hardcoded to false due to how the legacy worked 
            String projection,
            Integer limit,
            Integer offset,
            String sortBy,
            String direction);

    /**
     * Retrieves mutation with DETAILED projection (complete data set)
     * 
     * <p>Returns complete mutation data including all mutation fields. This projection provides 
     * the most comprehensive data but may have higher performance costs due to joins.
     *
     * @param allMolecularProfileIds list containing molecularProfile 
     * @param allSampleIds list containing sampleIds
     * @param entrezGeneIds list of entrez gene id
     * @param snpOnly
     * @param projection level of detail for each mutation
     * @param limit
     * @param offset
     * @param sortBy
     * @param direction
     * @return  list of mutation 
     */
    List<Mutation> getDetailedMutationsInMultipleMolecularProfiles(
            List<String> allMolecularProfileIds,
            List<String> allSampleIds,
            List<Integer> entrezGeneIds,
            boolean snpOnly, // Currently hardcoded to false due to how the legacy worked 
            String projection,
            Integer limit,
            Integer offset,
            String sortBy,
            String direction);

    /**
     * Retrieves the count of mutation matching the specified criteria.
     * 
     * <p> Returns total count and sample count that would be returned by a corresponding data
     * retrieval operation, without actually fetching the data.
     * @param allMolecularProfileIds list containing molecularProfile 
     * @param allSampleIds list containing sampleIds    
     * @param entrezGeneIds list of entrez gene id
     * @param snpOnly
     * @return MutationMeta
     */

        MutationMeta getMetaMutationsInMultipleMolecularProfiles(  List<String> allMolecularProfileIds,
                                                                   List<String> allSampleIds,
                                                                 List<Integer> entrezGeneIds, 
                                                                 boolean snpOnly // Currently hardcoded to false due to how the legacy worked 
                                                                    );
}
