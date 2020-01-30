package org.cbioportal.persistence.mybatis;

import java.util.ArrayList;
import java.util.List;
import org.cbioportal.model.CosmicMutation;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("/testContextDatabase.xml")
@Configurable
public class CosmicCountMyBatisRepositoryTest {
    @Autowired
    private CosmicCountMyBatisRepository cosmicCountMyBatisRepository;

    @Test
    public void getCosmicCountsByKeywords() {
        List<String> keywords = new ArrayList<>();
        keywords.add("OR4F5 D45 missense");
        keywords.add("SAMD11 P23 silent");

        List<CosmicMutation> result = cosmicCountMyBatisRepository.fetchCosmicCountsByKeywords(
            keywords
        );
        Assert.assertEquals(2, result.size());
        CosmicMutation cosmicMutation1 = result.get(0);
        Assert.assertEquals("3677745", cosmicMutation1.getCosmicMutationId());
        Assert.assertEquals("D45A", cosmicMutation1.getProteinChange());
        Assert.assertEquals("OR4F5 D45 missense", cosmicMutation1.getKeyword());
        Assert.assertEquals((Integer) 1, cosmicMutation1.getCount());
        CosmicMutation cosmicMutation2 = result.get(1);
        Assert.assertEquals("460103", cosmicMutation2.getCosmicMutationId());
        Assert.assertEquals("P23P", cosmicMutation2.getProteinChange());
        Assert.assertEquals("SAMD11 P23 silent", cosmicMutation2.getKeyword());
        Assert.assertEquals((Integer) 1, cosmicMutation2.getCount());
    }
}
