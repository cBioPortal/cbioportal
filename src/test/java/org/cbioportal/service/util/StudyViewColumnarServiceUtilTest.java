package org.cbioportal.service.util;

import org.cbioportal.model.ClinicalDataCount;
import org.cbioportal.model.ClinicalDataCountItem;
import org.cbioportal.model.Sample;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

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

}