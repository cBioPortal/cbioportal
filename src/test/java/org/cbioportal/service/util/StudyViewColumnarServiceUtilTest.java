package org.cbioportal.service.util;

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
    public void testAddClinicalDataCountsForMissingAttributes() {
        // Prepare the input data
        ClinicalDataCountItem existingItem = new ClinicalDataCountItem();
        existingItem.setAttributeId("existingAttr");
        ClinicalDataCount existingCount = new ClinicalDataCount();
        existingCount.setCount(1);
        existingCount.setValue("value1");
        existingItem.setCounts(Collections.singletonList(existingCount));
        List<ClinicalDataCountItem> result = Collections.singletonList(existingItem);

        List<String> filteredAttributes = Arrays.asList("existingAttr","attr1", "attr2");
        List<Sample> filteredSamples = Arrays.asList(new Sample(), new Sample());

        // Call the method under test
        List<ClinicalDataCountItem> updatedResult = StudyViewColumnarServiceUtil.addClinicalDataCountsForMissingAttributes(result, filteredAttributes, filteredSamples);

        // code adds missing attributes from filteredAttributes
        Assert.assertEquals(3, updatedResult.size());

        ClinicalDataCountItem item1 = updatedResult.get(0);
        Assert.assertEquals("existingAttr", item1.getAttributeId());
        Assert.assertEquals(1, item1.getCounts().size());
        Assert.assertEquals("value1", item1.getCounts().get(0).getValue());

        // the added attributes (they were filtered out)
        // have counts for NA value equal to the total sample count
        ClinicalDataCountItem item2 = updatedResult.get(1);
        Assert.assertEquals("attr1", item2.getAttributeId());
        Assert.assertEquals(1, item2.getCounts().size());
        Assert.assertEquals("NA", item2.getCounts().get(0).getValue());
        Assert.assertEquals(2, item2.getCounts().get(0).getCount().intValue());

        // ditto the third item
        ClinicalDataCountItem item3 = updatedResult.get(2);
        Assert.assertEquals("attr2", item3.getAttributeId());
        Assert.assertEquals(1, item3.getCounts().size());
        Assert.assertEquals("NA", item3.getCounts().get(0).getValue());
        Assert.assertEquals(2, item3.getCounts().get(0).getCount().intValue());
    }

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
    
}