package org.cbioportal.application.file.export.services;

import java.util.List;
import java.util.Set;
import org.cbioportal.application.file.export.repositories.GenePanelMatrixRepository;
import org.cbioportal.application.file.model.GenePanelMatrixItem;
import org.cbioportal.application.file.utils.CloseableIterator;

public class GenePanelMatrixService {
  private final GenePanelMatrixRepository genePanelMatrixRepository;

  public GenePanelMatrixService(GenePanelMatrixRepository genePanelMatrixRepository) {
    this.genePanelMatrixRepository = genePanelMatrixRepository;
  }

  public boolean hasGenePanelMatrix(String studyId, Set<String> sampleIds) {
    return genePanelMatrixRepository.hasGenePanelMatrix(studyId, sampleIds);
  }

  public CloseableIterator<GenePanelMatrixItem> getGenePanelMatrix(
      String studyId, Set<String> sampleIds) {
    return genePanelMatrixRepository.getGenePanelMatrix(studyId, sampleIds);
  }

  public List<String> getDistinctGeneProfileIdsWithGenePanelMatrix(
      String studyId, Set<String> sampleIds) {
    return genePanelMatrixRepository.getDistinctGeneProfileIdsWithGenePanelMatrix(
        studyId, sampleIds);
  }
}
