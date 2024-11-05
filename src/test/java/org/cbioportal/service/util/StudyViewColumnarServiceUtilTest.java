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