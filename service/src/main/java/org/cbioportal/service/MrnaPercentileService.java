package org.cbioportal.service;

import org.cbioportal.model.MrnaPercentile;
import org.cbioportal.model.meta.BaseMeta;
import org.cbioportal.service.exception.GeneticProfileNotFoundException;
import org.cbioportal.service.exception.SampleNotFoundException;

import java.util.List;

public interface MrnaPercentileService {
    
    List<MrnaPercentile> fetchMrnaPercentile(String geneticProfileId, String sampleId, List<Integer> entrezGeneIds) 
        throws SampleNotFoundException, GeneticProfileNotFoundException;
}
