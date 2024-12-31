package org.cbioportal.service;

import org.cbioportal.model.*;
import org.cbioportal.model.meta.BaseMeta;
import org.cbioportal.service.exception.MolecularProfileNotFoundException;

import java.util.List;

public interface DiscreteCopyNumberService {

    /**
     * Retrieves discrete copy number data for a specific molecular profile based on a sample list ID.
     * Allows filtering by gene IDs, alteration types, and projection type.
     *
     * @param molecularProfileId the ID of the molecular profile
     * @param sampleListId       the ID of the sample list
     * @param entrezGeneIds      a list of Entrez gene IDs to filter by
     * @param alterationTypes    a list of alteration types to filter by
     * @param projection         the projection type
     * @return a list of discrete copy number data
     * @throws MolecularProfileNotFoundException if the molecular profile is not found
     */
    List<DiscreteCopyNumberData> getDiscreteCopyNumbersInMolecularProfileBySampleListId(
        String molecularProfileId,
        String sampleListId,
        List<Integer> entrezGeneIds,
        List<Integer> alterationTypes,
        String projection
    ) throws MolecularProfileNotFoundException;

    /**
     * Retrieves metadata for discrete copy number data in a specific molecular profile 
     * using a sample list ID and filters by gene IDs and alteration types.
     *
     * @param molecularProfileId the ID of the molecular profile
     * @param sampleListId       the ID of the sample list
     * @param entrezGeneIds      a list of Entrez gene IDs to filter by
     * @param alterationTypes    a list of alteration types to filter by
     * @return metadata for the discrete copy number data
     * @throws MolecularProfileNotFoundException if the molecular profile is not found
     */
    BaseMeta getMetaDiscreteCopyNumbersInMolecularProfileBySampleListId(
        String molecularProfileId,
        String sampleListId,
        List<Integer> entrezGeneIds,
        List<Integer> alterationTypes
    ) throws MolecularProfileNotFoundException;

    /**
     * Fetches discrete copy number data for a specific molecular profile based on sample IDs.
     * Filters by gene IDs, alteration types, and projection type.
     *
     * @param molecularProfileId the ID of the molecular profile
     * @param sampleIds          a list of sample IDs
     * @param entrezGeneIds      a list of Entrez gene IDs to filter by
     * @param alterationTypes    a list of alteration types to filter by
     * @param projection         the projection type
     * @return a list of discrete copy number data
     * @throws MolecularProfileNotFoundException if the molecular profile is not found
     */
    List<DiscreteCopyNumberData> fetchDiscreteCopyNumbersInMolecularProfile(
        String molecularProfileId,
        List<String> sampleIds,
        List<Integer> entrezGeneIds,
        List<Integer> alterationTypes,
        String projection
    ) throws MolecularProfileNotFoundException;

    /**
     * Retrieves discrete copy number data for multiple molecular profiles based on sample IDs.
     * Filters by gene IDs, alteration types, and projection type.
     *
     * @param molecularProfileIds a list of molecular profile IDs
     * @param sampleIds           a list of sample IDs
     * @param entrezGeneIds       a list of Entrez gene IDs to filter by
     * @param alterationTypes     a list of alteration types to filter by
     * @param projection          the projection type
     * @return a list of discrete copy number data
     */
    List<DiscreteCopyNumberData> getDiscreteCopyNumbersInMultipleMolecularProfiles(
        List<String> molecularProfileIds,
        List<String> sampleIds,
        List<Integer> entrezGeneIds,
        List<Integer> alterationTypes,
        String projection
    );

    /**
     * Retrieves discrete copy number data for multiple molecular profiles using gene queries.
     *
     * @param molecularProfileIds a list of molecular profile IDs
     * @param sampleIds           a list of sample IDs
     * @param geneQueries         a list of gene filter queries
     * @param projection          the projection type
     * @return a list of discrete copy number data
     */
    List<DiscreteCopyNumberData> getDiscreteCopyNumbersInMultipleMolecularProfilesByGeneQueries(
        List<String> molecularProfileIds,
        List<String> sampleIds,
        List<GeneFilterQuery> geneQueries,
        String projection
    );

    /**
     * Fetches metadata for discrete copy number data in a specific molecular profile based on sample IDs.
     * Filters by gene IDs and alteration types.
     *
     * @param molecularProfileId the ID of the molecular profile
     * @param sampleIds          a list of sample IDs
     * @param entrezGeneIds      a list of Entrez gene IDs to filter by
     * @param alterationTypes    a list of alteration types to filter by
     * @return metadata for the discrete copy number data
     * @throws MolecularProfileNotFoundException if the molecular profile is not found
     */
    BaseMeta fetchMetaDiscreteCopyNumbersInMolecularProfile(
        String molecularProfileId,
        List<String> sampleIds,
        List<Integer> entrezGeneIds,
        List<Integer> alterationTypes
    ) throws MolecularProfileNotFoundException;

    /**
     * Retrieves sample counts grouped by gene and alteration type for specific sample IDs 
     * in a molecular profile.
     *
     * @param molecularProfileId the ID of the molecular profile
     * @param sampleIds          a list of sample IDs
     * @param entrezGeneIds      a list of Entrez gene IDs
     * @param alterations        a list of alteration types
     * @return a list of copy number counts by gene
     * @throws MolecularProfileNotFoundException if the molecular profile is not found
     */
    List<CopyNumberCountByGene> getSampleCountByGeneAndAlterationAndSampleIds(
        String molecularProfileId,
        List<String> sampleIds,
        List<Integer> entrezGeneIds,
        List<Integer> alterations
    ) throws MolecularProfileNotFoundException;

    /**
     * Fetches copy number counts grouped by gene and alteration type for a specific molecular profile.
     *
     * @param molecularProfileId the ID of the molecular profile
     * @param entrezGeneIds      a list of Entrez gene IDs
     * @param alterations        a list of alteration types
     * @return a list of copy number counts
     * @throws MolecularProfileNotFoundException if the molecular profile is not found
     */
    List<CopyNumberCount> fetchCopyNumberCounts(
        String molecularProfileId,
        List<Integer> entrezGeneIds,
        List<Integer> alterations
    ) throws MolecularProfileNotFoundException;
}