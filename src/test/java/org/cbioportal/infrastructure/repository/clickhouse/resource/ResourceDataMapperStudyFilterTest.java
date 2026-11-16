package org.cbioportal.infrastructure.repository.clickhouse.resource;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.xml.parsers.DocumentBuilderFactory;
import org.junit.Test;

/**
 * Every select in the resource mapper has to restrict {@code cs.cancer_study_identifier} to the
 * study list the caller supplied.
 *
 * <p>The controller's {@code @PreAuthorize} checks that the user may read the studies named in the
 * request. It cannot check what the query then reads, so a select that omits the filter would hand
 * a user who is legitimately authorized for one study the rows of every other study: authorization
 * passes and the data still leaks. The filter is what binds the authorized list to the data.
 *
 * <p>A select satisfies this either by including {@code ResourceTableBaseFilters} or by carrying
 * the same condition inline -- {@code getResourceTableTabs} does the latter, because it is passed a
 * different parameter object and cannot reuse the fragment.
 *
 * <p>This is a textual check. It catches the realistic mistake, a select added or copied without
 * the filter, and does not prove the filter is reachable in every branch of a select that has one.
 *
 * @see org.cbioportal.application.security.EndpointAuthorizationArchTest for the same approach
 *     applied to {@code @PreAuthorize} on controllers
 */
public class ResourceDataMapperStudyFilterTest {

  // Read from the source tree rather than the classpath, so the test names a path a developer can
  // open. Surefire runs from the project basedir.
  private static final String MAPPER =
      "src/main/resources/mappers/clickhouse/resource/ResourceDataMapper.xml";

  private static final String BASE_FILTERS = "ResourceTableBaseFilters";
  private static final String INLINE_FILTER = "cancer_study_identifier IN";

  private static final Pattern SELECT =
      Pattern.compile("<select\\s+id=\"(\\w+)\"(.*?)</select>", Pattern.DOTALL);

  private static final Pattern XML_COMMENT = Pattern.compile("<!--.*?-->", Pattern.DOTALL);

  @Test
  public void everySelectIsFilteredByTheCallersStudyList() throws IOException {
    List<String> unfiltered = new ArrayList<>();
    Matcher select = SELECT.matcher(read(MAPPER));
    int found = 0;
    while (select.find()) {
      found++;
      // Strip comments first: one select explains in a comment why it cannot reuse
      // ResourceTableBaseFilters, and a bare substring search would read that mention as the
      // filter itself.
      String body = XML_COMMENT.matcher(select.group(2)).replaceAll("");
      if (!body.contains(BASE_FILTERS) && !body.contains(INLINE_FILTER)) {
        unfiltered.add(select.group(1));
      }
    }
    assertThat(found).as("selects parsed from %s", MAPPER).isPositive();
    assertThat(unfiltered)
        .as(
            "selects in %s that neither include %s nor filter on '%s' -- see the warning at the top"
                + " of that file",
            MAPPER, BASE_FILTERS, INLINE_FILTER)
        .isEmpty();
  }

  /**
   * The mapper is otherwise only parsed when a test starts a ClickHouse container, so a malformed
   * edit surfaces minutes later as a Spring context failure rather than as a parse error. XML also
   * forbids "--" inside a comment, which is an easy thing to write in a prose warning.
   */
  @Test
  public void theMapperIsWellFormedXml() throws Exception {
    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    // The DTD in the doctype is fetched over the network otherwise, which a build should not need.
    factory.setValidating(false);
    factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
    factory.newDocumentBuilder().parse(Path.of(MAPPER).toFile());
  }

  /** A regex that quietly stopped matching would leave the check above passing on nothing. */
  @Test
  public void everySelectStatementIsChecked() throws IOException {
    String mapper = read(MAPPER);
    int declared = count(Pattern.compile("<select\\s"), mapper);
    assertThat(count(SELECT, mapper))
        .as("selects whose body was read, in %s", MAPPER)
        .isEqualTo(declared);
  }

  private static int count(Pattern pattern, String text) {
    Matcher matcher = pattern.matcher(text);
    int found = 0;
    while (matcher.find()) {
      found++;
    }
    return found;
  }

  private static String read(String path) throws IOException {
    Path file = Path.of(path);
    assertThat(Files.exists(file)).as("mapper %s exists", file.toAbsolutePath()).isTrue();
    return Files.readString(file, StandardCharsets.UTF_8);
  }
}
