package org.cbioportal.application.rest.mapper;

import org.cbioportal.application.rest.response.GeneDTO;
import org.cbioportal.legacy.model.Gene;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface GeneMapper {
    GeneMapper INSTANCE = Mappers.getMapper(GeneMapper.class);

    GeneDTO toGeneDTO(Gene gene);
}
