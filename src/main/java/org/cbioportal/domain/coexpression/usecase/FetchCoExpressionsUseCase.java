package org.cbioportal.domain.coexpression.usecase;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import org.apache.commons.math3.distribution.TDistribution;
import org.cbioportal.domain.coexpression.repository.CoExpressionRepository;
import org.cbioportal.infrastructure.repository.clickhouse.coexpression.CoExpressionResult;
import org.cbioportal.legacy.model.CoExpression;
import org.cbioportal.legacy.model.EntityType;
import org.cbioportal.legacy.model.Gene;
import org.cbioportal.legacy.model.MolecularProfile;
import org.cbioportal.legacy.persistence.SampleListRepository;
import org.cbioportal.legacy.service.GeneService;
import org.cbioportal.legacy.service.MolecularProfileService;
import org.cbioportal.legacy.service.exception.GeneNotFoundException;
import org.cbioportal.legacy.service.exception.GeneWithMultipleEntrezIdsException;
import org.cbioportal.legacy.service.exception.MolecularProfileNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class FetchCoExpressionsUseCase {

  private final CoExpressionRepository coExpressionRepository;
  private final MolecularProfileService molecularProfileService;
  private final SampleListRepository sampleListRepository;
  private final GeneService geneService;

  public FetchCoExpressionsUseCase(
      CoExpressionRepository coExpressionRepository,
      MolecularProfileService molecularProfileService,
      SampleListRepository sampleListRepository,
      GeneService geneService) {
    this.coExpressionRepository = coExpressionRepository;
    this.molecularProfileService = molecularProfileService;
    this.sampleListRepository = sampleListRepository;
    this.geneService = geneService;
  }

  public List<CoExpression> execute(
      String molecularProfileIdA,
      String molecularProfileIdB,
      Integer entrezGeneId,
      String sampleListId,
      Double threshold)
      throws MolecularProfileNotFoundException, GeneNotFoundException {

    List<String> sampleIds = sampleListRepository.getAllSampleIdsInSampleList(sampleListId);
    if (sampleIds.isEmpty()) {
      return Collections.emptyList();
    }
    return execute(molecularProfileIdA, molecularProfileIdB, entrezGeneId, sampleIds, threshold);
  }

  public List<CoExpression> execute(
      String molecularProfileIdA,
      String molecularProfileIdB,
      Integer entrezGeneId,
      List<String> sampleIds,
      Double threshold)
      throws MolecularProfileNotFoundException, GeneNotFoundException {

    if (sampleIds == null || sampleIds.isEmpty()) {
      return Collections.emptyList();
    }

    MolecularProfile profileA = molecularProfileService.getMolecularProfile(molecularProfileIdA);
    String cancerStudyIdentifierA = profileA.getCancerStudyIdentifier();
    String profileTypeA = molecularProfileIdA.substring(cancerStudyIdentifierA.length() + 1);

    String cancerStudyIdentifierB;
    String profileTypeB;
    if (molecularProfileIdA.equals(molecularProfileIdB)) {
      cancerStudyIdentifierB = cancerStudyIdentifierA;
      profileTypeB = profileTypeA;
    } else {
      MolecularProfile profileB = molecularProfileService.getMolecularProfile(molecularProfileIdB);
      cancerStudyIdentifierB = profileB.getCancerStudyIdentifier();
      profileTypeB = molecularProfileIdB.substring(cancerStudyIdentifierB.length() + 1);
    }

    Gene gene;
    try {
      gene = geneService.getGene(String.valueOf(entrezGeneId));
    } catch (GeneWithMultipleEntrezIdsException e) {
      return Collections.emptyList();
    }
    String hugoGeneSymbol = gene.getHugoGeneSymbol();

    List<String> sampleUniqueIds =
        sampleIds.stream().map(sampleId -> cancerStudyIdentifierA + "_" + sampleId).toList();

    List<CoExpressionResult> results =
        coExpressionRepository.getCoExpressions(
            cancerStudyIdentifierA,
            profileTypeA,
            cancerStudyIdentifierB,
            profileTypeB,
            hugoGeneSymbol,
            sampleUniqueIds,
            threshold);

    return results.stream()
        .map(
            result -> {
              CoExpression coExpression = new CoExpression();
              coExpression.setGeneticEntityId(String.valueOf(result.getEntrezGeneId()));
              coExpression.setGeneticEntityType(EntityType.GENE);
              if (result.getSpearmansCorrelation() != null) {
                coExpression.setSpearmansCorrelation(
                    BigDecimal.valueOf(result.getSpearmansCorrelation()));
                double pValue =
                    computePValue(result.getSpearmansCorrelation(), result.getNumSamples());
                coExpression.setpValue(BigDecimal.valueOf(pValue));
              }
              return coExpression;
            })
        .toList();
  }

  static double computePValue(double r, int n) {
    if (n <= 2) {
      return 1.0;
    }
    double rSquared = r * r;
    if (rSquared >= 1.0) {
      return 0.0;
    }
    double t = r * Math.sqrt((n - 2.0) / (1.0 - rSquared));
    TDistribution tDist = new TDistribution((double) n - 2);
    return 2.0 * tDist.cumulativeProbability(-Math.abs(t));
  }
}
