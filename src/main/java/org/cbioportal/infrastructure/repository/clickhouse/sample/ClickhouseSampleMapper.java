package org.cbioportal.infrastructure.repository.clickhouse.sample;

import org.apache.ibatis.annotations.Param;
import org.cbioportal.domain.sample.Sample;
import org.cbioportal.domain.studyview.StudyViewFilterContext;

import java.util.List;

/**
 * Mapper interface for retrieving sample data from ClickHouse.
 * This interface provides methods for fetching filtered samples and sample counts based on the study view filter context.
 */
public interface ClickhouseSampleMapper {

    /**
     * Retrieves filtered samples based on the study view filter context.
     *
     * @param studyViewFilterContext the context of the study view filter
     * @return a list of filtered samples
     */
    List<Sample> getFilteredSamples(@Param("studyViewFilterContext") StudyViewFilterContext studyViewFilterContext);

    /**
     * Retrieves the sample count based on the study view filter context.
     *
     * @param studyViewFilterContext the context of the study view filter
     * @return the sample count
     */
    int getSampleCount(@Param("studyViewFilterContext") StudyViewFilterContext studyViewFilterContext);
}

