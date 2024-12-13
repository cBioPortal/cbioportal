package org.cbioportal.service.util;

import org.cbioportal.model.CaseListDataCount;
import org.cbioportal.model.ClinicalAttribute;
import org.cbioportal.model.ClinicalDataCount;
import org.cbioportal.model.ClinicalDataCountItem;
import org.cbioportal.model.GenomicDataCount;
import org.cbioportal.model.GenomicDataCountItem;
import org.cbioportal.web.parameter.GenomicDataFilter;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.Assert.*;

public class StudyViewColumnarServiceUtilTest {
    
    @Test
    public void testMergeClinicalDataCounts() {
       
        // first two counts are for same value (value1) and so should be 
        // merged
        
        ClinicalDataCount count1 = new ClinicalDataCount();
        count1.setAttributeId("attr1");
        count1.setValue("value1");
        count1.setCount(1);

        ClinicalDataCount count2 = new ClinicalDataCount();
        count2.setAttributeId("attr1");
        count2.setValue("value1");
        count2.setCount(2);

        ClinicalDataCount count3 = new ClinicalDataCount();
        count3.setAttributeId("attr1");
        count3.setValue("value3");
        count3.setCount(6);

        ClinicalDataCount count4 = new ClinicalDataCount();
        count4.setAttributeId("attr1");
        count4.setValue("value3");
        count4.setCount(4);

        ClinicalDataCount count5 = new ClinicalDataCount();
        count5.setAttributeId("attr1");
        count5.setValue("value2");
        count5.setCount(4);

        ClinicalDataCountItem item = new ClinicalDataCountItem();
        item.setAttributeId("attr1");
        item.setCounts(Arrays.asList(count1, count2, count3, count4, count5));

        List<ClinicalDataCountItem> items = Collections.singletonList(item);

        // Call the method under test
        List<ClinicalDataCountItem> mergedItems = StudyViewColumnarServiceUtil.mergeClinicalDataCounts(items);

        // it merged three count items to 2
        Optional<ClinicalDataCount> mergedCount=mergedItems.get(0).getCounts().stream()
            .filter(count->count.getValue().equals("value1")).findFirst();
        Assert.assertEquals(3, mergedCount.get().getCount().intValue());
        
        Optional<ClinicalDataCount> mergedCount2=mergedItems.get(0).getCounts().stream()
            .filter(count->count.getValue().equals("value2")).findFirst();
        Assert.assertEquals(4, mergedCount2.get().getCount().intValue());
        
        Optional<ClinicalDataCount> mergedCount3=mergedItems.get(0).getCounts().stream()
            .filter(count->count.getValue().equals("value3")).findFirst();
        Assert.assertEquals(10, mergedCount3.get().getCount().intValue());
        
    }


    @Test
    public void testAddClinicalDataCountsForMissingAttributes() {
        ClinicalDataCountItem existingItem = new ClinicalDataCountItem();
        existingItem.setAttributeId("attr1");
        ClinicalDataCount existingCount = new ClinicalDataCount();
        existingCount.setCount(5);
        existingCount.setValue("value1");
        existingCount.setAttributeId("attr1");
        existingItem.setCounts(Collections.singletonList(existingCount));

        List<ClinicalDataCountItem> counts = Collections.singletonList(existingItem);

        // we're gonna create two attributes which will not be represented in the passed result set
        // test whether addClinicalDataCountsForMissingAttributes restores them
        
        ClinicalAttribute missingAttributeSample = new ClinicalAttribute();
        missingAttributeSample.setAttrId("attr2");
        missingAttributeSample.setPatientAttribute(false);
        
        ClinicalAttribute missingAttributePatient = new ClinicalAttribute();
        missingAttributePatient.setAttrId("attr3");
        missingAttributePatient.setPatientAttribute(true);

        List<ClinicalAttribute> attributes = Arrays.asList(missingAttributeSample, missingAttributePatient);

        List<ClinicalDataCountItem> result = StudyViewColumnarServiceUtil.addClinicalDataCountsForMissingAttributes(
            counts, attributes, 10, 20
        );

        assertEquals(3, result.size());

        Optional<ClinicalDataCountItem> addedItemSample = result.stream()
            .filter(item -> item.getAttributeId().equals("attr2"))
            .findFirst();

        assertTrue(addedItemSample.isPresent());
        assertEquals(1, addedItemSample.get().getCounts().size());
        assertEquals("NA", addedItemSample.get().getCounts().get(0).getValue());
        assertEquals(10, addedItemSample.get().getCounts().get(0).getCount().intValue());

        Optional<ClinicalDataCountItem> addedItemPatient = result.stream()
            .filter(item -> item.getAttributeId().equals("attr3"))
            .findFirst();

        assertTrue(addedItemPatient.isPresent());
        assertEquals(1, addedItemPatient.get().getCounts().size());
        assertEquals("NA", addedItemPatient.get().getCounts().get(0).getValue());
        assertEquals(20, addedItemPatient.get().getCounts().get(0).getCount().intValue());
        

    }


    @Test
    public void testMergeGenomicDataCounts() {
        GenomicDataCount count1 = new GenomicDataCount();
        count1.setValue("value1");
        count1.setLabel("label1");
        count1.setCount(1);

        GenomicDataCount count2 = new GenomicDataCount();
        count2.setValue("value1");
        count2.setLabel("label1");
        count2.setCount(2);

        GenomicDataCount count3 = new GenomicDataCount();
        count3.setValue("value2");
        count3.setLabel("label2");
        count3.setCount(3);

        List<GenomicDataCount> counts = Arrays.asList(count1, count2, count3);

        List<GenomicDataCount> mergedCounts = StudyViewColumnarServiceUtil.mergeGenomicDataCounts(counts);

        assertEquals(2, mergedCounts.size());

        GenomicDataCount mergedCount1 = mergedCounts.stream()
            .filter(count -> count.getValue().equals("value1"))
            .findFirst()
            .orElse(null);
        assertEquals(3, mergedCount1.getCount().intValue());
        assertEquals("label1", mergedCount1.getLabel());

        GenomicDataCount mergedCount2 = mergedCounts.stream()
            .filter(count -> count.getValue().equals("value2"))
            .findFirst()
            .orElse(null);
        assertEquals(3, mergedCount2.getCount().intValue());
        assertEquals("label2", mergedCount2.getLabel());
    }


    @Test
    public void testMergeCaseListCounts() {
        CaseListDataCount count1 = new CaseListDataCount();
        count1.setValue("value1");
        count1.setLabel("label1");
        count1.setCount(1);

        CaseListDataCount count2 = new CaseListDataCount();
        count2.setValue("value1");
        count2.setLabel("label1");
        count2.setCount(2);

        CaseListDataCount count3 = new CaseListDataCount();
        count3.setValue("value2");
        count3.setLabel("label2");
        count3.setCount(3);

        List<CaseListDataCount> counts = Arrays.asList(count1, count2, count3);

        List<CaseListDataCount> mergedCounts = StudyViewColumnarServiceUtil.mergeCaseListCounts(counts);

        assertEquals(2, mergedCounts.size());

        CaseListDataCount mergedCount1 = mergedCounts.stream()
            .filter(count -> count.getValue().equals("value1"))
            .findFirst()
            .orElse(null);
        assertEquals(3, mergedCount1.getCount().intValue());
        assertEquals("label1", mergedCount1.getLabel());

        CaseListDataCount mergedCount2 = mergedCounts.stream()
            .filter(count -> count.getValue().equals("value2"))
            .findFirst()
            .orElse(null);
        assertEquals(3, mergedCount2.getCount().intValue());
        assertEquals("label2", mergedCount2.getLabel());
    }


    @Test
    public void testNormalizeDataCounts() {
        ClinicalDataCount count1 = new ClinicalDataCount();
        count1.setAttributeId("attr1");
        count1.setValue("TRUE");
        count1.setCount(1);

        ClinicalDataCount count2 = new ClinicalDataCount();
        count2.setAttributeId("attr1");
        count2.setValue("True");
        count2.setCount(2);

        ClinicalDataCount count3 = new ClinicalDataCount();
        count3.setAttributeId("attr1");
        count3.setValue("true");
        count3.setCount(3);

        ClinicalDataCount count4 = new ClinicalDataCount();
        count4.setAttributeId("attr1");
        count4.setValue("FALSE");
        count4.setCount(4);

        ClinicalDataCount count5 = new ClinicalDataCount();
        count5.setAttributeId("attr1");
        count5.setValue("False");
        count5.setCount(5);

        List<ClinicalDataCount> dataCounts = Arrays.asList(count1, count2, count3, count4, count5);

        List<ClinicalDataCount> normalizedDataCounts = StudyViewColumnarServiceUtil.normalizeDataCounts(dataCounts);

        assertEquals(2, normalizedDataCounts.size());

        // should be null because it prioritizes lower case over upper case
        ClinicalDataCount trueCountNullCheck = normalizedDataCounts.stream()
            .filter(count -> count.getValue().equals("True"))
            .findFirst()
            .orElse(null);
        assertEquals(null, trueCountNullCheck);
        
        ClinicalDataCount trueCount = normalizedDataCounts.stream()
            .filter(count -> count.getValue().equals("true"))
            .findFirst()
            .orElse(null);
        assertEquals(6, trueCount.getCount().intValue());

        // should be null because it prioritizes lower case over upper case
        ClinicalDataCount falseCountNullCheck = normalizedDataCounts.stream()
            .filter(count -> count.getValue().equals("FALSE"))
            .findFirst()
            .orElse(null);
        assertEquals(null, falseCountNullCheck);
        
        ClinicalDataCount falseCount = normalizedDataCounts.stream()
            .filter(count -> count.getValue().equals("False"))
            .findFirst()
            .orElse(null);
        assertEquals(9, falseCount.getCount().intValue());
    }


    @Test
    public void testCreateGenomicDataCountItemFromMutationCounts() {
        GenomicDataFilter genomicDataFilter = new GenomicDataFilter();
        genomicDataFilter.setHugoGeneSymbol("hugo1");

        Map<String, Integer> counts1 = Map.of(
            "mutatedCount", 5,
            "notMutatedCount", 10,
            "notProfiledCount", 15
        );

        GenomicDataCountItem item1 = StudyViewColumnarServiceUtil.createGenomicDataCountItemFromMutationCounts(genomicDataFilter, counts1);

        assertEquals("hugo1", item1.getHugoGeneSymbol());
        assertEquals("mutations", item1.getProfileType());

        assertEquals(3, item1.getCounts().size());

        GenomicDataCount mutatedCount1 = item1.getCounts().stream()
            .filter(count -> count.getValue().equals("MUTATED"))
            .findFirst()
            .orElse(null);
        assertNotNull(mutatedCount1);
        assertEquals(5, mutatedCount1.getCount().intValue());

        GenomicDataCount notMutatedCount1 = item1.getCounts().stream()
            .filter(count -> count.getValue().equals("NOT_MUTATED"))
            .findFirst()
            .orElse(null);
        assertNotNull(notMutatedCount1);
        assertEquals(10, notMutatedCount1.getCount().intValue());

        GenomicDataCount notProfiledCount1 = item1.getCounts().stream()
            .filter(count -> count.getValue().equals("NOT_PROFILED"))
            .findFirst()
            .orElse(null);
        assertNotNull(notProfiledCount1);
        assertEquals(15, notProfiledCount1.getCount().intValue());

        // Test case where a count equals 0
        Map<String, Integer> counts2 = Map.of(
            "mutatedCount", 5,
            "notMutatedCount", 0,
            "notProfiledCount", 5
        );

        GenomicDataCountItem item2 = StudyViewColumnarServiceUtil.createGenomicDataCountItemFromMutationCounts(genomicDataFilter, counts2);

        assertEquals("hugo1", item2.getHugoGeneSymbol());
        assertEquals("mutations", item2.getProfileType());

        assertEquals(2, item2.getCounts().size());

        GenomicDataCount mutatedCount2 = item2.getCounts().stream()
            .filter(count -> count.getValue().equals("MUTATED"))
            .findFirst()
            .orElse(null);
        assertNotNull(mutatedCount2);
        assertEquals(5, mutatedCount2.getCount().intValue());

        GenomicDataCount notMutatedCount2 = item2.getCounts().stream()
            .filter(count -> count.getValue().equals("NOT_MUTATED"))
            .findFirst()
            .orElse(null);
        assertNull(notMutatedCount2);

        GenomicDataCount notProfiledCount2 = item2.getCounts().stream()
            .filter(count -> count.getValue().equals("NOT_PROFILED"))
            .findFirst()
            .orElse(null);
        assertNotNull(notProfiledCount2);
        assertEquals(5, notProfiledCount2.getCount().intValue());
    }
    
    
}