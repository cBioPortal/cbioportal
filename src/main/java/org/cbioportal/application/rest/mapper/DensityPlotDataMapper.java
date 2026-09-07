package org.cbioportal.application.rest.mapper;

import org.cbioportal.application.rest.response.DensityPlotDataDTO;
import org.cbioportal.legacy.model.DensityPlotData;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface DensityPlotDataMapper {
  DensityPlotDataMapper INSTANCE = Mappers.getMapper(DensityPlotDataMapper.class);

  DensityPlotDataDTO toDTO(DensityPlotData densityPlotData);
}
