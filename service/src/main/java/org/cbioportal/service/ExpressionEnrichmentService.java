package org.cbioportal.service;

import java.util.List;
import java.util.Map;
import org.cbioportal.model.ExpressionEnrichment;
import org.cbioportal.model.MolecularProfileCaseIdentifier;
import org.cbioportal.service.exception.MolecularProfileNotFoundException;

public interface ExpressionEnrichmentService {
    List<ExpressionEnrichment> getExpressionEnrichments(
        String molecularProfileId,
        Map<String, List<MolecularProfileCaseIdentifier>> molecularProfileCaseSets,
        String enrichmentType
    )
        throws MolecularProfileNotFoundException;
}
