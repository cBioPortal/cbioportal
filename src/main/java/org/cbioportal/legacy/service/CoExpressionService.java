package org.cbioportal.legacy.service;

import java.util.List;
import org.cbioportal.legacy.model.CoExpression;
import org.cbioportal.legacy.model.EntityType;
import org.cbioportal.legacy.service.exception.GeneNotFoundException;
import org.cbioportal.legacy.service.exception.GenesetNotFoundException;
import org.cbioportal.legacy.service.exception.MolecularProfileNotFoundException;
import org.cbioportal.legacy.service.exception.SampleListNotFoundException;

public interface CoExpressionService {

  List<CoExpression> getCoExpressions(
      String molecularProfileId,
      String sampleListId,
      String geneticEntityId,
      EntityType geneticEntityType,
      Double threshold)
      throws MolecularProfileNotFoundException, GenesetNotFoundException, GeneNotFoundException;

  List<CoExpression> getCoExpressions(
      String geneticEntityId,
      EntityType geneticEntityType,
      String sampleListId,
      String molecularProfileIdA,
      String molecularProfileIdB,
      Double threshold)
      throws MolecularProfileNotFoundException,
          SampleListNotFoundException,
          GenesetNotFoundException,
          GeneNotFoundException;

  List<CoExpression> fetchCoExpressions(
      String geneticEntityId,
      EntityType geneticEntityType,
      List<String> sampleIds,
      String molecularProfileIdA,
      String molecularProfileIdB,
      Double threshold)
      throws MolecularProfileNotFoundException, GenesetNotFoundException, GeneNotFoundException;

  List<CoExpression> fetchCoExpressions(
      String molecularProfileId,
      List<String> sampleIds,
      String geneticEntityId,
      EntityType geneticEntityType,
      Double threshold)
      throws MolecularProfileNotFoundException, GenesetNotFoundException, GeneNotFoundException;
}
