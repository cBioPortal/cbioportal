package org.cbioportal.service.impl;

import org.cbioportal.persistence.ClinicalAttributeRepository;
import org.cbioportal.persistence.ClinicalDataRepository;
import org.cbioportal.persistence.SampleRepository;
import org.cbioportal.service.ClinicalAttributeService;
import org.cbioportal.service.ClinicalDataService;
import org.cbioportal.service.SampleService;
import org.cbioportal.service.exception.FederationException;
import org.cbioportal.web.util.ClinicalDataBinUtil;
import org.cbioportal.web.util.ClinicalDataFetcher;
import org.cbioportal.web.util.StudyViewFilterApplier;
import org.cbioportal.web.util.StudyViewFilterUtil;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class FederatedDataSourceServiceTest {
    
    // NOTE: see ClinicalDataBinUtilTest for another example of a class where
    // we are mocking out deeply nested dependencies, rather than direct subfields
    // of the class under test.
    
    @Spy
    @InjectMocks
    private FederatedDataSourceService federatedDataSourceService;
    
    // Injected fields -- real implementation
    // In order for @Spy / @InjectMocks to work, it's important that these are *classes*, not interfaces.

    @Spy
    @InjectMocks
    private ClinicalDataServiceImpl clinicalDataService;

    @Spy
    @InjectMocks
    private ClinicalAttributeServiceImpl clinicalAttributeService;

    // no dependencies
    @Spy
    private StudyViewFilterUtil studyViewFilterUtil;

    @Spy
    @InjectMocks
    private StudyViewFilterApplier studyViewFilterApplier;
    
    @Spy
    @InjectMocks
    private SampleServiceImpl sampleService;

    @Spy
    @InjectMocks
    private ClinicalDataBinUtil clinicalDataBinUtil;
    
    @Spy
    @InjectMocks
    private ClinicalDataFetcher clinicalDataFetcher;
    
    // Injected fields -- we will mock these out

    @Mock
    private ClinicalDataRepository clinicalDataRepository;

    @Mock
    private ClinicalAttributeRepository clinicalAttributeRepository;

    @Mock
    private SampleRepository sampleRepository;
    
    @Before
    public void setup() {
        // This is necessary for initializing Mockito mocks pre-JUnit 4.5
        // See: https://stackoverflow.com/a/15494996/4077294
        MockitoAnnotations.initMocks(this);
    }
    
    @Test
    public void fetchClinicalAttributes() throws FederationException {
        var result = this.federatedDataSourceService.fetchClinicalAttributes();

        assertEquals(new ArrayList<>(), result);
    }
}
