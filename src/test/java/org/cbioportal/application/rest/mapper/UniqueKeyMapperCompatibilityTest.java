package org.cbioportal.application.rest.mapper;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.cbioportal.domain.clinical_data.ClinicalData;
import org.cbioportal.legacy.model.ClinicalEvent;
import org.cbioportal.legacy.model.CopyNumberSeg;
import org.cbioportal.legacy.model.MutationSpectrum;
import org.cbioportal.legacy.model.Patient;
import org.cbioportal.legacy.utils.Encoder;
import org.junit.Test;

public class UniqueKeyMapperCompatibilityTest {

  @Test
  public void mapsUniquePatientKeyForPatientDto() {
    Patient patient = new Patient();
    patient.setStableId("P001");
    patient.setCancerStudyIdentifier("study_1");

    var dto = PatientMapper.INSTANCE.toDto(patient);

    assertEquals(Encoder.calculateBase64("P001", "study_1"), dto.getUniquePatientKey());
    assertNull(dto.getUniqueSampleKey());
  }

  @Test
  public void mapsUniqueKeysForCopyNumberSegDto() {
    CopyNumberSeg seg = new CopyNumberSeg();
    seg.setSampleStableId("S001");
    seg.setPatientId("P001");
    seg.setCancerStudyIdentifier("study_1");

    var dto = CopyNumberSegMapper.INSTANCE.toDto(seg);

    assertEquals(Encoder.calculateBase64("S001", "study_1"), dto.getUniqueSampleKey());
    assertEquals(Encoder.calculateBase64("P001", "study_1"), dto.getUniquePatientKey());
  }

  @Test
  public void mapsUniqueKeysForMutationSpectrumDto() {
    MutationSpectrum spectrum = new MutationSpectrum();
    spectrum.setSampleId("S001");
    spectrum.setPatientId("P001");
    spectrum.setStudyId("study_1");

    var dto = MutationSpectrumMapper.INSTANCE.toDto(spectrum);

    assertEquals(Encoder.calculateBase64("S001", "study_1"), dto.uniqueSampleKey());
    assertEquals(Encoder.calculateBase64("P001", "study_1"), dto.uniquePatientKey());
  }

  @Test
  public void mapsUniquePatientKeyForClinicalEventDto() {
    ClinicalEvent event = new ClinicalEvent();
    event.setPatientId("P001");
    event.setStudyId("study_1");

    var dto = ClinicalEventMapper.INSTANCE.toDto(event);

    assertEquals(Encoder.calculateBase64("P001", "study_1"), dto.uniquePatientKey());
  }

  @Test
  public void doesNotCreateUniqueSampleKeyForPatientLevelClinicalData() {
    org.cbioportal.legacy.model.ClinicalData legacyClinicalData =
        new org.cbioportal.legacy.model.ClinicalData();
    legacyClinicalData.setPatientId("P001");
    legacyClinicalData.setStudyId("study_1");

    var legacyDto = LegacyClinicalDataMapper.INSTANCE.toDto(legacyClinicalData);
    assertNull(legacyDto.uniqueSampleKey());
    assertEquals(Encoder.calculateBase64("P001", "study_1"), legacyDto.uniquePatientKey());

    ClinicalData domainClinicalData =
        new ClinicalData(1, null, "P001", "study_1", "OS_STATUS", "LIVING");
    var domainDto = ClinicalDataMapper.INSTANCE.toClinicalDataDTO(domainClinicalData);
    assertNull(domainDto.uniqueSampleKey());
    assertEquals(Encoder.calculateBase64("P001", "study_1"), domainDto.uniquePatientKey());
  }
}
