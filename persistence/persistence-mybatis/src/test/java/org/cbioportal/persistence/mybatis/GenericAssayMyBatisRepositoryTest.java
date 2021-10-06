
package org.cbioportal.persistence.mybatis;

import java.util.Arrays;
import java.util.List;
import org.cbioportal.model.GenericAssayAdditionalProperty;
import org.cbioportal.model.meta.GenericAssayMeta;
import org.cbioportal.persistence.mybatis.config.TestConfig;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = {GenericAssayMyBatisRepository.class, TestConfig.class})
public class GenericAssayMyBatisRepositoryTest {

    @Autowired
    private GenericAssayMyBatisRepository genericAssayMyBatisRepository;

    @Test
    public void getGenericAssayMeta() {
        List<String> stableIds = Arrays.asList("mean_1", "mean_2");
        List<GenericAssayMeta> result = genericAssayMyBatisRepository.getGenericAssayMeta(stableIds);
        Assert.assertEquals(2, result.size());
    }

    @Test
    public void getGenericAssayAdditionalproperties() {
        List<String> stableIds = Arrays.asList("mean_1", "mean_2");
        List<GenericAssayAdditionalProperty> result = genericAssayMyBatisRepository.getGenericAssayAdditionalproperties(stableIds);
        Assert.assertNotNull(result);
        Assert.assertEquals(4, result.size());

        for (GenericAssayAdditionalProperty additionalProperty : result) {
            if (additionalProperty.getStableId().equals("mean_1")) {
                if (additionalProperty.getName().equals("name")) {
                    Assert.assertEquals("mean_1",additionalProperty.getValue());
                } else {
                    Assert.assertEquals("description of mean_1",additionalProperty.getValue());
                }
            }
            else if (additionalProperty.getStableId().equals("mean_2")) {
                if (additionalProperty.getName().equals("name")) {
                    Assert.assertEquals("mean_2",additionalProperty.getValue());
                } else {
                    Assert.assertEquals("description of mean_2",additionalProperty.getValue());
                }
            }
        }
    }

}