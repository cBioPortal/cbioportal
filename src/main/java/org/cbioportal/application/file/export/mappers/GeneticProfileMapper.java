package org.cbioportal.application.file.export.mappers;

import org.cbioportal.application.file.model.GeneticProfile;

import java.util.List;

public interface GeneticProfileMapper {
    List<GeneticProfile> getGeneticProfiles(String studyId);
}
