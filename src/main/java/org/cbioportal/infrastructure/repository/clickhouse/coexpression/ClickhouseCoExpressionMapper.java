package org.cbioportal.infrastructure.repository.clickhouse.coexpression;

import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface ClickhouseCoExpressionMapper {

  List<CoExpressionResult> getCoExpressions(
      @Param("cancerStudyIdentifierA") String cancerStudyIdentifierA,
      @Param("profileTypeA") String profileTypeA,
      @Param("cancerStudyIdentifierB") String cancerStudyIdentifierB,
      @Param("profileTypeB") String profileTypeB,
      @Param("hugoGeneSymbol") String hugoGeneSymbol,
      @Param("sampleUniqueIds") String[] sampleUniqueIds,
      @Param("threshold") Double threshold);
}
