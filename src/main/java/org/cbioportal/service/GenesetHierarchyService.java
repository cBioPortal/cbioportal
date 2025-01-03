package org.cbioportal.service;

import java.util.List;

import org.cbioportal.model.GenesetHierarchyInfo;
import org.cbioportal.service.exception.MolecularProfileNotFoundException;
import org.cbioportal.service.exception.SampleListNotFoundException;

public interface GenesetHierarchyService {

    /**
     * Fetches gene set hierarchy information for a specific genetic profile.
     * Filters results based on percentile, score threshold, and p-value threshold.
     *
     * @param geneticProfileId the ID of the genetic profile
     * @param percentile       the percentile threshold to filter gene sets
     * @param scoreThreshold   the score threshold to filter gene sets
     * @param pvalueThreshold  the p-value threshold to filter gene sets
     * @return a list of gene set hierarchy information
     * @throws MolecularProfileNotFoundException if the genetic profile is not found
     */
    List<GenesetHierarchyInfo> fetchGenesetHierarchyInfo(
        String geneticProfileId,
        Integer percentile,
        Double scoreThreshold,
        Double pvalueThreshold
    ) throws MolecularProfileNotFoundException;

    /**
     * Fetches gene set hierarchy information for a specific genetic profile and a list of sample IDs.
     * Filters results based on percentile, score threshold, and p-value threshold.
     *
     * @param geneticProfileId the ID of the genetic profile
     * @param percentile       the percentile threshold to filter gene sets
     * @param scoreThreshold   the score threshold to filter gene sets
     * @param pvalueThreshold  the p-value threshold to filter gene sets
     * @param sampleIds        a list of sample IDs to filter the results
     * @return a list of gene set hierarchy information
     * @throws MolecularProfileNotFoundException if the genetic profile is not found
     */
    List<GenesetHierarchyInfo> fetchGenesetHierarchyInfo(
        String geneticProfileId,
        Integer percentile,
        Double scoreThreshold,
        Double pvalueThreshold,
        List<String> sampleIds
    ) throws MolecularProfileNotFoundException;

    /**
     * Fetches gene set hierarchy information for a specific genetic profile and a sample list ID.
     * Filters results based on percentile, score threshold, and p-value threshold.
     *
     * @param geneticProfileId the ID of the genetic profile
     * @param percentile       the percentile threshold to filter gene sets
     * @param scoreThreshold   the score threshold to filter gene sets
     * @param pvalueThreshold  the p-value threshold to filter gene sets
     * @param sampleListId     the ID of the sample list to filter the results
     * @return a list of gene set hierarchy information
     * @throws MolecularProfileNotFoundException if the genetic profile is not found
     * @throws SampleListNotFoundException       if the sample list is not found
     */
    List<GenesetHierarchyInfo> fetchGenesetHierarchyInfo(
        String geneticProfileId,
        Integer percentile,
        Double scoreThreshold,
        Double pvalueThreshold,
        String sampleListId
    ) throws MolecularProfileNotFoundException, SampleListNotFoundException;
}