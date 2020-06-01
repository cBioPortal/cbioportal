package org.cbioportal.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.cbioportal.model.TypeOfCancer;
import org.cbioportal.persistence.CancerTypeRepository;
import org.cbioportal.service.impl.ServerStatusServiceImpl.ServerStatusMessage;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ServerStatusServiceImplTest extends BaseServiceImplTest {

    @InjectMocks
    private ServerStatusServiceImpl serverStatusService;

    @Mock
    private CancerTypeRepository cancerTypeRepository;

    @Test
    public void getServerStatusSuccess() throws Exception {

        List<TypeOfCancer> cancerList = new ArrayList<>();
        TypeOfCancer typeOfCancer = new TypeOfCancer();
        cancerList.add(typeOfCancer);

        Mockito.when(cancerTypeRepository.getAllCancerTypes("SUMMARY", null, null, null, null))
                .thenReturn(cancerList);

        Assert.assertEquals(ServerStatusServiceImpl.MESSAGE_RUNNING, serverStatusService.getServerStatus().status);
    }

    @Test
    public void getServerStatusFailure() throws Exception {

        List<TypeOfCancer> cancerList = new ArrayList<>();

        Mockito.when(cancerTypeRepository.getAllCancerTypes("SUMMARY", null, null, null, null))
                .thenReturn(cancerList);

        Assert.assertEquals(ServerStatusServiceImpl.MESSAGE_DOWN, serverStatusService.getServerStatus().status);
    }

}