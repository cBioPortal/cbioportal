package org.cbioportal.legacy.service;

import java.util.List;
import java.util.Map;

import org.cbioportal.legacy.model.EnrichmentType;
import org.cbioportal.legacy.model.GenericAssayEnrichment;
import org.cbioportal.legacy.model.GenomicEnrichment;
import org.cbioportal.legacy.model.MolecularProfileCaseIdentifier;
import org.cbioportal.legacy.model.GenericAssayBinaryEnrichment;
import org.cbioportal.legacy.model.GenericAssayCategoricalEnrichment;
import org.cbioportal.legacy.service.exception.MolecularProfileNotFoundException;

public interface ExpressionEnrichmentService {

    List<GenomicEnrichment> getGenomicEnrichments(String molecularProfileId,
            Map<String, List<MolecularProfileCaseIdentifier>> molecularProfileCaseSets, EnrichmentType enrichmentType)
            throws MolecularProfileNotFoundException;

    List<GenericAssayEnrichment> getGenericAssayNumericalEnrichments(String molecularProfileId,
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
