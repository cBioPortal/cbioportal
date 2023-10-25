package org.cbioportal.service;

import java.util.List;

import org.cbioportal.model.GenericAssayData;
import org.cbioportal.model.meta.GenericAssayMeta;
import org.cbioportal.service.exception.MolecularProfileNotFoundException;

public interface GenericAssayService {
    
    List<GenericAssayMeta> getGenericAssayMetaByStableIdsAndMolecularIds(List<String> stableIds, List<String> molecularProfileIds, String projection);   

    List<GenericAssayData> getGenericAssayData(String molecularProfileId, String sampleListId, 
                                            List<String> genericAssayStableIds, String projection) 
        throws MolecularProfileNotFoundException;

    List<GenericAssayData> fetchGenericAssayData(String molecularProfileId, List<String> sampleIds, 
                                            List<String> genericAssayStableIds, String projection) 
        throws MolecularProfileNotFoundException;

    List<GenericAssayData> fetchGenericAssayData(List<String> molecularProfileIds, List<String> sampleIds, 
                                            List<String> genericAssayStableIds, String projection)
        throws MolecularProfileNotFoundException;
}
