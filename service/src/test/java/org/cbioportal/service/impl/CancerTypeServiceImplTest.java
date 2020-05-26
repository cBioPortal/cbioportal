package org.cbioportal.service.impl;

import org.cbioportal.model.TypeOfCancer;
import org.cbioportal.model.meta.BaseMeta;
import org.cbioportal.persistence.CancerTypeRepository;
import org.cbioportal.service.exception.CancerTypeNotFoundException;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.List;

@RunWith(MockitoJUnitRunner.class)
public class CancerTypeServiceImplTest extends BaseServiceImplTest {

    @InjectMocks
    private CancerTypeServiceImpl cancerTypeService;

    @Mock
    private CancerTypeRepository cancerTypeRepository;

    @Test
    public void getAllCancerTypes() throws Exception {

        List<TypeOfCancer> expectedTypeOfCancerList = new ArrayList<>();
        TypeOfCancer typeOfCancer = new TypeOfCancer();
        expectedTypeOfCancerList.add(typeOfCancer);

        Mockito.when(cancerTypeRepository.getAllCancerTypes(PROJECTION, PAGE_SIZE, PAGE_NUMBER, SORT, DIRECTION))
                .thenReturn(expectedTypeOfCancerList);

        List<TypeOfCancer> result = cancerTypeService.getAllCancerTypes(PROJECTION, PAGE_SIZE, PAGE_NUMBER, SORT,
                DIRECTION);

        Assert.assertEquals(expectedTypeOfCancerList, result);
    }

    @Test
    public void getMetaCancerTypes() throws Exception {

        BaseMeta expectedBaseMeta = new BaseMeta();

        Mockito.when(cancerTypeRepository.getMetaCancerTypes()).thenReturn(expectedBaseMeta);

        BaseMeta result = cancerTypeService.getMetaCancerTypes();

        Assert.assertEquals(expectedBaseMeta, result);
    }

    @Test(expected = CancerTypeNotFoundException.class)
    public void getCancerTypeNotFound() throws Exception {

        Mockito.when(cancerTypeRepository.getCancerType(CANCER_TYPE_ID)).thenReturn(null);

        cancerTypeService.getCancerType(CANCER_TYPE_ID);
    }

    @Test
    public void getCancerType() throws Exception {

        TypeOfCancer expectedTypeOfCancer = new TypeOfCancer();

        Mockito.when(cancerTypeRepository.getCancerType(CANCER_TYPE_ID)).thenReturn(expectedTypeOfCancer);

        TypeOfCancer result = cancerTypeService.getCancerType(CANCER_TYPE_ID);

        Assert.assertEquals(expectedTypeOfCancer, result);
    }
}