package org.cbioportal.service;

import java.util.List;
import java.util.Map;

import org.cbioportal.model.AlterationEnrichment;
import org.cbioportal.model.MolecularProfileCaseIdentifier;
import org.cbioportal.model.web.parameter.EnrichmentType;
import org.cbioportal.service.exception.MolecularProfileNotFoundException;

public interface StructuralVariantEnrichmentService {

    List<AlterationEnrichment> getStructuralVariantEnrichments(
            Map<String, List<MolecularProfileCaseIdentifier>> molecularProfileCaseSets, EnrichmentType enrichmentType)
            throws MolecularProfileNotFoundException;

}
