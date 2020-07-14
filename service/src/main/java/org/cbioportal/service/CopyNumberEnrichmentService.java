package org.cbioportal.service;

import org.cbioportal.model.AlterationEnrichment;
import org.cbioportal.model.CNA;
import org.cbioportal.model.MolecularProfileCaseIdentifier;
import org.cbioportal.model.util.Select;
import org.cbioportal.service.exception.MolecularProfileNotFoundException;

import java.util.List;
import java.util.Map;

public interface CopyNumberEnrichmentService {

    List<AlterationEnrichment> getCopyNumberEnrichments(Map<String, List<MolecularProfileCaseIdentifier>> molecularProfileCaseSets,
                                                        CNA copyNumberEventType,
                                                        boolean includeDriver,
                                                        boolean includeVUS,
                                                        boolean includeUnknownOncogenicity,
                                                        Select<String> selectedTiers,
                                                        boolean includeUnknownTier) throws MolecularProfileNotFoundException;
}
