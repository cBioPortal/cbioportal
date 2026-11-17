package org.cbioportal.infrastructure.repository.clickhouse;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import javax.xml.parsers.DocumentBuilderFactory;
import org.junit.Test;

/**
 * Architecture test that statically validates all ClickHouse MyBatis XML mappers
 * in {@code src/main/resources/mappers/clickhouse/}.
 *
 * <p>In accordance with AGENTS.md, every database query accessing study-specific
 * data must enforce study boundaries (e.g., {@code cancer_study_identifier IN})
 * to prevent cross-study data leakage, even when the caller passes a valid token.
 *
 * <p>This test scans every XML mapper, parses all {@code <select>} queries, recursively
 * resolves any {@code <include>} SQL fragments, and verifies that the query either:
 * <ul>
 *   <li>Restricts by study ({@code cancer_study_identifier}, {@code cancer_study_id}, etc.), OR
 *   <li>Is in {@link #AUDITED_EXCEPTIONS} with a documented security justification.
 * </ul>
 */
public class AllClickhouseMappersStudyFilterArchTest {

  private static final Path MAPPERS_ROOT =
      Path.of("src/main/resources/mappers/clickhouse");

  private static final Pattern SELECT_PATTERN =
      Pattern.compile("<select\\s+id=\"([^\"]+)\"(.*?)</select>", Pattern.DOTALL);

  private static final Pattern SQL_FRAGMENT_PATTERN =
      Pattern.compile("<sql\\s+id=\"([^\"]+)\"(.*?)</sql>", Pattern.DOTALL);

  private static final Pattern NAMESPACE_PATTERN =
      Pattern.compile("<mapper\\s+namespace=\"([^\"]+)\"", Pattern.DOTALL);

  private static final Pattern INCLUDE_PATTERN =
      Pattern.compile("<include\\s+refid=\"([^\"]+)\"(?:\\s*/>|\\s*>.*?</include>)", Pattern.DOTALL);

  private static final Pattern XML_COMMENT_PATTERN =
      Pattern.compile("<!--.*?-->", Pattern.DOTALL);

  private static final List<String> STUDY_FILTER_INDICATORS =
      List.of(
          "cancer_study_identifier",
          "cancer_study_id",
          "cancer_study",
          "study_id");

  /**
   * Queries that are legitimately exempt from direct study scoping because they:
   * 1. Query public/reference data (e.g., gene panels), OR
   * 2. Are scoped by sample/profile identifiers that have already been authorized, OR
   * 3. Provide aggregate counts for explicitly authorized entity lists.
   *
   * <p>Format: "MapperFileName.selectId"
   */
  private static final Set<String> AUDITED_EXCEPTIONS =
      Set.of(
          // Reference data: gene panels and entity mappings
          "ClickhouseAlterationMapper.getGenePanelGenes",
          "ClickhouseAlterationMapper.getEntityToGenePanels",

          // Alteration counts scoped by explicitly authorized sample/patient lists
          "ClickhouseAlterationMapper.getAlterationCountByGeneGivenSamplesAndMolecularProfiles",
          "ClickhouseAlterationMapper.getAlterationCountByGeneGivenPatientsAndMolecularProfiles",

          // Clinical enrichment counts scoped by sample/patient lists
          "ClickhouseClinicalDataMapper.getClinicalDataCountsForEnrichments",

          // Generic assay metadata & stable ID lookups scoped by profile stable IDs or entity IDs
          "GenericAssayMapper.getGenericAssayStableIdsByProfileIds",
          "GenericAssayMapper.getGenericAssayMetaByStableIds",
          "GenericAssayMapper.countGenericAssayMetaByStableIds",
          "GenericAssayMapper.getGenericAssayMetaByProfileIds",
          "GenericAssayMapper.countGenericAssayMetaByProfileIds",

          // Sample list counts scoped by pre-authorized sample list IDs
          "ClickhouseSampleMapper.getMetaSamplesBySampleListIds"
      );

  @Test
  public void allClickHouseMappersAreWellFormedXml() throws Exception {
    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    factory.setValidating(false);
    factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);

    List<Path> mapperFiles = listMapperXmlFiles();
    assertThat(mapperFiles).as("ClickHouse mapper XML files").isNotEmpty();

    for (Path file : mapperFiles) {
      factory.newDocumentBuilder().parse(file.toFile());
    }
  }

  @Test
  public void allClickHouseSelectQueriesEnforceStudyScopingOrAreAudited() throws IOException {
    List<Path> mapperFiles = listMapperXmlFiles();
    Map<String, String> fragments = collectSqlFragments(mapperFiles);

    List<String> unverifiedSelects = new ArrayList<>();
    int totalSelectCount = 0;

    for (Path file : mapperFiles) {
      String fileName = file.getFileName().toString().replace(".xml", "");
      String content = Files.readString(file, StandardCharsets.UTF_8);

      Matcher selectMatcher = SELECT_PATTERN.matcher(content);
      while (selectMatcher.find()) {
        totalSelectCount++;
        String selectId = selectMatcher.group(1);
        String selectBody = selectMatcher.group(2);

        String qualifiedKey = fileName + "." + selectId;
        if (AUDITED_EXCEPTIONS.contains(qualifiedKey)) {
          continue;
        }

        String cleanedBody = XML_COMMENT_PATTERN.matcher(selectBody).replaceAll("");
        String fullyExpandedBody = expandIncludes(cleanedBody, fragments, 0);

        boolean hasStudyFilter =
            STUDY_FILTER_INDICATORS.stream()
                .anyMatch(indicator -> fullyExpandedBody.toLowerCase().contains(indicator));

        if (!hasStudyFilter) {
          unverifiedSelects.add(qualifiedKey + " (in " + file + ")");
        }
      }
    }

    assertThat(totalSelectCount).as("total select queries audited").isGreaterThan(50);
    assertThat(unverifiedSelects)
        .as("Select queries in ClickHouse mappers lacking study filter enforcement. "
            + "Ensure each select includes cancer_study_identifier/cancer_study_id, or add "
            + "it to AUDITED_EXCEPTIONS in AllClickhouseMappersStudyFilterArchTest with justification.")
        .isEmpty();
  }

  private List<Path> listMapperXmlFiles() throws IOException {
    try (Stream<Path> stream = Files.walk(MAPPERS_ROOT)) {
      return stream
          .filter(Files::isRegularFile)
          .filter(path -> path.toString().endsWith(".xml"))
          .toList();
    }
  }

  private Map<String, String> collectSqlFragments(List<Path> mapperFiles) throws IOException {
    Map<String, String> fragments = new HashMap<>();
    for (Path file : mapperFiles) {
      String content = Files.readString(file, StandardCharsets.UTF_8);
      Matcher nsMatcher = NAMESPACE_PATTERN.matcher(content);
      String namespace = nsMatcher.find() ? nsMatcher.group(1) : "";

      Matcher fragMatcher = SQL_FRAGMENT_PATTERN.matcher(content);
      while (fragMatcher.find()) {
        String fragId = fragMatcher.group(1);
        String fragBody = XML_COMMENT_PATTERN.matcher(fragMatcher.group(2)).replaceAll("");
        fragments.put(fragId, fragBody);
        if (!namespace.isEmpty()) {
          fragments.put(namespace + "." + fragId, fragBody);
        }
      }
    }
    return fragments;
  }

  private String expandIncludes(String text, Map<String, String> fragments, int depth) {
    if (depth > 10) {
      return text;
    }
    Matcher matcher = INCLUDE_PATTERN.matcher(text);
    StringBuffer sb = new StringBuffer();
    boolean found = false;
    while (matcher.find()) {
      found = true;
      String refid = matcher.group(1);
      String replacement = fragments.getOrDefault(refid, "");
      matcher.appendReplacement(sb, Matcher.quoteReplacement(replacement));
    }
    matcher.appendTail(sb);
    String expanded = sb.toString();
    if (found && expanded.contains("<include") && depth < 10) {
      return expandIncludes(expanded, fragments, depth + 1);
    }
    return expanded;
  }
}
