package org.cbioportal.application.file.export.services;

import org.cbioportal.application.file.export.mappers.GeneticProfileMapper;
import org.cbioportal.application.file.model.GeneticProfileDatatypeMetadata;

import java.util.List;
import java.util.Set;

public class GeneticProfileService {

    private final GeneticProfileMapper geneticProfileMapper;

    public GeneticProfileService(GeneticProfileMapper geneticProfileMapper) {
        this.geneticProfileMapper = geneticProfileMapper;
    }

    public List<GeneticProfileDatatypeMetadata> getGeneticProfiles(String studyId, Set<String> sampleIds, String geneticAlterationType, String datatype) {
        return geneticProfileMapper.getGeneticProfiles(studyId, sampleIds, geneticAlterationType, datatype);
    }
}
