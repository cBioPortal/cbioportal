package org.cbioportal.legacy.service;

import java.util.List;
import org.cbioportal.legacy.model.GenesetCorrelation;
import org.cbioportal.legacy.service.exception.GenesetNotFoundException;
import org.cbioportal.legacy.service.exception.MolecularProfileNotFoundException;
import org.cbioportal.legacy.service.exception.SampleListNotFoundException;

public interface GenesetCorrelationService {

  List<GenesetCorrelation> fetchCorrelatedGenes(
      String genesetId, String geneticProfileId, double correlationThreshold)
      throws MolecularProfileNotFoundException, GenesetNotFoundException;

  List<GenesetCorrelation> fetchCorrelatedGenes(
      String genesetId,
      String geneticProfileId,
      List<String> sampleIds,
      double correlationThreshold)
      throws MolecularProfileNotFoundException, GenesetNotFoundException;

  List<GenesetCorrelation> fetchCorrelatedGenes(
      String genesetId, String geneticProfileId, String sampleListId, double correlationThreshold)
      throws MolecularProfileNotFoundException,
          SampleListNotFoundException,
          GenesetNotFoundException;
}
