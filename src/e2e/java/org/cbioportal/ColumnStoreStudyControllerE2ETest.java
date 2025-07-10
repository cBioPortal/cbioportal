package org.cbioportal;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;
import java.util.List;

import org.cbioportal.application.rest.response.CancerStudyMetadataDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
public class ColumnStoreStudyControllerE2ETest extends AbstractE2ETest {

    private static final int TOTAL_STUDIES = 492;

    @Autowired
    private TestRestTemplate restTemplate;

    @LocalServerPort
    private int port;

    @Test
    void testGetAllStudies() {
        var response = restTemplate.getForEntity("http://localhost:" + port + "/api/column-store/studies?projection=DETAILED", CancerStudyMetadataDTO[].class);
        assertEquals(HttpStatus.OK, response.getStatusCode());

        List<CancerStudyMetadataDTO> data = Arrays.asList(response.getBody());
        assertEquals(TOTAL_STUDIES, data.size());
    }
}
