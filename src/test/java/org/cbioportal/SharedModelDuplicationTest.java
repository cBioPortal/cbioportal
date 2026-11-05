package org.cbioportal;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import org.junit.Test;

/**
 * The types in {@code shared-model} ({@code org.cbioportal.shared.model}) are the only definitions
 * of these types. The web application and the importer used to keep hand-maintained copies that
 * drifted apart, and nothing but this test stops a new copy from being added.
 *
 * <p>The test scans the web application's and the importer's sources for a declaration of any of
 * the shared type names, including nested ones (the copies used to be enums nested in {@code
 * MolecularProfile}). It reads source files rather than classes because the web tests run before
 * the importer is compiled.
 *
 * <p>This is a textual check. It catches a copy that keeps the original name; it cannot catch a
 * renamed copy.
 *
 * @see
 *     org.cbioportal.infrastructure.repository.clickhouse.resource.ResourceDataMapperStudyFilterTest
 *     for the same approach applied to mapper XML
 */
public class SharedModelDuplicationTest {

  // Paths are relative to the repository root, the tests' working directory.
  private static final List<String> SOURCE_ROOTS =
      List.of("src/main/java", "importer/src/main/java");

  private static final Set<String> SHARED_TYPES =
      Set.of(
          "CancerStudyTags",
          "EntityType",
          "GenericAssayMeta",
          "GenericEntityProperty",
          "GeneticAlterationType",
          "GeneticEntity",
          "MolecularAlterationType",
          "MolecularProfileDataType",
          "ReferenceGenome",
          "ResourceType",
          "User",
          "UserAuthorities");

  // Same names, different classes: the file export's row objects, which have their own shape
  // (for example entityType is a String) and are mapped by the export MyBatis mapper.
  private static final Set<String> ALLOWED =
      Set.of(
          "src/main/java/org/cbioportal/application/file/model/GeneticEntity.java",
          "src/main/java/org/cbioportal/application/file/model/GenericEntityProperty.java");

  private static final Pattern DECLARATION =
      Pattern.compile(
          "\\b(?:class|enum|interface|record)\\s+(" + String.join("|", SHARED_TYPES) + ")\\b");

  private static final Pattern COMMENT = Pattern.compile("//[^\\n]*|/\\*.*?\\*/", Pattern.DOTALL);

  @Test
  public void sharedTypesAreDeclaredOnlyInSharedModel() throws IOException {
    List<String> copies = new ArrayList<>();
    for (String root : SOURCE_ROOTS) {
      assertThat(Path.of(root)).as("source root").isDirectory();
      try (Stream<Path> files = Files.walk(Path.of(root))) {
        for (Path file : files.filter(f -> f.toString().endsWith(".java")).toList()) {
          if (ALLOWED.contains(file.toString())) {
            continue;
          }
          String source =
              COMMENT.matcher(Files.readString(file, StandardCharsets.UTF_8)).replaceAll("");
          Matcher declaration = DECLARATION.matcher(source);
          while (declaration.find()) {
            copies.add(file + " declares " + declaration.group(1));
          }
        }
      }
    }
    assertThat(copies)
        .as("use the type from org.cbioportal.shared.model (shared-model/) instead")
        .isEmpty();
  }
}
