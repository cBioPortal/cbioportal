package org.cbioportal.domain.embedding.util;

import java.util.*;
import org.cbioportal.domain.embedding.EmbeddingData;
import org.cbioportal.domain.embedding.EmbeddingDefinition;
import org.cbioportal.domain.embedding.EmbeddingRow;

public abstract class EmbeddingUtil {
  private EmbeddingUtil() {}

  // Need methods that  can count the number of patients
  // Need method that can count the number of samples
  // Need a method to return a list of embedding data
  // Need a method to return a conversion to Embedding

  /**
   * @return
   */
  public static int countPatient(List<EmbeddingRow> embeddingRows) {
    return Math.toIntExact(embeddingRows.stream().map(EmbeddingRow::patientId).distinct().count());
  }

  /**
   * @return
   */
  public static int countSample(List<EmbeddingRow> embeddingRows) {
    return Math.toIntExact(embeddingRows.stream().map(EmbeddingRow::sampleId).distinct().count());
  }

  /**
   * @return
   */
  public static List<EmbeddingData> getEmbeddingData(List<EmbeddingRow> embeddingRows) {
    List<EmbeddingData> embeddingDataList = new ArrayList<>();

    for (EmbeddingRow embeddingRow : embeddingRows) {
      String patientId = embeddingRow.patientId();
      String sampleId = embeddingRow.sampleId();
      Double x = embeddingRow.x();
      Double y = embeddingRow.y();
      String customAttribute = embeddingRow.customAttribute();

      embeddingDataList.add(new EmbeddingData(patientId, sampleId, x, y, customAttribute));
    }
    return embeddingDataList;
  }

  public static List<String> getStudies(List<EmbeddingRow> embeddingRows) {
    return embeddingRows.stream().map(EmbeddingRow::studyIdentifier).distinct().toList();
  }

  /**
   * TODO check this method could be improved
   *
   * @return
   */
  public static List<EmbeddingDefinition> getUniqueEmbeddingDefinitions(
      List<EmbeddingRow> embeddingRows) {
    Set<EmbeddingDefinition> definitionSet = new HashSet<>();
    for (EmbeddingRow embeddingRow : embeddingRows) {
      int internalId = embeddingRow.internalId();
      String description = embeddingRow.description();
      String entityType = embeddingRow.entityType();
      String shortName = embeddingRow.shortName();

      definitionSet.add(new EmbeddingDefinition(internalId, description, entityType, shortName));
    }

    return definitionSet.stream().toList();
  }
}
