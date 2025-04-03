package org.cbioportal.application.file.export.services;

import org.cbioportal.application.file.export.mappers.GeneticProfileMapper;
import org.cbioportal.application.file.model.GeneticProfileDatatypeMetadata;

import java.util.List;

public class GeneticProfileService {

    private final GeneticProfileMapper geneticProfileMapper;

    public GeneticProfileService(GeneticProfileMapper geneticProfileMapper) {
        this.geneticProfileMapper = geneticProfileMapper;
    }

    public List<GeneticProfileDatatypeMetadata> getGeneticProfiles(String studyId, String geneticAlterationType, String datatype) {
        return geneticProfileMapper.getGeneticProfiles(studyId, geneticAlterationType, datatype);
    }
}
