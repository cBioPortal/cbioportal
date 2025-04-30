package org.cbioportal.application.file.export.exporters;

import org.cbioportal.application.file.export.services.GeneticProfileService;
import org.cbioportal.application.file.export.services.MafRecordService;
import org.cbioportal.application.file.model.GeneticProfileDatatypeMetadata;
import org.cbioportal.application.file.model.MafRecord;
import org.cbioportal.application.file.model.Table;
import org.cbioportal.application.file.utils.CloseableIterator;

import java.util.Optional;
import java.util.Set;

public abstract class MafDataTypeExporter extends GeneticProfileDatatypeExporter {

    private final MafRecordService mafRecordService;

    public MafDataTypeExporter(GeneticProfileService geneticProfileService, MafRecordService mafRecordService) {
        super(geneticProfileService);
        this.mafRecordService = mafRecordService;
    }

    @Override
    protected Exporter composeExporterFor(GeneticProfileDatatypeMetadata metadata) {
        return new MAFGeneticProfileExporter(metadata);
    }

    @Override
    protected String getDatatype() {
        return "MAF";
    }

    private class MAFGeneticProfileExporter extends GeneticProfileExporter {

        private final GeneticProfileDatatypeMetadata metadata;

        public MAFGeneticProfileExporter(GeneticProfileDatatypeMetadata metadata) {
            this.metadata = metadata;
        }

        @Override
        protected Optional<GeneticProfileDatatypeMetadata> getMetadata(String studyId, Set<String> sampleIds) {
            return Optional.of(metadata);
        }

        @Override
        protected Table getData(String studyId, Set<String> sampleIds) {
            CloseableIterator<MafRecord> mafRecords = mafRecordService.getMafRecords(metadata.getStableId(), sampleIds);
            return new Table(mafRecords, MafRecord.getHeader());
        }
    }
}
