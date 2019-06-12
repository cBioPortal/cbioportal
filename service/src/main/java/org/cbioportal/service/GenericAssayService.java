package org.cbioportal.service;

import java.util.List;

import org.cbioportal.model.GenericAssayData;
import org.cbioportal.model.meta.GenericAssayMeta;
import org.cbioportal.service.exception.GenericAssayNotFoundException;
import org.cbioportal.service.exception.MolecularProfileNotFoundException;

public interface GenericAssayService {
    
    GenericAssayMeta getGenericAssayMetaByStableId(String stableId) 
        throws GenericAssayNotFoundException;

    List<GenericAssayMeta> getGenericAssayMetaByStableIds(List<String> stableIds)
        throws GenericAssayNotFoundException;

    List<GenericAssayData> getGenericAssayData(String molecularProfileId, String sampleListId, 
                                            List<Integer> genericAssayStableIds, String projection) 
        throws MolecularProfileNotFoundException;

    List<GenericAssayData> fetchGenericAssayData(String molecularProfileId, List<String> sampleIds, 
                                            List<Integer> genericAssayStableIds, String projection) 
        throws MolecularProfileNotFoundException;    

    List<GenericAssayData> getGenericAssayDataInMultipleMolecularProfiles(List<String> molecularProfileIds, 
                                                                        List<String> sampleIds, List<Integer> genericAssayStableIds, String projection)
        throws MolecularProfileNotFoundException;
}
