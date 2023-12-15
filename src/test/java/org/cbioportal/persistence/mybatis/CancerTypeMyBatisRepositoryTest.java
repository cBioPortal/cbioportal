package org.cbioportal.persistence.mybatis;

import java.util.List;
import org.cbioportal.model.TypeOfCancer;
import org.cbioportal.model.meta.BaseMeta;
import org.cbioportal.persistence.mybatis.config.TestConfig;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = {CancerTypeMyBatisRepository.class, TestConfig.class})
public class CancerTypeMyBatisRepositoryTest {

    @Autowired
    private CancerTypeMyBatisRepository cancerTypeMyBatisRepository;

    @Test
    public void getAllCancerTypesIdProjection() throws Exception {

        List<TypeOfCancer> result = cancerTypeMyBatisRepository.getAllCancerTypes("ID", null, null, null, null);

        Assert.assertEquals(2, result.size());
        TypeOfCancer typeOfCancer = result.get(0);
        Assert.assertEquals("acc", typeOfCancer.getTypeOfCancerId());
    }

    @Test
    public void getAllCancerTypesSummaryProjection() throws Exception {

        List<TypeOfCancer> result = cancerTypeMyBatisRepository.getAllCancerTypes("SUMMARY", null, null, null, null);

        Assert.assertEquals(2, result.size());
        TypeOfCancer typeOfCancer = result.get(0);
        Assert.assertEquals("brca", typeOfCancer.getTypeOfCancerId());
        Assert.assertEquals("Breast Invasive Carcinoma", typeOfCancer.getName());
        Assert.assertEquals("HotPink", typeOfCancer.getDedicatedColor());
        Assert.assertEquals("Breast", typeOfCancer.getShortName());
        Assert.assertEquals("tissue", typeOfCancer.getParent());
    }

    @Test
    public void getAllCancerTypesDetailedProjection() throws Exception {

        List<TypeOfCancer> result = cancerTypeMyBatisRepository.getAllCancerTypes("DETAILED", null, null, null, null);

        Assert.assertEquals(2, result.size());
        TypeOfCancer typeOfCancer = result.get(0);
        Assert.assertEquals("brca", typeOfCancer.getTypeOfCancerId());
        Assert.assertEquals("Breast Invasive Carcinoma", typeOfCancer.getName());
        Assert.assertEquals("HotPink", typeOfCancer.getDedicatedColor());
        Assert.assertEquals("Breast", typeOfCancer.getShortName());
        Assert.assertEquals("tissue", typeOfCancer.getParent());
    }

    @Test
    public void getAllCancerTypesSummaryProjection1PageSize() throws Exception {

        List<TypeOfCancer> result = cancerTypeMyBatisRepository.getAllCancerTypes("SUMMARY", 1, 0, null, null);

        Assert.assertEquals(1, result.size());
    }

    @Test
    public void getAllCancerTypesSummaryProjectionNameSort() throws Exception {

        List<TypeOfCancer> result = cancerTypeMyBatisRepository.getAllCancerTypes("SUMMARY", null, null, "name", "ASC");

        Assert.assertEquals(2, result.size());
        Assert.assertEquals("Adrenocortical Carcinoma", result.get(0).getName());
        Assert.assertEquals("Breast Invasive Carcinoma", result.get(1).getName());
    }

    @Test
    public void getMetaCancerTypes() throws Exception {

        BaseMeta result = cancerTypeMyBatisRepository.getMetaCancerTypes();

        Assert.assertEquals((Integer)2, result.getTotalCount());
    }

    @Test
    public void getCancerTypeNullResult() throws Exception {

        TypeOfCancer result = cancerTypeMyBatisRepository.getCancerType("invalid_cancer_type");

        Assert.assertNull(result);
    }

    @Test
    public void getCancerType() throws Exception {

        TypeOfCancer result = cancerTypeMyBatisRepository.getCancerType("acc");

        Assert.assertEquals("acc", result.getTypeOfCancerId());
        Assert.assertEquals("Adrenocortical Carcinoma", result.getName());
        Assert.assertEquals("Purple", result.getDedicatedColor());
        Assert.assertEquals("ACC", result.getShortName());
        Assert.assertEquals("adrenal_gland", result.getParent());
    }
}