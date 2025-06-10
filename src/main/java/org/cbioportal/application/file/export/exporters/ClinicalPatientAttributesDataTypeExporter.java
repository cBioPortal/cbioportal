package org.cbioportal.application.file.export.exporters;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.cbioportal.application.file.export.services.ClinicalAttributeDataService;
import org.cbioportal.application.file.model.ClinicalAttribute;
import org.cbioportal.application.file.model.ClinicalAttributeValue;
import org.cbioportal.application.file.model.ClinicalAttributesMetadata;
import org.cbioportal.application.file.model.ClinicalAttributesTable;
import org.cbioportal.application.file.utils.CloseableIterator;

/** Export metadata and data for clinical patient attributes. */
public class ClinicalPatientAttributesDataTypeExporter
    extends DataTypeExporter<ClinicalAttributesMetadata, ClinicalAttributesTable> {

  private final ClinicalAttributeDataService clinicalDataAttributeDataService;

  public ClinicalPatientAttributesDataTypeExporter(
      ClinicalAttributeDataService clinicalDataAttributeDataService) {
    this.clinicalDataAttributeDataService = clinicalDataAttributeDataService;
  }

  @Override
  protected Optional<ClinicalAttributesMetadata> getMetadata(
      String studyId, Set<String> sampleIds) {
    if (!clinicalDataAttributeDataService.hasClinicalPatientAttributes(studyId, sampleIds)) {
      return Optional.empty();
    }
    return Optional.of(new ClinicalAttributesMetadata(studyId, "CLINICAL", "PATIENT_ATTRIBUTES"));
  }

  @Override
  protected ClinicalAttributesTable getData(String studyId, Set<String> sampleIds) {
    List<ClinicalAttribute> clinicalPatientAttributes = new ArrayList<>();
    clinicalPatientAttributes.add(ClinicalAttribute.PATIENT_ID);
    clinicalPatientAttributes.addAll(
        clinicalDataAttributeDataService.getClinicalPatientAttributes(studyId));
    CloseableIterator<ClinicalAttributeValue> clinicalPatientAttributeValues =
        clinicalDataAttributeDataService.getClinicalPatientAttributeValues(studyId, sampleIds);
    return new ClinicalAttributesTable(clinicalPatientAttributes, clinicalPatientAttributeValues);
  }
}
