package org.cbioportal.test.integration.export;

import static org.assertj.core.api.Assertions.assertThat;
import static org.cbioportal.test.integration.security.ContainerConfig.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import org.cbioportal.test.integration.security.ContainerConfig;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@TestPropertySource(
    properties = {
      "feature.study.export=true",
      "authenticate=false",
      "session.endpoint.publisher-api-key=this-is-a-secret",
      "session.service.url=http://localhost:"
          + SESSION_SERVICE_PORT
          + "/api/sessions/public_portal/",
      // DB settings (also see MysqlInitializer)
      "spring.datasource.driverClassName=com.mysql.jdbc.Driver",
      "spring.jpa.database-platform=org.hibernate.dialect.MySQL5Dialect",
    })
@ContextConfiguration(initializers = {MyMysqlInitializer.class, PortInitializer.class})
@DirtiesContext
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class ExportStudyDataIntegrationTest extends ContainerConfig {

  static final String CBIO_URL = String.format("http://localhost:%d", CBIO_PORT);

  @Autowired private TestRestTemplate restTemplate;

  @Test
  public void test1NoPublicVirtualStudiesAtTheBeginning() throws IOException {
    ResponseEntity<Resource> response1 =
        restTemplate.getForEntity(CBIO_URL + "/export/study/study_tcga_pub.zip", Resource.class);

    assertThat(response1.getStatusCode().is2xxSuccessful()).isTrue();
    HttpHeaders headers = response1.getHeaders();
    assertThat(headers.getContentType()).isNotNull();
    assertThat(headers.getContentType().toString()).isEqualTo("application/zip");

    // Verify Content-Disposition header for file name
    String contentDisposition = headers.getFirst(HttpHeaders.CONTENT_DISPOSITION);
    assertThat(contentDisposition).isNotNull();
    assertThat(contentDisposition).contains("attachment");
    assertThat(contentDisposition).contains("study_tcga_pub.zip");
    // Ensure the ZIP file is not empty
    try (InputStream zipInputStream = response1.getBody().getInputStream();
        ZipInputStream zis = new ZipInputStream(zipInputStream)) {

      /*Path tempFile = Files.createTempFile("temp", ".zip");
      Files.copy(zipInputStream, tempFile, StandardCopyOption.REPLACE_EXISTING);
      System.out.println("Temp file: " + tempFile);*/

      // Ensure there's at least one entry in the ZIP
      ZipEntry entry = zis.getNextEntry();
      assertThat(entry).isNotNull(); // Assert that the ZIP contains at least one file
    }
  }
}
