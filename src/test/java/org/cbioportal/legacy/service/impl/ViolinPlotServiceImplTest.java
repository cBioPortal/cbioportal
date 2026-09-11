package org.cbioportal.legacy.service.impl;

import static java.util.stream.Collectors.toSet;

import java.math.BigDecimal;
import java.util.*;
import org.cbioportal.legacy.model.ClinicalData;
import org.cbioportal.legacy.model.ClinicalViolinPlotData;
import org.cbioportal.legacy.model.Sample;
import org.cbioportal.legacy.web.parameter.StudyViewFilter;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.Silent.class)
public class ViolinPlotServiceImplTest {
  private static final String[][] FGA_VALUES =
      new String[][] {
        new String[] {"0.1", "0.5", "0.2", "0.351", "0.44", "0.87", "0.75", "0.9", "0.2", "0.25"},
        new String[] {"0.62", "0.451", "0.224", "1.0", "0.95"},
        new String[] {
          "0.91", "0.3", "0.15", "0.42", "0.951", "0.724", "0.6", "0.45", "0.11", "0.1", "0.5",
          "0.2", "0.351"
        },
        new String[] {"0.44", "0.87"},
        new String[] {
          "0.75", "0.9", "0.2", "0.25", "0.62", "0.451", "0.224", "1.0", "0.95", "0.91"
        },
        new String[] {"0.3"},
        new String[] {"0.15", "0.42", "0.951", "0.724", "0.6", "0.45", "0.11"}
      };

  private static final String[] EXPECTED_ROW_CATEGORIES = new String[] {};

  @InjectMocks private ViolinPlotServiceImpl violinPlotService;

  @Test
  public void getClinicalViolinPlotData() throws Exception {
    List<ClinicalData> sampleClinicalData = new ArrayList<>();
    List<Sample> filteredSamples = new ArrayList<>();
    for (int j = 0; j < FGA_VALUES.length; j++) {
      for (int i = 0; i < FGA_VALUES[j].length; i++) {
        ClinicalData num = new ClinicalData();
        num.setAttrId("FRACTION_GENOME_ALTERED");
        num.setAttrValue(FGA_VALUES[j][i]);
        num.setStudyId("test_study_id");
        num.setSampleId("test_sample_id_" + i + "_" + j);
        num.setUniqueSampleKey("test_sample_id_" + i + "_" + j);
        sampleClinicalData.add(num);

        ClinicalData cat = new ClinicalData();
        cat.setAttrId("CANCER_TYPE_DETAILED");
        cat.setAttrValue("cancer_type_" + j);
        cat.setStudyId("test_study_id");
        cat.setSampleId("test_sample_id_" + i + "_" + j);
        cat.setUniqueSampleKey("test_sample_id_" + i + "_" + j);
        sampleClinicalData.add(cat);

        Sample s = new Sample();
        s.setUniqueSampleKey("test_sample_id_" + i + "_" + j);
        filteredSamples.add(s);
      }
    }

    final int NUM_CURVE_POINTS = 100;
    Set<Integer> sampleIdsSet =
        filteredSamples.stream().map(s -> s.getInternalId()).collect(toSet());
    ClinicalViolinPlotData result =
        violinPlotService.getClinicalViolinPlotData(
            sampleClinicalData,
            sampleIdsSet,
            new BigDecimal(0),
            new BigDecimal(1),
            new BigDecimal(NUM_CURVE_POINTS),
            false,
            new BigDecimal(1),
            new StudyViewFilter(),
            false);

    // sort the rows so that they're in the same order as FGA_VALUES so that it's easy to access
    // them in order for the test
    result
        .getRows()
        .sort(
            (a, b) -> {
              return a.getCategory().compareTo(b.getCategory());
            });

    Assert.assertEquals(FGA_VALUES.length, result.getRows().size());
    Assert.assertEquals(
        NUM_CURVE_POINTS,
        result
            .getRows()
            .get(0)
            .getCurveData()
            .size()); // curve data of the right count when there's enough points
    Assert.assertEquals(
        0,
        result.getRows().get(1).getCurveData().size()); // no curve data when there's too few points
    Assert.assertEquals(
        FGA_VALUES[1].length,
        result
            .getRows()
            .get(1)
            .getIndividualPoints()
            .size()); // all data as individual points when theres few
    Assert.assertEquals(
        NUM_CURVE_POINTS,
        result
            .getRows()
            .get(2)
            .getCurveData()
            .size()); // curve data of the right count when there's enough points
    Assert.assertEquals(
        0,
        result.getRows().get(3).getCurveData().size()); // no curve data when there's too few points
    Assert.assertEquals(
        FGA_VALUES[3].length,
        result
            .getRows()
            .get(3)
            .getIndividualPoints()
            .size()); // all data as individual points when theres few
    Assert.assertEquals(
        NUM_CURVE_POINTS,
        result
            .getRows()
            .get(4)
            .getCurveData()
            .size()); // curve data of the right count when there's enough points
    Assert.assertEquals(
        0,
        result.getRows().get(5).getCurveData().size()); // no curve data when there's too few points
    Assert.assertEquals(
        FGA_VALUES[5].length,
        result
            .getRows()
            .get(5)
            .getIndividualPoints()
            .size()); // all data as individual points when theres few
    Assert.assertEquals(
        0,
        result.getRows().get(6).getCurveData().size()); // no curve data when there's too few points
    Assert.assertEquals(
        FGA_VALUES[6].length,
        result
            .getRows()
            .get(6)
            .getIndividualPoints()
            .size()); // all data as individual points when theres few

    // When patientAttribute is false, numPatients should be null and patientAttribute should be
    // false
    Assert.assertFalse(result.getPatientAttribute());
    for (int i = 0; i < result.getRows().size(); i++) {
      Assert.assertNull(
          "numPatients should be null when patientAttribute is false",
          result.getRows().get(i).getNumPatients());
    }

    // Verify sample counts per category
    Assert.assertEquals(Integer.valueOf(10), result.getRows().get(0).getNumSamples());
    Assert.assertEquals(Integer.valueOf(5), result.getRows().get(1).getNumSamples());
    Assert.assertEquals(Integer.valueOf(13), result.getRows().get(2).getNumSamples());
    Assert.assertEquals(Integer.valueOf(2), result.getRows().get(3).getNumSamples());
    Assert.assertEquals(Integer.valueOf(10), result.getRows().get(4).getNumSamples());
    Assert.assertEquals(Integer.valueOf(1), result.getRows().get(5).getNumSamples());
    Assert.assertEquals(Integer.valueOf(7), result.getRows().get(6).getNumSamples());
  }

  @Test
  public void getClinicalViolinPlotDataWithPatientCounts() throws Exception {
    // Create test data where 3 patients have 2 samples each (6 samples total)
    // All in the same category to make counting straightforward
    List<ClinicalData> sampleClinicalData = new ArrayList<>();
    List<Sample> filteredSamples = new ArrayList<>();
    int internalIdCounter = 1;

    String[] patients = {"patient_A", "patient_B", "patient_C"};
    String[][] numValues = {
      {"0.1", "0.5"}, // patient_A has 2 samples
      {"0.3", "0.7"}, // patient_B has 2 samples
      {"0.9", "0.2"} // patient_C has 2 samples
    };

    for (int p = 0; p < patients.length; p++) {
      for (int s = 0; s < numValues[p].length; s++) {
        String sampleId = "sample_" + p + "_" + s;
        int internalId = internalIdCounter++;

        ClinicalData num = new ClinicalData();
        num.setAttrId("AGE");
        num.setAttrValue(numValues[p][s]);
        num.setStudyId("test_study_id");
        num.setSampleId(sampleId);
        num.setPatientId(patients[p]);
        num.setInternalId(internalId);
        sampleClinicalData.add(num);

        ClinicalData cat = new ClinicalData();
        cat.setAttrId("SEX");
        cat.setAttrValue("Male");
        cat.setStudyId("test_study_id");
        cat.setSampleId(sampleId);
        cat.setPatientId(patients[p]);
        cat.setInternalId(internalId);
        sampleClinicalData.add(cat);

        Sample sample = new Sample();
        sample.setInternalId(internalId);
        sample.setStableId(sampleId);
        filteredSamples.add(sample);
      }
    }

    final int NUM_CURVE_POINTS = 100;
    Set<Integer> sampleIdsSet =
        filteredSamples.stream().map(s -> s.getInternalId()).collect(toSet());

    // Call with patientAttribute = true
    ClinicalViolinPlotData result =
        violinPlotService.getClinicalViolinPlotData(
            sampleClinicalData,
            sampleIdsSet,
            new BigDecimal(0),
            new BigDecimal(1),
            new BigDecimal(NUM_CURVE_POINTS),
            false,
            new BigDecimal(1),
            new StudyViewFilter(),
            true);

    Assert.assertTrue(result.getPatientAttribute());
    Assert.assertEquals(1, result.getRows().size()); // all in "Male" category

    // 6 samples total (3 patients x 2 samples each)
    Assert.assertEquals(Integer.valueOf(6), result.getRows().get(0).getNumSamples());

    // 3 unique patients
    Assert.assertEquals(Integer.valueOf(3), result.getRows().get(0).getNumPatients());
  }

  @Test
  public void getClinicalViolinPlotDataWithSingleSamplePatients() throws Exception {
    // When each patient has exactly 1 sample, numPatients should equal numSamples
    List<ClinicalData> sampleClinicalData = new ArrayList<>();
    List<Sample> filteredSamples = new ArrayList<>();

    String[] patients = {"patient_X", "patient_Y", "patient_Z"};
    String[] values = {"0.3", "0.6", "0.9"};

    for (int p = 0; p < patients.length; p++) {
      String sampleId = "sample_" + p;
      int internalId = p + 100;

      ClinicalData num = new ClinicalData();
      num.setAttrId("AGE");
      num.setAttrValue(values[p]);
      num.setStudyId("test_study_id");
      num.setSampleId(sampleId);
      num.setPatientId(patients[p]);
      num.setInternalId(internalId);
      sampleClinicalData.add(num);

      ClinicalData cat = new ClinicalData();
      cat.setAttrId("SEX");
      cat.setAttrValue("Female");
      cat.setStudyId("test_study_id");
      cat.setSampleId(sampleId);
      cat.setPatientId(patients[p]);
      cat.setInternalId(internalId);
      sampleClinicalData.add(cat);

      Sample sample = new Sample();
      sample.setInternalId(internalId);
      sample.setStableId(sampleId);
      filteredSamples.add(sample);
    }

    final int NUM_CURVE_POINTS = 100;
    Set<Integer> sampleIdsSet =
        filteredSamples.stream().map(s -> s.getInternalId()).collect(toSet());

    ClinicalViolinPlotData result =
        violinPlotService.getClinicalViolinPlotData(
            sampleClinicalData,
            sampleIdsSet,
            new BigDecimal(0),
            new BigDecimal(1),
            new BigDecimal(NUM_CURVE_POINTS),
            false,
            new BigDecimal(1),
            new StudyViewFilter(),
            true);

    Assert.assertTrue(result.getPatientAttribute());
    Assert.assertEquals(1, result.getRows().size());

    // With 1 sample per patient, counts should be equal
    Assert.assertEquals(Integer.valueOf(3), result.getRows().get(0).getNumSamples());
    Assert.assertEquals(Integer.valueOf(3), result.getRows().get(0).getNumPatients());
  }
}
