package org.cbioportal.infrastructure.repository.clickhouse.cancerstudy;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.cbioportal.config.MybatisTestConfiguration;
import org.cbioportal.shared.SortAndSearchCriteria;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("clickhouse")
@Import(MybatisTestConfiguration.class)
public class ClickhouseCancerStudyRepositoryIntegrationTest {

    private ClickhouseCancerStudyRepository repository;

    @Autowired
    private ClickhouseCancerStudyMapper mapper;

    @BeforeEach
    void setup() {
        repository = new ClickhouseCancerStudyRepository(mapper);

    }

    @DynamicPropertySource
    static void registerProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.clickhouse.url",
                () -> "jdbc:ch:https://mecgt250i0.us-east-1.aws.clickhouse.cloud:8443/cgds_public_2025_06_24?zeroDateTimeBehavior=convertToNull");
        registry.add("spring.datasource.clickhouse.username", () -> "app_user");
        registry.add("spring.datasource.clickhouse.password", () -> "P@ssword1976");
        registry.add("spring.datasource.clickhouse.driver-class-name", () -> "com.clickhouse.jdbc.ClickHouseDriver");
    }

    @Test
    void testGetCancerStudiesMetadata() {
        var studies = repository.getCancerStudiesMetadata(new SortAndSearchCriteria("", "", ""));
        assertEquals(492, studies.size());
    }

    // @Test
    // void testGetCancerStudiesMetadataSummary() {

    // }

    // @Test
    // void testGetFilteredStudyIds() {

    // }
}
