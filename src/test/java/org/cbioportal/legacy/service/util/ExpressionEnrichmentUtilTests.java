/*
 * Copyright 2002-2026 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.cbioportal.legacy.service.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.cbioportal.legacy.model.EnrichmentType;
import org.cbioportal.legacy.model.GeneMolecularAlteration;
import org.cbioportal.legacy.model.GenomicEnrichment;
import org.cbioportal.legacy.model.GroupStatistics;
import org.cbioportal.legacy.model.MolecularProfile;
import org.cbioportal.legacy.model.MolecularProfileCaseIdentifier;
import org.cbioportal.legacy.model.MolecularProfileSamples;
import org.cbioportal.legacy.model.Sample;
import org.cbioportal.legacy.persistence.MolecularDataRepository;
import org.cbioportal.legacy.service.SampleService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ExpressionEnrichmentUtilTests {

  private static final String STUDY_ID = "test_study";
  private static final double TOLERANCE = 1e-12;

  @InjectMocks private ExpressionEnrichmentUtil expressionEnrichmentUtil;
  @Mock private MolecularDataRepository molecularDataRepository;
  @Mock private SampleService sampleService;

  // Independent reference values: SciPy 1.18.1 ttest_ind(..., equal_var=False)
  // and f_oneway(...). Expected values are literals, not calculated with Commons Math.
  @Test
  public void usesWelchTestForTwoGroupsWithUnequalSizesAndVariances() {
    GenomicEnrichment result = enrichments("test_expression", "1,2,4", "5,9,10,15").get(0);

    assertThat(result.getEntrezGeneId()).isEqualTo(7157);
    assertThat(result.getpValue().doubleValue()).isCloseTo(0.029508154789979552, within(TOLERANCE));
    assertGroup(result, "group0", 2.3333333333333335, 1.5275252316519465);
    assertGroup(result, "group1", 9.75, 4.112987559751022);
  }

  @Test
  public void usesAnovaForThreeGroups() {
    List<GenomicEnrichment> results = enrichments("test_expression", "1,2,3", "4,6,8", "10,12,14");

    assertThat(results).hasSize(1);
    GenomicEnrichment result = results.get(0);
    assertThat(result.getpValue().doubleValue())
        .isCloseTo(0.0011870547526969264, within(TOLERANCE));
    assertGroup(result, "group0", 2, 1);
    assertGroup(result, "group1", 6, 2);
    assertGroup(result, "group2", 12, 2);
  }

  @Test
  public void clampsNegativeRnaSeqValuesBeforeLog2Transformation() {
    // log2(1 + max(0, x)) produces [0, 0, 2, 3] and [1, 4, 5, 6].
    GenomicEnrichment result = enrichments("test_rna_seq", "-3,0,3,7", "1,15,31,63").get(0);

    assertThat(result.getpValue().doubleValue()).isCloseTo(0.08712729685483314, within(TOLERANCE));
    assertGroup(result, "group0", 1.25, 1.5);
    assertGroup(result, "group1", 4, 2.160246899469287);
  }

  @Test
  public void excludesMissingValuesAndGroupsWithoutNumericData() {
    List<GenomicEnrichment> results =
        enrichments("test_expression", "1,NA,2,4,", "5,9,NA,10,15", "NA,NA");

    assertThat(results).hasSize(1);
    GenomicEnrichment result = results.get(0);
    assertThat(result.getGroupsStatistics()).hasSize(2);
    assertThat(result.getpValue().doubleValue()).isCloseTo(0.029508154789979552, within(TOLERANCE));
    assertGroup(result, "group0", 2.3333333333333335, 1.5275252316519465);
    assertGroup(result, "group1", 9.75, 4.112987559751022);
  }

  @Test
  public void omitsEnrichmentWhenOnlyOneGroupHasTwoNumericValues() {
    assertThat(enrichments("test_expression", "1,NA", "5,9,10,15")).isEmpty();
  }

  @Test
  public void omitsEnrichmentWhenIdenticalConstantGroupsHaveUndefinedPValue() {
    assertThat(enrichments("test_expression", "3,3", "3,3")).isEmpty();
  }

  private List<GenomicEnrichment> enrichments(String profileId, String... groupValues) {
    MolecularProfile profile = new MolecularProfile();
    profile.setStableId(profileId);
    profile.setCancerStudyIdentifier(STUDY_ID);

    Map<String, List<MolecularProfileCaseIdentifier>> groups = new LinkedHashMap<>();
    List<Sample> samples = new ArrayList<>();
    List<String> orderedInternalIds = new ArrayList<>();
    for (int groupIndex = 0; groupIndex < groupValues.length; groupIndex++) {
      List<MolecularProfileCaseIdentifier> cases = new ArrayList<>();
      for (String value : groupValues[groupIndex].split(",", -1)) {
        int internalId = samples.size() + 1;
        String sampleId = "sample" + internalId;
        Sample sample = new Sample();
        sample.setInternalId(internalId);
        sample.setStableId(sampleId);
        sample.setCancerStudyIdentifier(STUDY_ID);
        samples.add(sample);
        orderedInternalIds.add(Integer.toString(internalId));
        cases.add(new MolecularProfileCaseIdentifier(sampleId, profileId));
      }
      groups.put("group" + groupIndex, cases);
    }

    MolecularProfileSamples profileSamples = new MolecularProfileSamples();
    profileSamples.setMolecularProfileId(profileId);
    profileSamples.setCommaSeparatedSampleIds(String.join(",", orderedInternalIds));
    given(this.molecularDataRepository.getCommaSeparatedSampleIdsOfMolecularProfile(profileId))
        .willReturn(profileSamples);
    // Repository return order need not match the profile's ordered sample list.
    Collections.reverse(samples);
    given(this.sampleService.fetchSamples(anyList(), anyList(), eq("ID"))).willReturn(samples);

    GeneMolecularAlteration alteration = new GeneMolecularAlteration();
    alteration.setMolecularProfileId(profileId);
    alteration.setEntrezGeneId(7157);
    alteration.setValues(String.join(",", groupValues));

    return this.expressionEnrichmentUtil.getEnrichments(
        profile, groups, EnrichmentType.SAMPLE, Collections.singletonList(alteration));
  }

  private void assertGroup(
      GenomicEnrichment result, String name, double mean, double standardDeviation) {
    List<GroupStatistics> matchingGroups =
        result.getGroupsStatistics().stream()
            .filter(group -> group.getName().equals(name))
            .collect(Collectors.toList());
    assertThat(matchingGroups).hasSize(1);
    GroupStatistics group = matchingGroups.get(0);
    assertThat(group.getMeanExpression().doubleValue()).isCloseTo(mean, within(TOLERANCE));
    assertThat(group.getStandardDeviation().doubleValue())
        .isCloseTo(standardDeviation, within(TOLERANCE));
  }
}
