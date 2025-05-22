package org.cbioportal.legacy.persistence.virtualstudy;

import org.cbioportal.legacy.model.Patient;
import org.cbioportal.legacy.model.Sample;
import org.cbioportal.legacy.persistence.SampleRepository;
import org.cbioportal.legacy.service.CancerTypeService;
import org.cbioportal.legacy.service.VirtualStudyService;
import org.cbioportal.legacy.service.impl.VirtualStudyServiceImpl;
import org.cbioportal.legacy.service.util.SessionServiceRequestHandler;
import org.cbioportal.legacy.web.parameter.VirtualStudy;
import org.cbioportal.legacy.web.util.StudyViewFilterApplier;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

public class VSAwareSampleRepositoryTests {
    final VirtualStudyService virtualStudyService = spy(new VirtualStudyServiceImpl(mock(CancerTypeService.class), mock(SessionServiceRequestHandler.class), mock(StudyViewFilterApplier.class)));
    final SampleRepository sampleRepository = mock(SampleRepository.class);
    final VSAwareSampleRepository testee = new VSAwareSampleRepository(virtualStudyService, sampleRepository);

    @Test
    public void testFetchSamples_materialised_studies_only() {
        Sample sample = new Sample();
        sample.setStableId("SAMPLE1");
        List<Sample> returnSamples = List.of(sample);
        when(sampleRepository.fetchSamples(List.of("STUDY1"), List.of("SAMPLE1"), "ID")).thenReturn(returnSamples);
        VirtualStudy vs = mock(VirtualStudy.class);
        doReturn("VS1")
            .when(vs)
            .getId();
        doReturn(List.of(vs))
            .when(virtualStudyService)
            .getPublishedVirtualStudies();

        List<Sample> returnedSamples = testee.fetchSamples(List.of("STUDY1"), List.of("SAMPLE1"), "ID");

        assertEquals(returnSamples, returnedSamples);
    }

    @Test
    public void testFetchSamples_virtual_studies_only() {
        Sample sample = new Sample();
        sample.setInternalId(1);
        sample.setStableId("SAMPLE1");
        sample.setSampleType(Sample.SampleType.METASTATIC);
        sample.setPatientId(2);
        sample.setPatientStableId("PATIENT1");
        Patient patient = new Patient();
        patient.setInternalId(2);
        patient.setStableId("PATIENT1");
        patient.setCancerStudyId(3);
        patient.setCancerStudyIdentifier("STUDY1");
        sample.setPatient(patient);
        List<Sample> returnSamples = List.of(sample);
        when(sampleRepository.fetchSamples(List.of("STUDY1"), List.of("SAMPLE1"), "ID")).thenReturn(returnSamples);
        VirtualStudy vs = mock(VirtualStudy.class);
        doReturn("VS1")
            .when(vs)
            .getId();
        doReturn(List.of(vs))
            .when(virtualStudyService)
            .getPublishedVirtualStudies();

        List<Sample> returnedSamples = testee.fetchSamples(List.of("VS1"), List.of("STUDY1_SAMPLE1"), "ID");
        assertNotNull(returnedSamples);
        assertEquals(1, returnedSamples.size());
        Sample returnedSample = returnedSamples.getFirst();
        assertNotNull(returnedSample);
        assertNull(returnedSample.getInternalId()); //We don't preserve the internal Ids in the virtual study to fail if they are used for virtual studies
        assertEquals("STUDY1_SAMPLE1", returnedSample.getStableId());
        assertEquals(Sample.SampleType.METASTATIC, returnedSample.getSampleType());
        assertNull(returnedSample.getPatientId()); //We don't preserve the internal Ids in the virtual study to fail if they are used for virtual studies
        assertEquals("PATIENT1", returnedSample.getPatientStableId());
    }
}
