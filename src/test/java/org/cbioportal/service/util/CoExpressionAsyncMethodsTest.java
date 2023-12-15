package org.cbioportal.service.util;

import org.cbioportal.model.CoExpression;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.List;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.stream.Collectors;
import java.util.concurrent.CompletableFuture;

@RunWith(MockitoJUnitRunner.class)
public class CoExpressionAsyncMethodsTest {

    private static final double THRESHOLD = 0.3;

    @InjectMocks
    private CoExpressionAsyncMethods asyncMethods;

    @Test
    public void computeGeneCoExpressions() throws Exception {

        List<CompletableFuture<CoExpression>> futures = new ArrayList<>();
        List<List<String>> allValuesA = createAllValuesA();
        List<String> valuesB = createValuesB();

        CompletableFuture<CoExpression> future1 = asyncMethods
            .computeCoExpression("2", allValuesA.get(0), valuesB, THRESHOLD);
        CompletableFuture<CoExpression> future2 = asyncMethods
            .computeCoExpression("3", allValuesA.get(1), valuesB, THRESHOLD);
        CompletableFuture<CoExpression> future3 = asyncMethods
            .computeCoExpression("4", allValuesA.get(2), valuesB, THRESHOLD);

        futures.add(future1); futures.add(future2); futures.add(future3);
        List<CoExpression> result = futures
            .stream().filter(Objects::nonNull).map(CompletableFuture::join).collect(Collectors.toList());

        Assert.assertEquals(2, result.size());
        CoExpression coExpression1 = result.get(0);
        Assert.assertEquals("2", coExpression1.getGeneticEntityId());
        Assert.assertEquals(new BigDecimal("0.5"), coExpression1.getSpearmansCorrelation());
        Assert.assertEquals(new BigDecimal("0.6666666666666667"), coExpression1.getpValue());
        CoExpression coExpression2 = result.get(1);
        Assert.assertEquals("3", coExpression2.getGeneticEntityId());
        Assert.assertEquals(new BigDecimal("0.8660254037844386"), coExpression2.getSpearmansCorrelation());
        Assert.assertEquals(new BigDecimal("0.3333333333333333"), coExpression2.getpValue());
    }

    @Test
    public void computeGenesetCoExpressions() throws Exception {

        List<CompletableFuture<CoExpression>> futures = new ArrayList<>();
        List<List<String>> allValuesA = createAllValuesA();
        List<String> valuesB = createValuesB();

        CompletableFuture<CoExpression> future1 = asyncMethods
            .computeCoExpression("KEGG_DNA_REPLICATION", allValuesA.get(1), valuesB, THRESHOLD);
        CompletableFuture<CoExpression> future2 = asyncMethods
            .computeCoExpression("BIOCARTA_ASBCELL_PATHWAY", allValuesA.get(0), valuesB, THRESHOLD);
        CompletableFuture<CoExpression> future3 = asyncMethods
            .computeCoExpression("REACTOME_DIGESTION_OF_DIETARY_CARBOHYDRATE", allValuesA.get(2), valuesB, THRESHOLD);

        futures.add(future1); futures.add(future2); futures.add(future3);
        List<CoExpression> result = futures
            .stream().filter(Objects::nonNull).map(CompletableFuture::join).collect(Collectors.toList());

        Assert.assertEquals(2, result.size());
        CoExpression coExpression1 = result.get(0);
        Assert.assertEquals("KEGG_DNA_REPLICATION", coExpression1.getGeneticEntityId());
        Assert.assertEquals(new BigDecimal("0.8660254037844386"), coExpression1.getSpearmansCorrelation());
        Assert.assertEquals(new BigDecimal("0.3333333333333333"), coExpression1.getpValue());
        CoExpression coExpression2 = result.get(1);
        Assert.assertEquals("BIOCARTA_ASBCELL_PATHWAY", coExpression2.getGeneticEntityId());
        Assert.assertEquals(new BigDecimal("0.5"), coExpression2.getSpearmansCorrelation());
        Assert.assertEquals(new BigDecimal("0.6666666666666667"), coExpression2.getpValue());
    }

    private List<List<String>> createAllValuesA() {
        List<List<String>> allValuesA = new ArrayList<>();
        List<String> valuesA1 = new ArrayList<>();
        valuesA1.add("2"); valuesA1.add("3"); valuesA1.add("2");
        allValuesA.add(valuesA1);
        List<String> valuesA2 = new ArrayList<>();
        valuesA2.add("1.1"); valuesA2.add("5"); valuesA2.add("3");
        allValuesA.add(valuesA2);
        List<String> valuesA3 = new ArrayList<>();
        valuesA3.add("1"); valuesA3.add("4"); valuesA3.add("0");
        allValuesA.add(valuesA3);
        return allValuesA;
    }

    private List<String> createValuesB() {
        return new ArrayList<>(Arrays.asList("2.1", "3", "3"));
    }
}
