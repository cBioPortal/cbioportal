package org.cbioportal.domain.coexpression.repository;

import java.util.List;
import org.cbioportal.infrastructure.repository.clickhouse.coexpression.CoExpressionResult;

public interface CoExpressionRepository {

  List<CoExpressionResult> getCoExpressions(
      String cancerStudyIdentifierA,
      String profileTypeA,
      String cancerStudyIdentifierB,
      String profileTypeB,
      String hugoGeneSymbol,
      List<String> sampleUniqueIds,
      Double threshold);
}
