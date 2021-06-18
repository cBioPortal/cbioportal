package org.cbioportal.service.util;

import org.apache.commons.lang3.tuple.Triple;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class MutationMapperUtilsTest {

    @InjectMocks
    private MutationMapperUtils mutationMapperUtils;

    @Test
    public void validateRegex() {
        Triple<String, String, VariantType> result;

        // when only gene is present. ex: ALK
        result = mutationMapperUtils.extractInformationFromProteinChange("ALK");
        Assert.assertEquals("ALK", result.getLeft());
        Assert.assertNull(result.getMiddle());
        Assert.assertNull(result.getRight());

        // when only variant type is present. ex: DELETION
        result = mutationMapperUtils.extractInformationFromProteinChange("DELETION");
        Assert.assertNull(result.getLeft());
        Assert.assertNull(result.getMiddle());
        Assert.assertEquals(VariantType.DELETION, result.getRight());

        // when 2 genes is present. ex: ALK-TP53
        result = mutationMapperUtils.extractInformationFromProteinChange("ALK-TP53");
        Assert.assertEquals("ALK", result.getLeft());
        Assert.assertEquals("TP53", result.getMiddle());
        Assert.assertNull(result.getRight());

        // when gene and variant type is present. ex: ALK-intragenic
        result = mutationMapperUtils.extractInformationFromProteinChange("ALK-intragenic");
        Assert.assertEquals("ALK", result.getLeft());
        Assert.assertNull(result.getMiddle());
        Assert.assertEquals(VariantType.INTRAGENIC, result.getRight());

        // when 2 genes and variant type is present. ex: ALK-TP53 INVERSION
        result = mutationMapperUtils.extractInformationFromProteinChange("ALK-TP53 INVERSION");
        Assert.assertEquals("ALK", result.getLeft());
        Assert.assertEquals("TP53", result.getMiddle());
        Assert.assertEquals(VariantType.INVERSION, result.getRight());
    }
}
