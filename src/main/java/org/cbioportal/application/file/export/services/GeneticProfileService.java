package org.cbioportal.application.file.export.services;

import org.cbioportal.application.file.export.mappers.GeneticProfileMapper;
import org.cbioportal.application.file.model.GeneticProfile;

import java.util.List;

public class GeneticProfileService {

    private final GeneticProfileMapper geneticProfileMapper;

    public GeneticProfileService(GeneticProfileMapper geneticProfileMapper) {
        this.geneticProfileMapper = geneticProfileMapper;
    }

    public List<GeneticProfile> getGeneticProfiles(String studyId) {
        return geneticProfileMapper.getGeneticProfiles(studyId);
    }
}
