package org.cbioportal.service;

import java.util.List;

import org.cbioportal.model.GenericAssayData;
import org.cbioportal.model.meta.GenericAssayMeta;
import org.cbioportal.service.exception.MolecularProfileNotFoundException;

public interface GenericAssayService {

    /**
     * Retrieves metadata for generic assay data based on a list of stable IDs and molecular profile IDs.
     * Allows filtering by projection type.
     *
     * @param stableIds          a list of stable IDs for the generic assays
     * @param molecularProfileIds a list of molecular profile IDs
     * @param projection         the projection type
     * @return a list of generic assay metadata
     */
    List<GenericAssayMeta> getGenericAssayMetaByStableIdsAndMolecularIds(
        List<String> stableIds,
        List<String> molecularProfileIds,
        String projection
    );

    /**
     * Retrieves generic assay data for a specific molecular profile and sample list.
     * Filters by a list of stable IDs and allows specifying a projection type.
     *
     * @param molecularProfileId  the ID of the molecular profile
     * @param sampleListId        the ID of the sample list
     * @param genericAssayStableIds a list of stable IDs for the generic assays
     * @param projection          the projection type
     * @return a list of generic assay data
     * @throws MolecularProfileNotFoundException if the molecular profile is not found
     */
    List<GenericAssayData> getGenericAssayData(
        String molecularProfileId,
        String sampleListId,
        List<String> genericAssayStableIds,
        String projection
    ) throws MolecularProfileNotFoundException;

    /**
     * Fetches generic assay data for a specific molecular profile and a list of sample IDs.
     * Filters by a list of stable IDs and allows specifying a projection type.
     *
     * @param molecularProfileId  the ID of the molecular profile
     * @param sampleIds           a list of sample IDs
     * @param genericAssayStableIds a list of stable IDs for the generic assays
     * @param projection          the projection type
     * @return a list of generic assay data
     * @throws MolecularProfileNotFoundException if the molecular profile is not found
     */
    List<GenericAssayData> fetchGenericAssayData(
        String molecularProfileId,
        List<String> sampleIds,
        List<String> genericAssayStableIds,
        String projection
    ) throws MolecularProfileNotFoundException;

    /**
     * Fetches generic assay data for multiple molecular profiles and sample IDs.
     * Filters by a list of stable IDs and allows specifying a projection type.
     *
     * @param molecularProfileIds  a list of molecular profile IDs
     * @param sampleIds            a list of sample IDs
     * @param genericAssayStableIds a list of stable IDs for the generic assays
     * @param projection           the projection type
     * @return a list of generic assay data
     * @throws MolecularProfileNotFoundException if any of the molecular profiles are not found
     */
    List<GenericAssayData> fetchGenericAssayData(
        List<String> molecularProfileIds,
        List<String> sampleIds,
        List<String> genericAssayStableIds,
        String projection
    ) throws MolecularProfileNotFoundException;
}
