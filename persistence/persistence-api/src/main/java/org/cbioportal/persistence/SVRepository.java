package org.cbioportal.persistence;

import org.cbioportal.model.SV;

import java.util.List;
/**
 *
 * @author jake
 */
public interface SVRepository {
    

    List<SV> getSV(List<String> geneticProfileStableIds, List<String> hugoGeneSymbols, List<String> sampleStableIds);
    
}
