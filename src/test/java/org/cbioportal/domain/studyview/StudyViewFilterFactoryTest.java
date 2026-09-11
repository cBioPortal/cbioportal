package org.cbioportal.domain.studyview;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
import org.cbioportal.legacy.model.MolecularProfile;
import org.cbioportal.legacy.persistence.enums.DataSource;
import org.cbioportal.legacy.web.columnar.util.CustomDataFilterUtil;
import org.cbioportal.legacy.web.parameter.DataFilterValue;
import org.cbioportal.legacy.web.parameter.GenericAssayDataFilter;
import org.cbioportal.legacy.web.parameter.StudyViewFilter;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class StudyViewFilterFactoryTest {

  private static final String STUDY_ID = "study_tcga_pub";
  private static final String PROFILE_TYPE = "generic_assay_profile";

  @Mock private CustomDataFilterUtil customDataFilterUtil;

  // Regression test: a genericAssayDataFilter without values (null or empty) should not cause
  // a downstream SQL error (analogous to the genomicDataFilter fix for #11206).
  @Test
  public void makeFiltersOutGenericAssayDataFilterWithNullValues() {
    StudyViewFilter studyViewFilter = new StudyViewFilter();
    studyViewFilter.setStudyIds(List.of(STUDY_ID));

    GenericAssayDataFilter filterWithNullValues =
        new GenericAssayDataFilter("stable_id", PROFILE_TYPE);
    studyViewFilter.setGenericAssayDataFilters(List.of(filterWithNullValues));

    when(customDataFilterUtil.extractCustomDataSamples(studyViewFilter)).thenReturn(List.of());
    when(customDataFilterUtil.extractInvolvedCancerStudies(studyViewFilter))
        .thenReturn(List.of(STUDY_ID));

    MolecularProfile categoricalProfile = new MolecularProfile();
    categoricalProfile.setStableId(STUDY_ID + "_" + PROFILE_TYPE);
    categoricalProfile.setCancerStudyIdentifier(STUDY_ID);
    categoricalProfile.setDatatype("CATEGORICAL");
    Map<DataSource, List<MolecularProfile>> genericAssayProfilesMap =
        Map.of(DataSource.SAMPLE, List.of(categoricalProfile));

    StudyViewFilterContext context =
        StudyViewFilterFactory.make(studyViewFilter, customDataFilterUtil, genericAssayProfilesMap);

    // the values-less filter is a no-op and must be dropped, otherwise the mapper's
    // <foreach collection="genericAssayDataFilter.values"> throws on the null list
    assertTrue(context.genericAssayDataFilters().isEmpty());
    assertTrue(
        context
            .categorizedGenericAssayDataCountFilter()
            .getSampleCategoricalGenericAssayDataFilters()
            .isEmpty());
  }

  @Test
  public void makeKeepsGenericAssayDataFilterWithValues() {
    StudyViewFilter studyViewFilter = new StudyViewFilter();
    studyViewFilter.setStudyIds(List.of(STUDY_ID));

    GenericAssayDataFilter filterWithValues = new GenericAssayDataFilter("stable_id", PROFILE_TYPE);
    DataFilterValue dataFilterValue = new DataFilterValue();
    dataFilterValue.setValue("Gain");
    filterWithValues.setValues(List.of(dataFilterValue));
    studyViewFilter.setGenericAssayDataFilters(List.of(filterWithValues));

    when(customDataFilterUtil.extractCustomDataSamples(studyViewFilter)).thenReturn(List.of());
    when(customDataFilterUtil.extractInvolvedCancerStudies(studyViewFilter))
        .thenReturn(List.of(STUDY_ID));

    MolecularProfile categoricalProfile = new MolecularProfile();
    categoricalProfile.setStableId(STUDY_ID + "_" + PROFILE_TYPE);
    categoricalProfile.setCancerStudyIdentifier(STUDY_ID);
    categoricalProfile.setDatatype("CATEGORICAL");
    Map<DataSource, List<MolecularProfile>> genericAssayProfilesMap =
        Map.of(DataSource.SAMPLE, List.of(categoricalProfile));

    StudyViewFilterContext context =
        StudyViewFilterFactory.make(studyViewFilter, customDataFilterUtil, genericAssayProfilesMap);

    assertEquals(1, context.genericAssayDataFilters().size());
    assertEquals(
        1,
        context
            .categorizedGenericAssayDataCountFilter()
            .getSampleCategoricalGenericAssayDataFilters()
            .size());
  }
}
