package org.cbioportal.service;

import java.util.List;
import java.util.Map;

import org.cbioportal.model.GenericAssayEnrichment;
import org.cbioportal.model.GenomicEnrichment;
import org.cbioportal.model.MolecularProfileCaseIdentifier;
import org.cbioportal.service.exception.MolecularProfileNotFoundException;

public interface ExpressionEnrichmentService {

    List<GenomicEnrichment> getGenomicEnrichments(String molecularProfileId,
            Map<String, List<MolecularProfileCaseIdentifier>> molecularProfileCaseSets, String enrichmentType)
            throws MolecularProfileNotFoundException;

    List<GenericAssayEnrichment> getGenericAssayEnrichments(String molecularProfileId,
            Map<String, List<MolecularProfileCaseIdentifier>> molecularProfileCaseSets, String enrichmentType)
            throws MolecularProfileNotFoundException;

}
