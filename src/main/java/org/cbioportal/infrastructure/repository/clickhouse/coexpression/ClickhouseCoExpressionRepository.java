package org.cbioportal.infrastructure.repository.clickhouse.coexpression;

import java.util.List;
import org.springframework.stereotype.Repository;

@Repository
public class ClickhouseCoExpressionRepository {

  private final ClickhouseCoExpressionMapper mapper;

  public ClickhouseCoExpressionRepository(ClickhouseCoExpressionMapper mapper) {
    this.mapper = mapper;
  }

  public List<CoExpressionResult> getCoExpressions(
      String cancerStudyIdentifierA,
      String profileTypeA,
      String cancerStudyIdentifierB,
      String profileTypeB,
      String hugoGeneSymbol,
      List<String> sampleUniqueIds,
      Double threshold) {
    return mapper.getCoExpressions(
        cancerStudyIdentifierA,
        profileTypeA,
        cancerStudyIdentifierB,
        profileTypeB,
        hugoGeneSymbol,
        sampleUniqueIds,
        threshold);
  }
}
