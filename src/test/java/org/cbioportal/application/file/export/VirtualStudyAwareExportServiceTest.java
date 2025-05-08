package org.cbioportal.application.file.export;

import org.cbioportal.application.file.export.exporters.ExportDetails;
import org.cbioportal.application.file.export.services.ExportService;
import org.cbioportal.application.file.export.services.VirtualStudyAwareExportService;
import org.cbioportal.legacy.service.VirtualStudyService;
import org.cbioportal.legacy.web.parameter.VirtualStudy;
import org.cbioportal.legacy.web.parameter.VirtualStudyData;
import org.cbioportal.legacy.web.parameter.VirtualStudySamples;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class VirtualStudyAwareExportServiceTest {

    private final VirtualStudyService virtualStudyService = mock(VirtualStudyService.class);
    private final ExportService exportService = mock(ExportService.class);
    private final VirtualStudyAwareExportService service = new VirtualStudyAwareExportService(virtualStudyService, exportService);

    @Test
    public void testIsStudyExportableWithVirtualStudy() {
        VirtualStudy virtualStudy = new VirtualStudy();
        VirtualStudyData data = new VirtualStudyData();
        VirtualStudySamples sample1 = new VirtualStudySamples();
        sample1.setId("STUDY_1");
        sample1.setSamples(Set.of());
        data.setStudies(Set.of(sample1));
        virtualStudy.setData(data);

        when(virtualStudyService.getVirtualStudyByIdIfExists("VIRTUAL_STUDY_ID"))
            .thenReturn(Optional.of(virtualStudy));
        when(exportService.isStudyExportable("STUDY_1")).thenReturn(true);

        assertTrue(service.isStudyExportable("VIRTUAL_STUDY_ID"));
        verify(exportService).isStudyExportable("STUDY_1");
    }

    @Test
    public void testIsStudyExportableWithoutVirtualStudy() {
        when(virtualStudyService.getVirtualStudyByIdIfExists("STUDY_ID"))
            .thenReturn(Optional.empty());
        when(exportService.isStudyExportable("STUDY_ID")).thenReturn(true);

        assertTrue(service.isStudyExportable("STUDY_ID"));
        verify(exportService).isStudyExportable("STUDY_ID");
    }

    @Test
    public void testExportDataForSingleStudy() {
        VirtualStudy virtualStudy = new VirtualStudy();
        VirtualStudyData data = new VirtualStudyData();
        data.setName("Virtual Study");
        data.setDescription("Description");
        data.setPmid("12345");
        data.setTypeOfCancerId("TYPE");
        VirtualStudySamples sample1 = new VirtualStudySamples();
        sample1.setId("STUDY_1");
        sample1.setSamples(Set.of("SAMPLE_1", "SAMPLE_2"));
        data.setStudies(Set.of(sample1));
        virtualStudy.setData(data);

        var factory = new InMemoryFileWriterFactory();

        when(virtualStudyService.getVirtualStudyByIdIfExists("VIRTUAL_STUDY_ID"))
            .thenReturn(Optional.of(virtualStudy));
        when(exportService.exportData(eq(factory), eq(
            new ExportDetails(
                "STUDY_1",
                "VIRTUAL_STUDY_ID",
                Set.of("SAMPLE_1", "SAMPLE_2"),
                "Virtual Study",
                "Description",
                "12345",
                "TYPE"
            )))).thenReturn(true);

        boolean result = service.exportData(factory, new ExportDetails("VIRTUAL_STUDY_ID"));

        assertTrue(result);
        var fileContents = factory.getFileContents();
        assertTrue(fileContents.containsKey("virtual_study_definition.json"));
        verify(exportService).exportData(eq(factory), any(ExportDetails.class));
    }

    @Test
    public void testExportDataForMultipleStudies() {
        VirtualStudy virtualStudy = new VirtualStudy();
        VirtualStudyData data = new VirtualStudyData();
        VirtualStudySamples sample1 = new VirtualStudySamples();
        sample1.setId("STUDY_1");
        sample1.setSamples(Set.of("SAMPLE_1"));
        VirtualStudySamples sample2 = new VirtualStudySamples();
        sample2.setId("STUDY_2");
        sample2.setSamples(Set.of("SAMPLE_2"));
        data.setStudies(Set.of(
            sample1,
            sample2
        ));
        virtualStudy.setData(data);

        var factory = new InMemoryFileWriterFactory();

        when(virtualStudyService.getVirtualStudyByIdIfExists("VIRTUAL_STUDY_ID"))
            .thenReturn(Optional.of(virtualStudy));
        when(exportService.exportData(eq(factory), eq(
            new ExportDetails(
                "STUDY_1",
                "VIRTUAL_STUDY_ID_STUDY_1",
                Set.of("SAMPLE_1")
            )))).thenReturn(true);
        when(exportService.exportData(eq(factory), eq(
            new ExportDetails(
                "STUDY_2",
                "VIRTUAL_STUDY_ID_STUDY_2",
                Set.of("SAMPLE_2")
            )))).thenReturn(true);
        boolean result = service.exportData(factory, new ExportDetails("VIRTUAL_STUDY_ID"));

        assertTrue(result);
        var fileContents = factory.getFileContents();
        assertTrue(fileContents.containsKey("virtual_study_definition.json"));
        verify(exportService, times(2)).exportData(eq(factory), any(ExportDetails.class));
    }

    @Test
    public void testExportDataWithoutVirtualStudy() {
        when(virtualStudyService.getVirtualStudyByIdIfExists("STUDY_ID"))
            .thenReturn(Optional.empty());
        when(exportService.exportData(any(), any())).thenReturn(true);

        var factory = new InMemoryFileWriterFactory();
        boolean result = service.exportData(factory, new ExportDetails("STUDY_ID"));

        assertTrue(result);
        verify(exportService).exportData(eq(factory), any(ExportDetails.class));
    }
}