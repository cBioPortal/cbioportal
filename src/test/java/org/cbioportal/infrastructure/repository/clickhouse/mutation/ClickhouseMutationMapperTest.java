package org.cbioportal.infrastructure.repository.clickhouse.mutation;

import org.cbioportal.infrastructure.repository.clickhouse.AbstractTestcontainers;
import org.cbioportal.infrastructure.repository.clickhouse.config.MyBatisConfig;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@Import(MyBatisConfig.class)
@DataJpaTest
@DirtiesContext
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ContextConfiguration(initializers = AbstractTestcontainers.Initializer.class)
public class ClickhouseMutationMapperTest {

    @Autowired private ClickhouseMutationMapper clickhouseMutationMapper;

    @Test
    public void getMutationsInMultipleMolecularProfilesIdProjection() {
        var allMolecularProfileIds = List.of("study_tcga_pub_mutations");
        var allSampleIds = List.of("tcga-a1-a0sh-01");
        var entrezGeneIds = List.of(672);
        


        var result = clickhouseMutationMapper.getMutationsInMultipleMolecularProfilesId(allMolecularProfileIds,allSampleIds,
            entrezGeneIds, false, "ID", null, null, null, null);

        assertEquals(2, result.size());
        result.forEach(mutation -> {
            assertEquals("study_tcga_pub_mutations", mutation.getMolecularProfileId());
            assertEquals("tcga-a1-a0sh-01", mutation.getSampleId());
            assertEquals((Integer) 672, mutation.getEntrezGeneId());
        });

    }
    
    @Test
    public void getMetaMutationsInMultipleMolecularProfiles() {
        var allMolecularProfileIds = List.of("study_tcga_pub_mutations");
        var allSampleIds = List.of("tcga-a1-a0sh-01");
        var entrezGeneIds = List.of(672);
        

        var result = clickhouseMutationMapper.getMetaMutationsInMultipleMolecularProfiles(
            allMolecularProfileIds, allSampleIds, entrezGeneIds, false);

        assertEquals((Integer) 2, result.getTotalCount());
        assertEquals((Integer) 1, result.getSampleCount());
    }

    @Test
    public void getMetaMutationsInMultipleMolecularProfilesSampleIdEmpty() {
        var allMolecularProfileIds = List.of("study_tcga_pub_mutations");
        var allSampleIds = new ArrayList<String>();
        var entrezGeneIds = List.of(672);


        var result = clickhouseMutationMapper.getMetaMutationsInMultipleMolecularProfiles(
            allMolecularProfileIds, allSampleIds, entrezGeneIds, false);

        assertEquals((Integer) 5, result.getTotalCount());
        assertEquals((Integer) 4, result.getSampleCount());
    }
}