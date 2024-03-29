package org.cbioportal.persistence.mybatis.util;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes=PaginationCalculator.class)
public class PaginationCalculatorTest {
    
    
    @Test
    public void testOffsetNull() {
        Assert.assertNull(PaginationCalculator.offset(null, 1));
        Assert.assertNull(PaginationCalculator.offset(1, null));
        Assert.assertNull(PaginationCalculator.offset(null, null));
    }
    
    @Test
    public void testOffset() {
        Assert.assertEquals(0, (int) PaginationCalculator.offset(10, 0));
        Assert.assertEquals(10, (int) PaginationCalculator.offset(10, 1));
        Assert.assertEquals(100, (int) PaginationCalculator.offset(10, 10));
    }

    @Test
    public void testLastIndexNull() {
        Assert.assertNull(PaginationCalculator.lastIndex(null, 1, 1));
        Assert.assertNull(PaginationCalculator.lastIndex(1, null, 1));
        Assert.assertNull(PaginationCalculator.lastIndex(null, null, 1));
        Assert.assertNull(PaginationCalculator.lastIndex(null, 1, 1));
        Assert.assertNull(PaginationCalculator.lastIndex(1, 1, null));
        Assert.assertNull(PaginationCalculator.lastIndex(null, 1, null));
        Assert.assertNull(PaginationCalculator.lastIndex(1, null, null));
        Assert.assertNull(PaginationCalculator.lastIndex(null, null, null));
    }
    
    @Test
    public void testLastIndex() {
        Assert.assertEquals(3, (int) PaginationCalculator.lastIndex(0, 3, 26));
        Assert.assertEquals(6, (int) PaginationCalculator.lastIndex(3, 3, 26));
        Assert.assertEquals(26, (int) PaginationCalculator.lastIndex(25, 3, 26));
    }
}