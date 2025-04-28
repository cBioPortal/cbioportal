package org.cbioportal.legacy.service.impl;

import org.cbioportal.legacy.model.NamespaceAttribute;
import org.cbioportal.legacy.model.NamespaceData;
import org.cbioportal.legacy.model.NamespaceDataCount;
import org.cbioportal.legacy.model.NamespaceDataCountItem;
import org.cbioportal.legacy.persistence.NamespaceRepository;
import org.cbioportal.legacy.web.parameter.NamespaceDataFilter;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.*;

import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class NamespaceDataServiceImplTest extends BaseServiceImplTest {

    @InjectMocks
    private NamespaceDataServiceImpl namespaceDataService;

    @Mock
    private NamespaceRepository namespaceRepository;

    @Test
    public void fetchNamespaceData() {

        List<NamespaceDataFilter> namespaceDataFilters = new ArrayList<>();
        NamespaceDataFilter namespaceDataFilter = new NamespaceDataFilter();
        namespaceDataFilter.setOuterKey(NAMESPACE_OUTER_KEY_1);
        namespaceDataFilter.setInnerKey(NAMESPACE_OUTER_KEY_1);
        namespaceDataFilters.add(namespaceDataFilter);
        List<NamespaceData> expectedNamespaceDataList = new ArrayList<>();
        NamespaceData namespaceData = new NamespaceData();
        expectedNamespaceDataList.add(namespaceData);

        when(namespaceRepository.getNamespaceData(Arrays.asList(STUDY_ID),
            Arrays.asList(SAMPLE_ID1, SAMPLE_ID2, SAMPLE_ID3), namespaceDataFilter.getOuterKey(),
            namespaceDataFilter.getInnerKey()))
            .thenReturn(expectedNamespaceDataList);

        List<NamespaceData> result = namespaceDataService.fetchNamespaceData(Arrays.asList(STUDY_ID),
            Arrays.asList(SAMPLE_ID1, SAMPLE_ID2, SAMPLE_ID3), namespaceDataFilters);

        Assert.assertEquals(expectedNamespaceDataList, result);
    }

    @Test
    public void fetchNamespaceDataForComparison() {

        NamespaceAttribute namespaceAttribute = new NamespaceAttribute();
        namespaceAttribute.setOuterKey(NAMESPACE_OUTER_KEY_1);
        namespaceAttribute.setInnerKey(NAMESPACE_OUTER_KEY_1);
        List<NamespaceData> expectedNamespaceDataList = new ArrayList<>();
        NamespaceData namespaceData = new NamespaceData();
        expectedNamespaceDataList.add(namespaceData);

        when(namespaceRepository.getNamespaceDataForComparison(Arrays.asList(STUDY_ID),
            Arrays.asList(SAMPLE_ID1, SAMPLE_ID2, SAMPLE_ID3), namespaceAttribute.getOuterKey(),
            namespaceAttribute.getInnerKey(), CATEGORY_VALUE_1))
            .thenReturn(expectedNamespaceDataList);

        List<NamespaceData> result = namespaceDataService.fetchNamespaceDataForComparison(Arrays.asList(STUDY_ID),
            Arrays.asList(SAMPLE_ID1, SAMPLE_ID2, SAMPLE_ID3), namespaceAttribute, Arrays.asList(CATEGORY_VALUE_1));

        Assert.assertEquals(expectedNamespaceDataList, result);
    }

    @Test
    public void fetchNamespaceDataCounts() {

        NamespaceAttribute namespaceAttribute1 = new NamespaceAttribute();
        namespaceAttribute1.setOuterKey(NAMESPACE_OUTER_KEY_1);
        namespaceAttribute1.setInnerKey(NAMESPACE_INNER_KEY_1);
        NamespaceDataCount namespaceDataCount1 = new NamespaceDataCount();
        namespaceDataCount1.setValue("value1");
        namespaceDataCount1.setCount(1);
        namespaceDataCount1.setTotalCount(5);
        NamespaceDataCount namespaceDataCount2 = new NamespaceDataCount();
        namespaceDataCount2.setValue("value2");
        namespaceDataCount2.setCount(3);
        namespaceDataCount2.setTotalCount(6);

        when(namespaceRepository.getNamespaceDataCounts(Arrays.asList(STUDY_ID, STUDY_ID, STUDY_ID),
            Arrays.asList(SAMPLE_ID1, SAMPLE_ID2, SAMPLE_ID3), NAMESPACE_OUTER_KEY_1, NAMESPACE_INNER_KEY_1))
            .thenReturn(Arrays.asList(namespaceDataCount1, namespaceDataCount2));

        List<NamespaceDataCountItem> result = namespaceDataService.fetchNamespaceDataCounts(Arrays.asList(STUDY_ID, STUDY_ID, STUDY_ID),
            Arrays.asList(SAMPLE_ID1, SAMPLE_ID2, SAMPLE_ID3), Arrays.asList(namespaceAttribute1));

        Assert.assertEquals(1, result.size());
        NamespaceDataCountItem counts1 = result.get(0);
        Assert.assertEquals(NAMESPACE_OUTER_KEY_1, counts1.getOuterKey());
        Assert.assertEquals(NAMESPACE_INNER_KEY_1, counts1.getInnerKey());
        List<NamespaceDataCount> namespaceDataCounts = counts1.getCounts();
        Assert.assertEquals(2, namespaceDataCounts.size());
        NamespaceDataCount count1 = namespaceDataCounts.get(0);
        Assert.assertEquals("value1", count1.getValue());
        Assert.assertEquals((Integer) 1, count1.getCount());
        Assert.assertEquals((Integer) 5, count1.getTotalCount());
        NamespaceDataCount count2 = namespaceDataCounts.get(1);
        Assert.assertEquals("value2", count2.getValue());
        Assert.assertEquals((Integer) 3, count2.getCount());
        Assert.assertEquals((Integer) 6, count2.getTotalCount());
    }
}
