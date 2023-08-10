package org.cbioportal.service;

import java.util.List;
import java.util.Map;

import org.cbioportal.model.*;
import org.cbioportal.service.exception.MolecularProfileNotFoundException;

public interface ExpressionEnrichmentService {

    List<GenomicEnrichment> getGenomicEnrichments(String molecularProfileId,
            Map<String, List<MolecularProfileCaseIdentifier>> molecularProfileCaseSets, EnrichmentType enrichmentType)
            throws MolecularProfileNotFoundException;

    List<GenericAssayEnrichment> getGenericAssayEnrichments(String molecularProfileId,
            Map<String, List<MolecularProfileCaseIdentifier>> molecularProfileCaseSets, EnrichmentType enrichmentType)
            throws MolecularProfileNotFoundException;

    List<GenericAssayBinaryEnrichment> getGenericAssayBinaryEnrichments(
        String molecularProfileId,
        Map<String, List<MolecularProfileCaseIdentifier>> molecularProfileCaseSets,
        EnrichmentType enrichmentType)
        throws MolecularProfileNotFoundException;

    List<GenericAssayCategoricalEnrichment> getGenericAssayCategoricalEnrichments(String molecularProfileId,
                                                                                  Map<String, List<MolecularProfileCaseIdentifier>> molecularProfileCaseSets,
                                                                                  EnrichmentType enrichmentType)
        throws MolecularProfileNotFoundException;
    
}
