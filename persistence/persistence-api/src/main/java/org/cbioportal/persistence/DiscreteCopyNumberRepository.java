package org.cbioportal.persistence;

import org.cbioportal.model.DiscreteCopyNumberData;
import org.cbioportal.model.meta.BaseMeta;

import java.util.List;

public interface DiscreteCopyNumberRepository {
    
    List<DiscreteCopyNumberData> getDiscreteCopyNumbersInGeneticProfile(String geneticProfileId, String sampleId, 
                                                                        List<Integer> alterations, String projection);


    BaseMeta getMetaDiscreteCopyNumbersInGeneticProfile(String geneticProfileId, String sampleId, 
                                                        List<Integer> alterations);

    List<DiscreteCopyNumberData> fetchDiscreteCopyNumbersInGeneticProfile(String geneticProfileId, 
                                                                          List<String> sampleIds, 
                                                                          List<Integer> alterations, String projection);

    BaseMeta fetchMetaDiscreteCopyNumbersInGeneticProfile(String geneticProfileId, List<String> sampleIds, 
                                                          List<Integer> alterations);
}
