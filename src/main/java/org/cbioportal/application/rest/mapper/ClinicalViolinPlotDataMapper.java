package org.cbioportal.application.rest.mapper;

import org.cbioportal.application.rest.response.ClinicalViolinPlotDataDTO;
import org.cbioportal.legacy.model.ClinicalViolinPlotData;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface ClinicalViolinPlotDataMapper {
  ClinicalViolinPlotDataMapper INSTANCE = Mappers.getMapper(ClinicalViolinPlotDataMapper.class);

  ClinicalViolinPlotDataDTO toDTO(ClinicalViolinPlotData clinicalViolinPlotData);
}
