package org.cbioportal.service.util;

import org.cbioportal.model.ClinicalAttribute;
import org.cbioportal.model.ClinicalDataCount;
import org.cbioportal.model.ClinicalDataCountItem;
import org.cbioportal.model.Sample;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
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
    
    
    
    
    
    
}