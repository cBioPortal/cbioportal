package org.cbioportal.legacy.service.util;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.math3.distribution.TDistribution;
import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.stat.correlation.SpearmansCorrelation;
import org.cbioportal.legacy.model.CoExpression;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
public class CoExpressionAsyncMethods {

  @Async
  public CompletableFuture<CoExpression> computeCoExpression(
      String entityId, List<String> valuesA, List<String> valuesB, Double threshold) {

    List<String> valuesACopy = new ArrayList<>(valuesA);
    List<String> valuesBCopy = new ArrayList<>(valuesB);

    Iterator<String> itA = valuesACopy.listIterator();
    Iterator<String> itB = valuesBCopy.listIterator();
    while (itA.hasNext() && itB.hasNext()) {
      if (!NumberUtils.isCreatable(itA.next()) | !NumberUtils.isCreatable(itB.next())) {
        itA.remove();
        itB.remove();
      }
    }

    CoExpression coExpression = new CoExpression();
    coExpression.setGeneticEntityId(entityId);

    double[] valuesBNumber = valuesBCopy.stream().mapToDouble(Double::parseDouble).toArray();
    double[] valuesANumber = valuesACopy.stream().mapToDouble(Double::parseDouble).toArray();

    if (valuesANumber.length <= 2) {
      return null;
    }

    double[][] arrays = new double[2][valuesANumber.length];
    arrays[0] = valuesBNumber;
    arrays[1] = valuesANumber;
    SpearmansCorrelation spearmansCorrelation =
        new SpearmansCorrelation((new Array2DRowRealMatrix(arrays, false)).transpose());

    double spearmansValue = spearmansCorrelation.correlation(valuesBNumber, valuesANumber);
    if (Double.isNaN(spearmansValue) || Math.abs(spearmansValue) < threshold) {
      return null;
    }
    coExpression.setSpearmansCorrelation(BigDecimal.valueOf(spearmansValue));

    RealMatrix resultMatrix = spearmansCorrelation.getRankCorrelation().getCorrelationPValues();
    coExpression.setpValue(BigDecimal.valueOf(resultMatrix.getEntry(0, 1)));

    return CompletableFuture.supplyAsync(() -> coExpression);
  }

  public static double computePValue(double r, int n) {
    if (n <= 2) {
      return 1.0;
    }
    double rSquared = r * r;
    if (rSquared >= 1.0) {
      return 0.0;
    }
    double t = r * Math.sqrt((n - 2.0) / (1.0 - rSquared));
    TDistribution tDist = new TDistribution(n - 2);
    // Use cdf(-|t|) instead of 1-cdf(|t|) to avoid floating point cancellation
    // when cdf(|t|) is very close to 1.0. This matches Apache Commons Math3's
    // PearsonsCorrelation.getCorrelationPValues() approach.
    return 2.0 * tDist.cumulativeProbability(-Math.abs(t));
  }
}
