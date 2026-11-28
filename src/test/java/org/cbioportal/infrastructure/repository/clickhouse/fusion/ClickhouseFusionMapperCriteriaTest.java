package org.cbioportal.infrastructure.repository.clickhouse.fusion;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.Test;

/**
 * The fusion criterion is defined in the fusion mapper and, negated, in the legacy structural
 * variant mapper. Each event must be served by exactly one endpoint, so the two copies must not
 * drift apart.
 */
public class ClickhouseFusionMapperCriteriaTest {

  private static final Path FUSION_MAPPER =
      Path.of("src/main/resources/mappers/clickhouse/fusion/ClickhouseFusionMapper.xml");
  private static final Path STRUCTURAL_VARIANT_MAPPER =
      Path.of(
          "src/main/resources/org/cbioportal/legacy/persistence/mybatis/StructuralVariantMapper.xml");

  @Test
  public void structuralVariantMapperExcludesExactlyTheFusionCriteria() throws IOException {
    String fusionCriteria = sqlFragment(FUSION_MAPPER, "fusionCriteria");
    String excludeFusions = sqlFragment(STRUCTURAL_VARIANT_MAPPER, "excludeFusions");

    assertEquals("AND NOT " + fusionCriteria, excludeFusions);
  }

  private static String sqlFragment(Path file, String id) throws IOException {
    Matcher matcher =
        Pattern.compile("<sql id=\"" + id + "\">(.*?)</sql>", Pattern.DOTALL)
            .matcher(Files.readString(file));
    if (!matcher.find()) {
      throw new AssertionError("no <sql id=\"" + id + "\"> in " + file);
    }
    return matcher.group(1).replaceAll("\\s+", " ").trim();
  }
}
