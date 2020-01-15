
package org.cbioportal.persistence.mybatis;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.cbioportal.model.meta.GenericAssayMeta;
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
public class GenericAssayMyBatisRepositoryTest {

    @Autowired
    private GenericAssayMyBatisRepository genericAssayMyBatisRepository;

    @Test
    public void getGenericAssayMeta() {
        List<String> stableIds = Arrays.asList("Nmut", "mean_1");
        List<GenericAssayMeta> result = genericAssayMyBatisRepository.getGenericAssayMeta(stableIds);
        Assert.assertEquals(2, result.size());
    }

    @Test
    public void getGeneticEntityIdByStableId() {
        int result = genericAssayMyBatisRepository.getGeneticEntityIdByStableId("Nmut");
        Assert.assertNotNull(result);
        Assert.assertEquals(28, result);
    }

    @Test
    public void getGenericAssayMetaPropertiesMap() {
        List<HashMap<String, String>> result = genericAssayMyBatisRepository.getGenericAssayMetaPropertiesMap(28);
        Assert.assertNotNull(result);
        Assert.assertEquals(2, result.size());
        HashMap<String, String> map1 = result.get(0);
        HashMap<String, String> map2 = result.get(1);

        for (String key : map1.keySet()) {
            if (key.equals("key")) {
                Assert.assertEquals("description",map1.get(key));
            }
            if (key.equals("value")) {
                Assert.assertEquals("number of mutations",map1.get(key));
            }
        }

        for (String key : map2.keySet()) {
            if (key.equals("key")) {
                Assert.assertEquals("name",map2.get(key));
            }
            if (key.equals("value")) {
                Assert.assertEquals("Nmut",map2.get(key));
            }
        }
    }

}