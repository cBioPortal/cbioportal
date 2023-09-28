package org.cbioportal.persistence.mybatis.util;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes=PaginationCalculator.class)
public class PaginationCalculatorTest {
    
    @Autowired
    private PaginationCalculator paginationCalculator;
    
    @Test
    public void testOffsetNull() {
        Assert.assertNull(paginationCalculator.offset(null, 1));
        Assert.assertNull(paginationCalculator.offset(1, null));
        Assert.assertNull(paginationCalculator.offset(null, null));
    }
    
    @Test
    public void testOffset() {
        Assert.assertEquals(0, (int) paginationCalculator.offset(10, 0));
        Assert.assertEquals(10, (int) paginationCalculator.offset(10, 1));
        Assert.assertEquals(100, (int) paginationCalculator.offset(10, 10));
    }

    @Test
    public void testLastIndexNull() {
        Assert.assertNull(paginationCalculator.lastIndex(null, 1, 1));
        Assert.assertNull(paginationCalculator.lastIndex(1, null, 1));
        Assert.assertNull(paginationCalculator.lastIndex(null, null, 1));
        Assert.assertNull(paginationCalculator.lastIndex(null, 1, 1));
        Assert.assertNull(paginationCalculator.lastIndex(1, 1, null));
        Assert.assertNull(paginationCalculator.lastIndex(null, 1, null));
        Assert.assertNull(paginationCalculator.lastIndex(1, null, null));
        Assert.assertNull(paginationCalculator.lastIndex(null, null, null));
    }
    
    @Test
    public void testLastIndex() {
        Assert.assertEquals(3, (int) paginationCalculator.lastIndex(0, 3, 26));
        Assert.assertEquals(6, (int) paginationCalculator.lastIndex(3, 3, 26));
        Assert.assertEquals(26, (int) paginationCalculator.lastIndex(25, 3, 26));
    }
}