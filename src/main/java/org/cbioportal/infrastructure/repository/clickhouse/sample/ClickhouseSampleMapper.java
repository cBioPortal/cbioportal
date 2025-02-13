package org.cbioportal.infrastructure.repository.clickhouse.sample;

import org.apache.ibatis.annotations.Param;
import org.cbioportal.sample.Sample;
import org.cbioportal.studyview.StudyViewFilterContext;

import java.util.List;

public interface ClickhouseSampleMapper {
    List<Sample> getFilteredSamples(@Param("studyViewFilterContext") StudyViewFilterContext studyViewFilterContext);
    int getSampleCount(@Param("studyViewFilterContext") StudyViewFilterContext studyViewFilterContext);
}
