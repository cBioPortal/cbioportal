package org.cbioportal.infrastructure.repository.clickhouse.cancerstudy;

import org.cbioportal.cancerstudy.CancerStudyMetadata;

import java.util.List;

public interface ClickhouseCancerStudyMapper {
    List<CancerStudyMetadata> getCancerStudiesMetadata();
}
