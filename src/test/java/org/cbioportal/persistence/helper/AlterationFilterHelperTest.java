package org.cbioportal.persistence.helper;

import org.cbioportal.model.AlterationFilter;
import org.cbioportal.model.MutationEventType;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class AlterationFilterHelperTest {

    @Test
    public void build() {
        assertNotNull(AlterationFilterHelper.build(null));
    }

    @Test
    public void getMutationTypeList() {
        // Create AlterationFilter
        AlterationFilter alterationFilter = new AlterationFilter();
        Map<MutationEventType, Boolean> mutationEventTypeFilterMap = new HashMap<>();
        mutationEventTypeFilterMap.put(MutationEventType.nonsense_mutation, Boolean.TRUE);
        mutationEventTypeFilterMap.put(MutationEventType.other, Boolean.FALSE);
        alterationFilter.setMutationEventTypes(mutationEventTypeFilterMap);
        
        AlterationFilterHelper helper = AlterationFilterHelper.build(alterationFilter);
        var mutationList = helper.getMutationTypeList();
        assertFalse(mutationList.hasNone());
        assertFalse(mutationList.hasAll());
        assertTrue(mutationList.hasValues());

    }

    @Test
    public void hasDriver() {
        AlterationFilter alterationFilter = new AlterationFilter();
        alterationFilter.setIncludeDriver(true);
        assertTrue(AlterationFilterHelper.build(alterationFilter).hasDriver());
    }

    @Test
    public void hasVUSDriver() {
        AlterationFilter alterationFilter = new AlterationFilter();
        alterationFilter.setIncludeVUS(true);
        assertTrue(AlterationFilterHelper.build(alterationFilter).hasVUSDriver());
    }

    @Test
    public void hasUnknownOncogenicity() {
        AlterationFilter alterationFilter = new AlterationFilter();
        alterationFilter.setIncludeUnknownOncogenicity(true);
        assertTrue(AlterationFilterHelper.build(alterationFilter).hasUnknownOncogenicity());
    }

    @Test
    public void hasGermline() {
        AlterationFilter alterationFilter = new AlterationFilter();
        alterationFilter.setIncludeGermline(true);
        assertTrue(AlterationFilterHelper.build(alterationFilter).hasGermline());
    }

    @Test
    public void hasSomatic() {
        AlterationFilter alterationFilter = new AlterationFilter();
        alterationFilter.setIncludeSomatic(true);
        assertTrue(AlterationFilterHelper.build(alterationFilter).hasSomatic());
    }

    @Test
    public void hasUnknownMutationStatus() {
        AlterationFilter alterationFilter = new AlterationFilter();
        alterationFilter.setIncludeUnknownStatus(true);
        assertTrue(AlterationFilterHelper.build(alterationFilter).hasUnknownMutationStatus());
    }

    @Test
    public void getSelectedTiers() {
        AlterationFilter alterationFilter = new AlterationFilter();
        Map<String, Boolean> tiersMap = new HashMap<>();
        alterationFilter.setTiersBooleanMap(tiersMap);
        assertNotNull(AlterationFilterHelper.build(alterationFilter).getSelectedTiers());
    }

    @Test
    public void hasUnknownTier() {
        AlterationFilter alterationFilter = new AlterationFilter();
        alterationFilter.setIncludeUnknownTier(true);
        assertTrue(AlterationFilterHelper.build(alterationFilter).hasUnknownTier());
    }

    @Test
    public void isAllDriverAnnotationSelected() {
        AlterationFilter alterationFilter = new AlterationFilter();
        alterationFilter.setIncludeDriver(true);
        alterationFilter.setIncludeVUS(true);
        alterationFilter.setIncludeUnknownOncogenicity(true);
        assertTrue(AlterationFilterHelper.build(alterationFilter).isAllDriverAnnotationSelected());
    }

    @Test
    public void isNoDriverAnnotationSelected() {
        AlterationFilter alterationFilter = new AlterationFilter();
        alterationFilter.setIncludeDriver(false);
        alterationFilter.setIncludeVUS(false);
        alterationFilter.setIncludeUnknownOncogenicity(false);
        assertTrue(AlterationFilterHelper.build(alterationFilter).isNoDriverAnnotationSelected());
    }

    @Test
    public void isSomeDriverAnnotationsSelected() {
        AlterationFilter alterationFilter = new AlterationFilter();
        alterationFilter.setIncludeDriver(true);
        alterationFilter.setIncludeVUS(false);
        alterationFilter.setIncludeUnknownOncogenicity(false);
        assertTrue(AlterationFilterHelper.build(alterationFilter).isSomeDriverAnnotationsSelected());
    }

    @Test
    public void isAllMutationStatusSelected() {
        AlterationFilter alterationFilter = new AlterationFilter();
        alterationFilter.setIncludeGermline(true);
        alterationFilter.setIncludeSomatic(true);
        alterationFilter.setIncludeUnknownStatus(true);
        assertTrue(AlterationFilterHelper.build(alterationFilter).isAllMutationStatusSelected());
    }

    @Test
    public void isNoMutationStatusSelected() {
        AlterationFilter alterationFilter = new AlterationFilter();
        alterationFilter.setIncludeGermline(false);
        alterationFilter.setIncludeSomatic(false);
        alterationFilter.setIncludeUnknownStatus(false);
        assertTrue(AlterationFilterHelper.build(alterationFilter).isNoMutationStatusSelected());
    }

    @Test
    public void isSomeMutationStatusSelected() {
        AlterationFilter alterationFilter = new AlterationFilter();
        alterationFilter.setIncludeGermline(false);
        alterationFilter.setIncludeSomatic(true);
        alterationFilter.setIncludeUnknownStatus(false);
        assertTrue(AlterationFilterHelper.build(alterationFilter).isSomeMutationStatusSelected());
    }

    @Test
    public void isAllTierOptionsSelected() {
        AlterationFilter alterationFilter = new AlterationFilter();
        Map<String, Boolean> tiersMap = new HashMap<>();
        alterationFilter.setTiersBooleanMap(tiersMap);
        alterationFilter.setIncludeUnknownTier(true);
        assertTrue(AlterationFilterHelper.build(alterationFilter).isAllTierOptionsSelected());
    }
    
    @Test
    public void shouldApply() {
        AlterationFilter alterationFilter = new AlterationFilter();
        alterationFilter.setIncludeDriver(true);
        alterationFilter.setIncludeVUS(false);
        alterationFilter.setIncludeUnknownOncogenicity(false);
        assertTrue(AlterationFilterHelper.build(alterationFilter).shouldApplyMutationAlterationFilter());
        
    }
}