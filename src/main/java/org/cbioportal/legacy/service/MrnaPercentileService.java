package org.cbioportal.legacy.service;

import org.cbioportal.legacy.model.MrnaPercentile;
import org.cbioportal.legacy.service.exception.MolecularProfileNotFoundException;

import java.util.List;

public interface MrnaPercentileService {
    
    List<MrnaPercentile> fetchMrnaPercentile(String molecularProfileId, String sampleId, List<Integer> entrezGeneIds) 
        throws MolecularProfileNotFoundException;
}
