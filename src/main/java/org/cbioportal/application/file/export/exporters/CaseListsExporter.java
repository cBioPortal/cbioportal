package org.cbioportal.application.file.export.exporters;

import org.cbioportal.application.file.export.services.CaseListMetadataService;
import org.cbioportal.application.file.export.writers.KeyValueMetadataWriter;
import org.cbioportal.application.file.model.CaseListMetadata;
import org.cbioportal.application.file.utils.FileWriterFactory;

import java.io.IOException;
import java.io.Writer;
import java.util.List;

public class CaseListsExporter implements Exporter {

    private final CaseListMetadataService caseListMetadataService;

    public CaseListsExporter(CaseListMetadataService caseListMetadataService) {
        this.caseListMetadataService = caseListMetadataService;
    }

    @Override
    public boolean exportData(FileWriterFactory fileWriterFactory, String studyId) {
        List<CaseListMetadata> sampleLists = caseListMetadataService.getCaseListsMetadata(studyId);
        boolean exported = false;
        for (CaseListMetadata sampleList : sampleLists) {
            //we skip this one as we have addGlobalCaseList=true for study
            if (sampleList.getStableId().endsWith("_all")) {
                continue;
            }
            try (Writer caseListWriter = fileWriterFactory.newWriter("case_lists/cases_" + sampleList.getStableId().replace(studyId + "_", "") + ".txt")) {
                new KeyValueMetadataWriter(caseListWriter).write(new CaseListMetadata(studyId, sampleList.getStableId(),
                    sampleList.getName(), sampleList.getDescription(), sampleList.getSampleIds()));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            exported = true;
        } 
        return exported;
    }
}
