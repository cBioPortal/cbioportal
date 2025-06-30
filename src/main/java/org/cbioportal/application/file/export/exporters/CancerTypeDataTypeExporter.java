package org.cbioportal.application.file.export.exporters;

import org.cbioportal.application.file.export.services.CancerStudyMetadataService;
import org.cbioportal.application.file.model.CancerType;
import org.cbioportal.application.file.model.ClinicalAttributesMetadata;
import org.cbioportal.application.file.model.Table;
import org.cbioportal.application.file.model.TableRow;
import org.cbioportal.application.file.utils.CloseableIterator;

import java.util.List;
import java.util.Optional;

public class CancerTypeDataTypeExporter extends DataTypeExporter<ClinicalAttributesMetadata, Table> {

    private final CancerStudyMetadataService cancerStudyMetadataService;

    public CancerTypeDataTypeExporter(CancerStudyMetadataService cancerStudyMetadataService) {
        this.cancerStudyMetadataService = cancerStudyMetadataService;
    }

    @Override
    protected Optional<ClinicalAttributesMetadata> getMetadata(String studyId) {
        return Optional.of(new ClinicalAttributesMetadata(
            studyId,
            "CANCER_TYPE",
            "CANCER_TYPE"
        ));
    }

    @Override
    public String getDataFilename(ClinicalAttributesMetadata metadata) {
        return "data_cancer_type.txt";
    }

    @Override
    public String getMetaFilename(ClinicalAttributesMetadata metadata) {
        return "meta_cancer_type.txt";
    }

    @Override
    protected Table getData(String studyId) {
        List<CancerType> cancerTypes = cancerStudyMetadataService.getCancerTypeHierarchy(studyId);
        var iterator = cancerTypes.iterator();
        return new Table(new CloseableIterator<>() {
            @Override
            public void close() {
            }

            @Override
            public boolean hasNext() {
                return iterator.hasNext();
            }

            @Override
            public TableRow next() {
                return iterator.next();
            }
        });
    }
}
