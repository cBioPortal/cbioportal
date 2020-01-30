package org.cbioportal.service;

import java.util.List;
import org.cbioportal.model.MrnaPercentile;
import org.cbioportal.service.exception.MolecularProfileNotFoundException;

public interface MrnaPercentileService {
    List<MrnaPercentile> fetchMrnaPercentile(
        String molecularProfileId,
        String sampleId,
        List<Integer> entrezGeneIds
    )
        throws MolecularProfileNotFoundException;
}
