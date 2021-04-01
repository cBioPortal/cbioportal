package org.cbioportal.model.util;

import org.apache.commons.collections4.map.HashedMap;
import org.cbioportal.model.AlterationFilter;
import org.cbioportal.model.BaseAlterationFilter;
import org.cbioportal.model.CNA;
import org.cbioportal.model.MutationEventType;
import org.junit.Assert;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

public class AlterationFiltersTest {

    @Test
    public void testAlterationFilterWithEmptyConstructor() {
        AlterationFilter f = new AlterationFilter();
        Assert.assertTrue(f.getSelectedMutationTypes().hasAll());
        Assert.assertTrue(f.getSelectedCnaTypes().hasAll());
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
    public void setSelectAllWhenAllCnaTypesTrue() {
        AlterationFilter f = new AlterationFilter();
        Map<CNA, Boolean> types = new HashedMap();
        types.put(CNA.HOMDEL, true);
        types.put(CNA.DIPLOID, true);
        f.setCnaBooleanMap(types);
        Assert.assertTrue(f.getSelectedCnaTypes().hasValues());
        Assert.assertTrue(f.getSelectedCnaTypes().hasAll());
        Assert.assertFalse(f.getSelectedCnaTypes().hasNone());
    }

    @Test
    public void setSelectAllWhenSomeCnaTypesTrue() {
        AlterationFilter f = new AlterationFilter();
        Map<CNA, Boolean> types = new HashedMap();
        types.put(CNA.HOMDEL, false);
        types.put(CNA.DIPLOID, true);
        f.setCnaBooleanMap(types);
        Assert.assertTrue(f.getSelectedCnaTypes().hasValues());
        Assert.assertFalse(f.getSelectedCnaTypes().hasAll());
        Assert.assertFalse(f.getSelectedCnaTypes().hasNone());
    }

    @Test
    public void setSelectAllWhenAllCnaTypesFalse() {
        AlterationFilter f = new AlterationFilter();
        Map<CNA, Boolean> types = new HashedMap();
        types.put(CNA.HOMDEL, false);
        types.put(CNA.DIPLOID, false);
        f.setCnaBooleanMap(types);
        Assert.assertFalse(f.getSelectedCnaTypes().hasValues());
        Assert.assertFalse(f.getSelectedCnaTypes().hasAll());
        Assert.assertTrue(f.getSelectedCnaTypes().hasNone());
    }

    @Test
    public void setSelectAllWhenCnaTypesNull() {
        AlterationFilter f = new AlterationFilter();
        f.setCnaBooleanMap(null);
        Assert.assertFalse(f.getSelectedCnaTypes().hasValues());
        Assert.assertTrue(f.getSelectedCnaTypes().hasAll());
        Assert.assertFalse(f.getSelectedCnaTypes().hasNone());
    }

    @Test
    public void setSelectAllWhenCnaTypesEmpty() {
        AlterationFilter f = new AlterationFilter();
        f.setCnaBooleanMap(new HashMap<>());
        Assert.assertFalse(f.getSelectedCnaTypes().hasValues());
        Assert.assertFalse(f.getSelectedCnaTypes().hasAll());
        Assert.assertTrue(f.getSelectedCnaTypes().hasNone());
    }

    @Test
    public void setSelectAllWhenAllMutationTypesTrue() {
        AlterationFilter f = new AlterationFilter();
        Map<MutationEventType, Boolean> types = new HashedMap();
        types.put(MutationEventType.feature_truncation, true);
        types.put(MutationEventType.missense_mutation, true);
        f.setMutationBooleanMap(types);
        Assert.assertTrue(f.getSelectedMutationTypes().hasValues());
        Assert.assertTrue(f.getSelectedMutationTypes().hasAll());
        Assert.assertFalse(f.getSelectedMutationTypes().hasNone());
    }

    @Test
    public void setSelectAllWhenSomeMutationTypesTrue() {
        AlterationFilter f = new AlterationFilter();
        Map<MutationEventType, Boolean> types = new HashedMap();
        types.put(MutationEventType.feature_truncation, false);
        types.put(MutationEventType.missense_mutation, true);
        f.setMutationBooleanMap(types);
        Assert.assertTrue(f.getSelectedMutationTypes().hasValues());
        Assert.assertFalse(f.getSelectedMutationTypes().hasAll());
        Assert.assertFalse(f.getSelectedMutationTypes().hasNone());
    }

    @Test
    public void setSelectAllWhenAllMutationTypesFalse() {
        AlterationFilter f = new AlterationFilter();
        Map<MutationEventType, Boolean> types = new HashedMap();
        types.put(MutationEventType.feature_truncation, false);
        types.put(MutationEventType.missense_mutation, false);
        f.setMutationBooleanMap(types);
        Assert.assertFalse(f.getSelectedMutationTypes().hasValues());
        Assert.assertFalse(f.getSelectedMutationTypes().hasAll());
        Assert.assertTrue(f.getSelectedMutationTypes().hasNone());
    }

    @Test
    public void setSelectAllWhenMutationTypesNull() {
        AlterationFilter f = new AlterationFilter();
        f.setMutationBooleanMap(null);
        Assert.assertFalse(f.getSelectedMutationTypes().hasValues());
        Assert.assertTrue(f.getSelectedMutationTypes().hasAll());
        Assert.assertFalse(f.getSelectedMutationTypes().hasNone());
    }

    @Test
    public void setSelectAllWhenMutationTypesEmpty() {
        AlterationFilter f = new AlterationFilter();
        f.setMutationBooleanMap(new HashMap<>());
        Assert.assertFalse(f.getSelectedMutationTypes().hasValues());
        Assert.assertFalse(f.getSelectedMutationTypes().hasAll());
        Assert.assertTrue(f.getSelectedMutationTypes().hasNone());
    }

    @Test
    public void setSelectAllWhenAllTiersTrue() {
        BaseAlterationFilter f = new BaseAlterationFilter();
        Map<String, Boolean> types = new HashedMap();
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
        Map<String, Boolean> types = new HashedMap();
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
        Map<String, Boolean> types = new HashedMap();
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
        f.setTiersBooleanMap(new HashedMap());
        Assert.assertFalse(f.getSelectedTiers().hasValues());
        Assert.assertFalse(f.getSelectedTiers().hasAll());
        Assert.assertTrue(f.getSelectedTiers().hasNone());
    }

    @Test
    public void setSelectAllWhenNullTiers() {
        BaseAlterationFilter f = new BaseAlterationFilter();
        f.setTiersBooleanMap(null);
        Assert.assertFalse(f.getSelectedTiers().hasValues());
        Assert.assertTrue(f.getSelectedTiers().hasAll());
        Assert.assertFalse(f.getSelectedTiers().hasNone());
    }
    
}