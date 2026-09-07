package org.cbioportal.application.rest.response;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import io.swagger.v3.core.converter.ModelConverters;
import io.swagger.v3.core.converter.ResolvedSchema;
import io.swagger.v3.core.jackson.ModelResolver;
import io.swagger.v3.oas.annotations.media.Schema;
import java.io.IOException;
import java.lang.reflect.RecordComponent;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.cbioportal.legacy.web.config.CustomObjectMapper;
import org.junit.Test;

public class DtoOpenApiSchemaTest {

  private static final String DTO_PACKAGE = "org.cbioportal.application.rest.response";
  private static final Path DTO_SOURCE_DIR =
      Path.of("src/main/java/org/cbioportal/application/rest/response");

  @Test
  public void generatedSchemasShouldMatchExplicitDtoFields() throws Exception {
    ModelConverters converters = new ModelConverters();
    converters.addConverter(new ModelResolver(new CustomObjectMapper()));

    assertTrue(
        "DTO source directory must exist: " + DTO_SOURCE_DIR, Files.isDirectory(DTO_SOURCE_DIR));
    List<Class<?>> dtoClasses = discoverResponseDtoClasses();
    assertTrue("At least one DTO record should be discovered", !dtoClasses.isEmpty());

    for (Class<?> dtoClass : dtoClasses) {
      assertTrue(dtoClass.getSimpleName() + " should stay a record", dtoClass.isRecord());

      ResolvedSchema resolvedSchema = converters.readAllAsResolvedSchema(dtoClass);
      assertNotNull(dtoClass.getSimpleName() + " should resolve an OpenAPI schema", resolvedSchema);
      assertNotNull(
          dtoClass.getSimpleName() + " should resolve a root OpenAPI schema",
          resolvedSchema.schema);

      Set<String> expectedProperties = new LinkedHashSet<>();
      for (RecordComponent component : dtoClass.getRecordComponents()) {
        expectedProperties.add(component.getName());
      }

      Set<String> actualProperties = new LinkedHashSet<>();
      Map<String, io.swagger.v3.oas.models.media.Schema> schemaProperties =
          resolvedSchema.schema.getProperties();
      if (schemaProperties != null) {
        actualProperties.addAll(schemaProperties.keySet());
      }

      assertEquals(
          dtoClass.getSimpleName() + " schema properties should match its explicit record fields",
          expectedProperties,
          actualProperties);

      Schema schemaAnnotation = dtoClass.getAnnotation(Schema.class);
      if (schemaAnnotation != null && !schemaAnnotation.name().isBlank()) {
        assertEquals(
            dtoClass.getSimpleName() + " schema name should match its explicit @Schema name",
            schemaAnnotation.name(),
            resolvedSchema.schema.getName());
      }
    }
  }

  private List<Class<?>> discoverResponseDtoClasses() throws IOException, ClassNotFoundException {
    try (Stream<Path> files = Files.list(DTO_SOURCE_DIR)) {
      return files
          .map(Path::getFileName)
          .map(Path::toString)
          .filter(name -> name.endsWith("DTO.java"))
          .map(name -> name.substring(0, name.length() - ".java".length()))
          .sorted()
          .map(className -> loadClass(DTO_PACKAGE + "." + className))
          .filter(Class::isRecord)
          .collect(Collectors.toList());
    }
  }

  private Class<?> loadClass(String fullyQualifiedName) {
    try {
      return Class.forName(fullyQualifiedName);
    } catch (ClassNotFoundException e) {
      throw new IllegalStateException("Unable to load DTO class: " + fullyQualifiedName, e);
    }
  }
}
