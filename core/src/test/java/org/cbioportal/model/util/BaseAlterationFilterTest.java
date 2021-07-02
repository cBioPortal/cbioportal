package org.cbioportal.model.util;

import org.cbioportal.model.BaseAlterationFilter;
import org.junit.Assert;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

public class BaseAlterationFilterTest {

    @Test
    public void testAlterationFilterWithEmptyConstructor() {
        BaseAlterationFilter f = new BaseAlterationFilter();
        Assert.assertTrue(f.getIncludeDriver());
        Assert.assertTrue(f.getIncludeVUS());
        Assert.assertTrue(f.getIncludeUnknownOncogenicity());
        Assert.assertTrue(f.getIncludeGermline());
        Assert.assertTrue(f.getIncludeGermline());
        Assert.assertTrue(f.getIncludeUnknownStatus());
        Assert.assertTrue(f.getIncludeUnknownTier());
        Assert.assertTrue(f.getSelectedTiers().hasAll());
    }

    @Test
    public void testMutationStatusFilterWithEmptyConstructor() {
        BaseAlterationFilter f = new BaseAlterationFilter();
        Assert.assertTrue(f.getIncludeGermline());
        Assert.assertTrue(f.getIncludeUnknownStatus());
        Assert.assertTrue(f.getIncludeUnknownTier());
        Assert.assertTrue(f.getSelectedTiers().hasAll());
        Assert.assertTrue(f.getIncludeDriver());
        Assert.assertTrue(f.getIncludeVUS());
        Assert.assertTrue(f.getIncludeUnknownOncogenicity());
        Assert.assertTrue(f.getIncludeUnknownTier());
        Assert.assertTrue(f.getSelectedTiers().hasAll());
    }
    
    @Test
    public void setSelectAllWhenAllTiersTrue() {
        BaseAlterationFilter f = new BaseAlterationFilter();
        Map<String, Boolean> types = new HashMap();
        types.put("Class 1", true);
        types.put("Class 2", true);
        f.setTiersBooleanMap(types);
        Assert.assertTrue(f.getSelectedTiers().hasValues());
        Assert.assertTrue(f.getSelectedTiers().hasAll());
        Assert.assertFalse(f.getSelectedTiers().hasNone());
    }

    @Test
    public void setSelectAllWhenSomeTiersTrue() {
        BaseAlterationFilter f = new BaseAlterationFilter();
        Map<String, Boolean> types = new HashMap();
        types.put("Class 1", false);
        types.put("Class 2", true);
        f.setTiersBooleanMap(types);
        Assert.assertTrue(f.getSelectedTiers().hasValues());
        Assert.assertFalse(f.getSelectedTiers().hasAll());
        Assert.assertFalse(f.getSelectedTiers().hasNone());
    }

    @Test
    public void setSelectAllWhenAllTiersFalse() {
        BaseAlterationFilter f = new BaseAlterationFilter();
        Map<String, Boolean> types = new HashMap();
        types.put("Class 1", false);
        types.put("Class 2", false);
        f.setTiersBooleanMap(types);
        Assert.assertFalse(f.getSelectedTiers().hasValues());
        Assert.assertFalse(f.getSelectedTiers().hasAll());
        Assert.assertTrue(f.getSelectedTiers().hasNone());
    }

    @Test
    public void setSelectAllWhenTiersEmpty() {
        BaseAlterationFilter f = new BaseAlterationFilter();
        f.setTiersBooleanMap(new HashMap());
        Assert.assertFalse(f.getSelectedTiers().hasValues());
        Assert.assertTrue(f.getSelectedTiers().hasAll());
        Assert.assertFalse(f.getSelectedTiers().hasNone());
    }

    @Test
    public void setSelectNoneWhenNullTiers() {
        BaseAlterationFilter f = new BaseAlterationFilter();
        f.setTiersBooleanMap(null);
        Assert.assertFalse(f.getSelectedTiers().hasValues());
        Assert.assertFalse(f.getSelectedTiers().hasAll());
        Assert.assertTrue(f.getSelectedTiers().hasNone());
    }
    
}