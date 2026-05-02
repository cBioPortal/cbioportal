package org.cbioportal.domain.generic_assay.usecase;

import org.cbioportal.application.rest.response.GenericAssayDataMatrixDTO;
import org.cbioportal.legacy.model.GenericAssayData;
import org.cbioportal.legacy.service.GenericAssayService;
import org.cbioportal.legacy.service.exception.MolecularProfileNotFoundException;
import org.cbioportal.legacy.web.parameter.GenericAssayFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class FetchGenericAssayDataMatrixUseCase {

    private final GenericAssayService genericAssayService;

    @Autowired
    public FetchGenericAssayDataMatrixUseCase(GenericAssayService genericAssayService) {
        this.genericAssayService = genericAssayService;
    }

    public GenericAssayDataMatrixDTO execute(String molecularProfileId, GenericAssayFilter filter)
            throws MolecularProfileNotFoundException {
        // 1. Fetch flat data using existing repository logic
        List<GenericAssayData> flatData;
        if (filter.getSampleListId() != null) {
            flatData = genericAssayService.getGenericAssayData(
                    molecularProfileId,
                    filter.getSampleListId(),
                    filter.getGenericAssayStableIds(),
                    "SUMMARY");
        } else {
            flatData = genericAssayService.fetchGenericAssayData(
                    molecularProfileId,
                    filter.getSampleIds(),
                    filter.getGenericAssayStableIds(),
                    "SUMMARY");
        }
        
        // 2. Extract unique sample IDs to form our index array
        Set<String> sampleSet = new LinkedHashSet<>();
        for (GenericAssayData data : flatData) {
            sampleSet.add(data.getSampleId());
        }
        List<String> sampleIds = new ArrayList<>(sampleSet);
        
        // Fast lookup map for sample indices
        Map<String, Integer> sampleIndexMap = new HashMap<>();
        for (int i = 0; i < sampleIds.size(); i++) {
            sampleIndexMap.put(sampleIds.get(i), i);
        }
        
        // 3. Pivot into the matrix map
        Map<String, Object[]> entryArrays = new HashMap<>();
        for (GenericAssayData data : flatData) {
            String entityId = data.getGenericAssayStableId();
            
            if (!entryArrays.containsKey(entityId)) {
                entryArrays.put(entityId, new Object[sampleIds.size()]);
            }
            
            int index = sampleIndexMap.get(data.getSampleId());
            entryArrays.get(entityId)[index] = data.getValue(); 
        }
        
        // 4. Convert arrays to Lists for the DTO
        Map<String, List<Object>> entries = new HashMap<>();
        for (Map.Entry<String, Object[]> entry : entryArrays.entrySet()) {
             entries.put(entry.getKey(), Arrays.asList(entry.getValue()));
        }
        
        GenericAssayDataMatrixDTO matrixDTO = new GenericAssayDataMatrixDTO();
        matrixDTO.setSampleIds(sampleIds);
        matrixDTO.setEntries(entries);
        
        return matrixDTO;
    }
}