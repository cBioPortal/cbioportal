package org.cbioportal.model.util;

import org.cbioportal.model.AlterationFilter;
import org.cbioportal.model.CNA;
import org.cbioportal.model.MutationEventType;
import org.junit.Assert;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

public class AlterationFilterTest {

    @Test
    public void setSelectAllWhenAllCnaTypesTrue() {
        AlterationFilter f = new AlterationFilter();
        Map<CNA, Boolean> types = new HashMap();
        types.put(CNA.HOMDEL, true);
        types.put(CNA.DIPLOID, true);
        f.setCopyNumberAlterationEventTypes(types);
        Assert.assertFalse(f.getCNAEventTypeSelect().hasValues());
        Assert.assertTrue(f.getCNAEventTypeSelect().hasAll());
        Assert.assertFalse(f.getCNAEventTypeSelect().hasNone());
    }

    @Test
    public void setSelectAllWhenSomeCnaTypesTrue() {
        AlterationFilter f = new AlterationFilter();
        Map<CNA, Boolean> types = new HashMap();
        types.put(CNA.HOMDEL, false);
        types.put(CNA.DIPLOID, true);
        f.setCopyNumberAlterationEventTypes(types);
        Assert.assertTrue(f.getCNAEventTypeSelect().hasValues());
        Assert.assertFalse(f.getCNAEventTypeSelect().hasAll());
        Assert.assertFalse(f.getCNAEventTypeSelect().hasNone());
    }

    @Test
    public void setSelectAllWhenAllCnaTypesFalse() {
        AlterationFilter f = new AlterationFilter();
        Map<CNA, Boolean> types = new HashMap();
        types.put(CNA.HOMDEL, false);
        types.put(CNA.DIPLOID, false);
        f.setCopyNumberAlterationEventTypes(types);
        Assert.assertFalse(f.getCNAEventTypeSelect().hasValues());
        Assert.assertFalse(f.getCNAEventTypeSelect().hasAll());
        Assert.assertTrue(f.getCNAEventTypeSelect().hasNone());
    }

    @Test(expected = IllegalArgumentException.class)
    public void setSelectAllWhenCnaTypesNull() {
        AlterationFilter f = new AlterationFilter();
        f.setCopyNumberAlterationEventTypes(null);
    }

    @Test
    public void setSelectAllWhenCnaTypesEmpty() {
        AlterationFilter f = new AlterationFilter();
        f.setCopyNumberAlterationEventTypes(new HashMap<>());
        Assert.assertFalse(f.getCNAEventTypeSelect().hasValues());
        Assert.assertTrue(f.getCNAEventTypeSelect().hasAll());
        Assert.assertFalse(f.getCNAEventTypeSelect().hasNone());
    }

    @Test
    public void setSelectAllWhenAllMutationTypesTrue() {
        AlterationFilter f = new AlterationFilter();
        Map<MutationEventType, Boolean> types = new HashMap();
        types.put(MutationEventType.feature_truncation, true);
        types.put(MutationEventType.missense_mutation, true);
        f.setMutationEventTypes(types);
        Assert.assertFalse(f.getMutationTypeSelect().hasValues());
        Assert.assertTrue(f.getMutationTypeSelect().hasAll());
        Assert.assertFalse(f.getMutationTypeSelect().hasNone());
    }

    @Test
    public void setSelectAllWhenSomeMutationTypesTrue() {
        AlterationFilter f = new AlterationFilter();
        Map<MutationEventType, Boolean> types = new HashMap();
        types.put(MutationEventType.feature_truncation, false);
        types.put(MutationEventType.missense_mutation, true);
        f.setMutationEventTypes(types);
        Assert.assertTrue(f.getMutationTypeSelect().hasValues());
        Assert.assertFalse(f.getMutationTypeSelect().hasAll());
        Assert.assertFalse(f.getMutationTypeSelect().hasNone());
    }

    @Test
    public void setSelectAllWhenAllMutationTypesFalse() {
        AlterationFilter f = new AlterationFilter();
        Map<MutationEventType, Boolean> types = new HashMap();
        types.put(MutationEventType.feature_truncation, false);
        types.put(MutationEventType.missense_mutation, false);
        f.setMutationEventTypes(types);
        Assert.assertFalse(f.getMutationTypeSelect().hasValues());
        Assert.assertFalse(f.getMutationTypeSelect().hasAll());
        Assert.assertTrue(f.getMutationTypeSelect().hasNone());
    }

    @Test(expected = IllegalArgumentException.class)
    public void setSelectAllWhenMutationTypesNull() {
        AlterationFilter f = new AlterationFilter();
        f.setMutationEventTypes(null);
    }

    @Test
    public void setSelectAllWhenMutationTypesEmpty() {
        AlterationFilter f = new AlterationFilter();
        f.setMutationEventTypes(new HashMap<>());
        Assert.assertFalse(f.getMutationTypeSelect().hasValues());
        Assert.assertTrue(f.getMutationTypeSelect().hasAll());
        Assert.assertFalse(f.getMutationTypeSelect().hasNone());
    }

}