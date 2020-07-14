package org.cbioportal.model.util;

import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;

public class SelectTest {

    @Test
    public void all() {
        Select<Object> s = Select.all();
        Assert.assertFalse(s.hasNone());
        Assert.assertFalse(s.hasValues());
        Assert.assertTrue(s.hasAll());
    }

    @Test
    public void none() {
        Select<Object> s = Select.none();
        Assert.assertTrue(s.hasNone());
        Assert.assertFalse(s.hasValues());
        Assert.assertFalse(s.hasAll());
    }

    @Test
    public void byValues() {
        Select<String> s = Select.byValues(Arrays.asList("A", "B"));
        Assert.assertFalse(s.hasNone());
        Assert.assertTrue(s.hasValues());
        Assert.assertFalse(s.hasAll());
    }
    
    @Test
    public void byValuesEmptyList() {
        Select<String> s = Select.byValues(Collections.emptyList());
        Assert.assertTrue(s.hasNone());
        Assert.assertFalse(s.hasValues());
        Assert.assertFalse(s.hasAll());
    }

    @Test
    public void hasAllExternal() {
        Select<String> s = Select.byValues(Arrays.asList("A", "B"));
        s.hasAll(true);
        Assert.assertFalse(s.hasNone());
        Assert.assertTrue(s.hasValues());
        Assert.assertTrue(s.hasAll());
    }

}