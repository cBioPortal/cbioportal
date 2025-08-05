package org.cbioportal.domain.mutation.util;

import org.cbioportal.legacy.web.parameter.SampleMolecularIdentifier;

import java.util.ArrayList;
import java.util.List;

public abstract class MutationUtil {
    private MutationUtil(){}

    /**
     * Extracts molecular profile IDs from a list of SampleMolecularIdentifier objects.
     * 
     * @param identifiers the list of SampleMolecularIdentifier Object
     * @return  a list of molecular profile ID strings 
     */
    public static List<String> extractMolecularProfileIds(List<SampleMolecularIdentifier> identifiers) {
        List<String> molecularProfileIds= new ArrayList<>();
        identifiers.forEach(
            molecularProfileId->molecularProfileIds.add(molecularProfileId.getMolecularProfileId()));
        return molecularProfileIds;
    }

    /**
     * Extracts molecular profile IDs from a list of SampleMolecularIdentifier objects.
     * 
     * @param identifiers the list of SampleMolecularIdentifier objects
     * @return a list of sample ID strings
     */
    public static List<String> extractSampleIds(List<SampleMolecularIdentifier> identifiers) {
        List<String> sampleIds= new ArrayList<>();
        identifiers.forEach(
            sampleId->sampleIds.add(sampleId.getSampleId()));
        return sampleIds;
    }
    
}
