package org.cbioportal.service;

import org.cbioportal.model.MrnaPercentile;
import org.cbioportal.service.exception.MolecularProfileNotFoundException;

import java.util.List;

public interface MrnaPercentileService {
    
    List<MrnaPercentile> fetchMrnaPercentile(String molecularProfileId, String sampleId, List<Integer> entrezGeneIds) 
        throws MolecularProfileNotFoundException;
}
