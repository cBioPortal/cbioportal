package org.cbioportal.application.file.export.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.cbioportal.application.file.export.exporters.ExportDetails;
import org.cbioportal.application.file.export.exporters.Exporter;
import org.cbioportal.application.file.utils.FileWriterFactory;
import org.cbioportal.legacy.service.util.SessionServiceRequestHandler;
import org.cbioportal.legacy.web.parameter.VirtualStudy;
import org.cbioportal.legacy.web.parameter.VirtualStudySamples;

import java.io.IOException;
import java.io.Writer;
import java.util.Set;

public class VirtualStudyAwareExportService implements Exporter {

    private final SessionServiceRequestHandler sessionServiceRequestHandler;
    private final ExportService exportService;

    public VirtualStudyAwareExportService(SessionServiceRequestHandler sessionServiceRequestHandler, ExportService exportService) {
        this.sessionServiceRequestHandler = sessionServiceRequestHandler;
        this.exportService = exportService;
    }

    public boolean isStudyExportable(String studyId) {
        var virtualStudy = sessionServiceRequestHandler.getVirtualStudyByIdIfExists(studyId);
        return virtualStudy
                .map(study -> study.getData()
                        .getStudies().stream().map(VirtualStudySamples::getId)
                        .allMatch(exportService::isStudyExportable))
                .orElseGet(() -> exportService.isStudyExportable(studyId));
    }

    @Override
    public boolean exportData(FileWriterFactory fileWriterFactory, ExportDetails exportDetails) {
        var virtualStudyOpt = sessionServiceRequestHandler.getVirtualStudyByIdIfExists(exportDetails.getStudyId());
        if (virtualStudyOpt.isEmpty()) {
            return exportService.exportData(fileWriterFactory, exportDetails);
        } else {
            VirtualStudy virtualStudy = virtualStudyOpt.get();
            writeVirtualStudyDefinitionJsonFile(fileWriterFactory, virtualStudy);
            Set<VirtualStudySamples> virtualStudySamples = virtualStudy.getData().getStudies();
            if (virtualStudySamples.size() == 1) {
                VirtualStudySamples virtualStudySample = virtualStudySamples.iterator().next();
                ExportDetails newExportDetails = new ExportDetails(virtualStudySample.getId(), exportDetails.getStudyId(), virtualStudySample.getSamples());
                return exportService.exportData(fileWriterFactory, newExportDetails);
            } else {
                boolean allStudiesExported = true;
                for (VirtualStudySamples virtualStudySample : virtualStudySamples) {
                    String exportAsStudyId = exportDetails.getStudyId() + "_" + virtualStudySample.getId();
                    fileWriterFactory.setBasePath(exportAsStudyId);
                    ExportDetails newExportDetails = new ExportDetails(exportDetails.getStudyId(),
                            exportAsStudyId,
                            virtualStudySample.getSamples());
                    allStudiesExported &= exportService.exportData(
                            fileWriterFactory,
                            newExportDetails);
                }
                fileWriterFactory.setBasePath(null);
                return allStudiesExported;
            }
        }
    }

    private static void writeVirtualStudyDefinitionJsonFile(FileWriterFactory fileWriterFactory, VirtualStudy virtualStudy) {
        try (Writer writer = fileWriterFactory.newWriter("virtual_study_definition.json")) {
            ObjectMapper mapper = new ObjectMapper();
            String json = mapper.writeValueAsString(virtualStudy);
            writer.write(json);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
