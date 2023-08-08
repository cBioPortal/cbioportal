package org.cbioportal.service;

import org.cbioportal.model.EnrichmentType;
import org.cbioportal.model.GenericAssayBinaryEnrichment;
import org.cbioportal.model.MolecularProfileCaseIdentifier;
import org.cbioportal.service.exception.MolecularProfileNotFoundException;

import java.util.List;
import java.util.Map;

public interface GenericAssayBinaryDataService {
    List<GenericAssayBinaryEnrichment> getGenericAssayBinaryEnrichments(
        String molecularProfileId,
        Map<String, List<MolecularProfileCaseIdentifier>> molecularProfileCaseSets,
        EnrichmentType enrichmentType)
        throws MolecularProfileNotFoundException;

}
