package org.cbioportal.test.integration;

import org.cbioportal.test.integration.security.ContainerConfig;
import org.cbioportal.utils.removeme.Session;
import org.cbioportal.web.parameter.StudyViewFilter;
import org.cbioportal.web.parameter.VirtualStudy;
import org.cbioportal.web.parameter.VirtualStudyData;
import org.cbioportal.web.parameter.VirtualStudySamples;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.cbioportal.test.integration.security.ContainerConfig.MyMysqlInitializer;
import static org.cbioportal.test.integration.security.ContainerConfig.PortInitializer;
import static org.cbioportal.test.integration.security.ContainerConfig.SESSION_SERVICE_PORT;

@RunWith(SpringRunner.class)
@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT
)
@TestPropertySource(
    properties = {
        "authenticate=false",
        "session.endpoint.publisher-api-key=this-is-a-secret",
        "session.service.url=http://localhost:" + SESSION_SERVICE_PORT + "/api/sessions/public_portal/",
        // DB settings (also see MysqlInitializer)
        "spring.datasource.driverClassName=com.mysql.jdbc.Driver",
        "spring.jpa.database-platform=org.hibernate.dialect.MySQL5Dialect",
    }
)
@ContextConfiguration(initializers = {
    MyMysqlInitializer.class,
    PortInitializer.class
})
@DirtiesContext
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class PublicVirtualStudiesIntegrationTest extends ContainerConfig {

    final static String CBIO_URL = String.format("http://localhost:%d", CBIO_PORT);

    final static HttpHeaders jsonContentType = new HttpHeaders() {
        {
            set("Content-Type", "application/json");
        }
    };
    
    final static HttpHeaders invalidKeyContainingHeaders = new HttpHeaders() {
        {
            set("X-PUBLISHER-API-KEY", "this-is-not-valid-key");
        }
    };
    
    final static HttpHeaders validKeyContainingHeaders = new HttpHeaders() {
        {
            set("X-PUBLISHER-API-KEY", "this-is-a-secret");
        }
    };
    
    final static ParameterizedTypeReference<List<VirtualStudy>> typeRef = new ParameterizedTypeReference<>() {
    };

    static String createdVsId;
    static String publishedVsId;

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    public void test1NoPublicVirtualStudiesAtTheBeginning() {
        ResponseEntity<List<VirtualStudy>> response1 = restTemplate.exchange(
            CBIO_URL + "/api/public_virtual_studies",
            HttpMethod.GET,
            null,
            typeRef);

        assertThat(response1.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(response1.getBody()).hasSize(0);
    }
    
    @Test
    public void test2CreateVirtualStudy() {
        VirtualStudyData vsDataToSave = createTestVsData();

        ResponseEntity<VirtualStudy> response2 = restTemplate.exchange(
            CBIO_URL + "/api/session/virtual_study",
            HttpMethod.POST,
            new HttpEntity<>(vsDataToSave, jsonContentType),
            VirtualStudy.class);
        assertThat(response2.getStatusCode().is2xxSuccessful()).isTrue();
        VirtualStudy savedVs = response2.getBody();
        assertThat(savedVs).isNotNull().hasFieldOrProperty("id").isNotNull();
        createdVsId = savedVs.getId();
    }

    @Test
    public void test3PublishVirtualStudy() {
        ResponseEntity<VirtualStudy> response3 = restTemplate.exchange(
            CBIO_URL + "/api/public_virtual_studies/" + createdVsId,
            HttpMethod.POST,
            null,
            VirtualStudy.class);
        assertThat(response3.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        
        response3 = restTemplate.exchange(
            CBIO_URL + "/api/public_virtual_studies/" + createdVsId,
            HttpMethod.POST,
            new HttpEntity<>(null, invalidKeyContainingHeaders),
            VirtualStudy.class);
        assertThat(response3.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        
        response3 = restTemplate.exchange(
            CBIO_URL + "/api/public_virtual_studies/" + createdVsId,
            HttpMethod.POST,
            new HttpEntity<>(null, validKeyContainingHeaders),
            VirtualStudy.class);
        assertThat(response3.getStatusCode().is2xxSuccessful()).isTrue();
        VirtualStudy publishedVs = response3.getBody();
        assertThat(publishedVs).isNotNull().hasFieldOrProperty("id").isNotNull();
        publishedVsId = publishedVs.getId();
    }

    @Test
    public void test4ListJustPublishedStudy() {
        ResponseEntity<List<VirtualStudy>> response4 = restTemplate.exchange(
            CBIO_URL + "/api/public_virtual_studies",
            HttpMethod.GET,
            null,
            typeRef);

        assertThat(response4.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(response4.getBody()).hasSize(1);
    }

    @Test
    public void test5UnpublishVirtualStudy() {
        ResponseEntity<Object> response5 = restTemplate.exchange(
            CBIO_URL + "/api/public_virtual_studies/" + publishedVsId,
            HttpMethod.DELETE,
            null,
            Object.class);
        assertThat(response5.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);

        response5 = restTemplate.exchange(
            CBIO_URL + "/api/public_virtual_studies/" + publishedVsId,
            HttpMethod.DELETE,
            new HttpEntity<>(null, invalidKeyContainingHeaders),
            Object.class);
        assertThat(response5.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);

        response5 = restTemplate.exchange(
            CBIO_URL + "/api/public_virtual_studies/" + publishedVsId,
            HttpMethod.DELETE,
            new HttpEntity<>(null, validKeyContainingHeaders),
            Object.class);
        assertThat(response5.getStatusCode().is2xxSuccessful()).isTrue();
    }
    
    @Test
    public void test6NoPublicVirtualStudiesAfterRemoval() {
        ResponseEntity<List<VirtualStudy>> response6 = restTemplate.exchange(
            CBIO_URL + "/api/public_virtual_studies",
            HttpMethod.GET,
            null,
            typeRef);

        assertThat(response6.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(response6.getBody()).hasSize(0);
    }

    private VirtualStudyData createTestVsData() {
        VirtualStudyData data = new VirtualStudyData();
        VirtualStudySamples study1 = new VirtualStudySamples();
        study1.setId("study_tcga_pub");
        study1.setSamples(Set.of("TCGA-A1-A0SB-01", "TCGA-A1-A0SJ-01"));
        VirtualStudySamples study2 = new VirtualStudySamples();
        study2.setId("acc_tcga");
        study2.setSamples(Set.of("TCGA-XX-0800-01"));
        Set<VirtualStudySamples> studies = Set.of(
            study1,
            study2
        );
        data.setStudies(studies);
        StudyViewFilter studyViewFilter = new StudyViewFilter();
        studyViewFilter.setStudyIds(List.of("study_tcga_pub", "acc_tcga"));
        data.setStudyViewFilter(studyViewFilter);
        return data;
    }

}