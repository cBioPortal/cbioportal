package org.cbioportal.legacy.service.impl;

import static org.mockito.ArgumentMatchers.any;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.cbioportal.legacy.model.NamespaceAttribute;
import org.cbioportal.legacy.model.NamespaceAttributeCount;
import org.cbioportal.legacy.persistence.NamespaceRepository;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class NamespaceAttributeServiceImplTest extends BaseServiceImplTest {

  @InjectMocks private NamespaceAttributeServiceImpl namespaceAttributeService;

  @Mock private NamespaceRepository namespaceRepository;

  @Test
  public void fetchNamespaceAttributes() {

    List<NamespaceAttribute> expectedNamespaceAttributeList = new ArrayList<>();
    NamespaceAttribute namespaceAttribute =
        new NamespaceAttribute(NAMESPACE_OUTER_KEY_1, NAMESPACE_INNER_KEY_1);
    expectedNamespaceAttributeList.add(namespaceAttribute);

    Mockito.when(namespaceRepository.getNamespaceOuterKey(Arrays.asList(STUDY_ID)))
        .thenReturn(expectedNamespaceAttributeList);

    Mockito.when(namespaceRepository.getNamespaceInnerKey(any(), any()))
        .thenReturn(expectedNamespaceAttributeList);

    List<NamespaceAttribute> result =
        namespaceAttributeService.fetchNamespaceAttributes(Arrays.asList(STUDY_ID));

    Assert.assertEquals(expectedNamespaceAttributeList, result);
  }

  @Test
  public void getNamespaceAttributeCountsBySampleIds() {

    List<NamespaceAttribute> namespaceAttributes = new ArrayList<>();
    NamespaceAttribute namespaceAttribute =
        new NamespaceAttribute(NAMESPACE_OUTER_KEY_1, NAMESPACE_INNER_KEY_1);
    List<String> sampleIds = new ArrayList<>();
    List<String> studyIds = new ArrayList<>();
    sampleIds.add(SAMPLE_ID1);
    studyIds.add(STUDY_ID);
    namespaceAttributes.add(namespaceAttribute);

    List<NamespaceAttributeCount> expectedNamespaceAttributeCounts = new ArrayList<>();
    NamespaceAttributeCount namespaceAttributeCount = new NamespaceAttributeCount();
    expectedNamespaceAttributeCounts.add(namespaceAttributeCount);

    Mockito.when(
            namespaceRepository.getNamespaceAttributeCountsBySampleIds(
                sampleIds, studyIds, namespaceAttributes))
        .thenReturn(expectedNamespaceAttributeCounts);

    List<NamespaceAttributeCount> result =
        namespaceAttributeService.fetchNamespaceAttributeCountsBySampleIds(
            sampleIds, studyIds, namespaceAttributes);

    Assert.assertEquals(expectedNamespaceAttributeCounts, result);
  }
}
