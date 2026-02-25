package org.cbioportal.legacy.service.impl;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;
import org.cbioportal.infrastructure.repository.clickhouse.coexpression.ClickhouseCoExpressionRepository;
import org.cbioportal.infrastructure.repository.clickhouse.coexpression.CoExpressionResult;
import org.cbioportal.legacy.model.CoExpression;
import org.cbioportal.legacy.model.EntityType;
import org.cbioportal.legacy.model.Gene;
import org.cbioportal.legacy.model.MolecularProfile;
import org.cbioportal.legacy.persistence.SampleListRepository;
import org.cbioportal.legacy.service.CoExpressionService;
import org.cbioportal.legacy.service.GeneService;
import org.cbioportal.legacy.service.MolecularProfileService;
import org.cbioportal.legacy.service.exception.GeneNotFoundException;
import org.cbioportal.legacy.service.exception.GeneWithMultipleEntrezIdsException;
import org.cbioportal.legacy.service.exception.GenesetNotFoundException;
import org.cbioportal.legacy.service.exception.MolecularProfileNotFoundException;
import org.cbioportal.legacy.service.exception.SampleListNotFoundException;
import org.cbioportal.legacy.service.util.CoExpressionAsyncMethods;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CoExpressionServiceImpl implements CoExpressionService {

  @Autowired private ClickhouseCoExpressionRepository clickhouseCoExpressionRepository;
  @Autowired private MolecularProfileService molecularProfileService;
  @Autowired private SampleListRepository sampleListRepository;
  @Autowired private GeneService geneService;

  @Override
  public List<CoExpression> getCoExpressions(
      String geneticEntityId,
      EntityType geneticEntityType,
      String sampleListId,
      String molecularProfileIdA,
      String molecularProfileIdB,
      Double threshold)
      throws MolecularProfileNotFoundException,
          SampleListNotFoundException,
          GenesetNotFoundException,
          GeneNotFoundException {

    if (geneticEntityType.equals(EntityType.GENESET)) {
      return Collections.emptyList();
    }

    List<String> sampleIds = sampleListRepository.getAllSampleIdsInSampleList(sampleListId);
    if (sampleIds.isEmpty()) {
      return Collections.emptyList();
    }

    return computeClickhouseCoExpressions(
        molecularProfileIdA,
        molecularProfileIdB,
        Integer.valueOf(geneticEntityId),
        sampleIds,
        threshold);
  }

  @Override
  public List<CoExpression> getCoExpressions(
      String molecularProfileId,
      String sampleListId,
      String geneticEntityId,
      EntityType geneticEntityType,
      Double threshold)
      throws MolecularProfileNotFoundException, GenesetNotFoundException, GeneNotFoundException {

    if (geneticEntityType.equals(EntityType.GENESET)) {
      return Collections.emptyList();
    }

    List<String> sampleIds = sampleListRepository.getAllSampleIdsInSampleList(sampleListId);
    if (sampleIds.isEmpty()) {
      return Collections.emptyList();
    }

    return computeClickhouseCoExpressions(
        molecularProfileId,
        molecularProfileId,
        Integer.valueOf(geneticEntityId),
        sampleIds,
        threshold);
  }

  @Override
  public List<CoExpression> fetchCoExpressions(
      String molecularProfileId,
      List<String> sampleIds,
      String queryGeneticEntityId,
      EntityType geneticEntityType,
      Double threshold)
      throws MolecularProfileNotFoundException, GenesetNotFoundException, GeneNotFoundException {

    if (geneticEntityType.equals(EntityType.GENESET)) {
      return Collections.emptyList();
    }

    return computeClickhouseCoExpressions(
        molecularProfileId,
        molecularProfileId,
        Integer.valueOf(queryGeneticEntityId),
        sampleIds,
        threshold);
  }

  @Override
  public List<CoExpression> fetchCoExpressions(
      String geneticEntityId,
      EntityType geneticEntityType,
      List<String> sampleIds,
      String molecularProfileIdA,
      String molecularProfileIdB,
      Double threshold)
      throws MolecularProfileNotFoundException, GenesetNotFoundException, GeneNotFoundException {

    if (geneticEntityType.equals(EntityType.GENESET)) {
      return Collections.emptyList();
    }

    return computeClickhouseCoExpressions(
        molecularProfileIdA,
        molecularProfileIdB,
        Integer.valueOf(geneticEntityId),
        sampleIds,
        threshold);
  }

  private List<CoExpression> computeClickhouseCoExpressions(
      String molecularProfileIdA,
      String molecularProfileIdB,
      Integer entrezGeneId,
      List<String> sampleIds,
      Double threshold)
      throws MolecularProfileNotFoundException, GeneNotFoundException {

    // Resolve profile A metadata
    MolecularProfile profileA = molecularProfileService.getMolecularProfile(molecularProfileIdA);
    String cancerStudyIdentifierA = profileA.getCancerStudyIdentifier();
    String profileTypeA = molecularProfileIdA.substring(cancerStudyIdentifierA.length() + 1);

    // Resolve profile B metadata (may be same as A)
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

    // Resolve hugo gene symbol from entrez gene id
    Gene gene;
    try {
      gene = geneService.getGene(String.valueOf(entrezGeneId));
    } catch (GeneWithMultipleEntrezIdsException e) {
      return Collections.emptyList();
    }
    String hugoGeneSymbol = gene.getHugoGeneSymbol();

    // Build sample_unique_ids
    List<String> sampleUniqueIds =
        sampleIds.stream()
            .map(sampleId -> cancerStudyIdentifierA + "_" + sampleId)
            .collect(Collectors.toList());

    List<CoExpressionResult> results =
        clickhouseCoExpressionRepository.getCoExpressions(
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
              coExpression.setSpearmansCorrelation(
                  BigDecimal.valueOf(result.getSpearmansCorrelation()));
              double pValue =
                  CoExpressionAsyncMethods.computePValue(
                      result.getSpearmansCorrelation(), result.getNumSamples());
              coExpression.setpValue(BigDecimal.valueOf(pValue));
              return coExpression;
            })
        .collect(Collectors.toList());
  }
}
