package org.cbioportal.application.file.export.exporters;

import org.cbioportal.application.file.export.services.GeneticProfileService;
import org.cbioportal.application.file.export.services.MafRecordService;
import org.cbioportal.application.file.model.GeneticProfileDatatypeMetadata;
import org.cbioportal.application.file.model.MafRecord;
import org.cbioportal.application.file.model.Table;
import org.cbioportal.application.file.utils.CloseableIterator;

import java.util.Optional;

public class MafDataTypeExporter extends GeneticProfileDatatypeExporter {

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
    protected String getGeneticAlterationType() {
        return "MUTATION_EXTENDED";
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
        protected Optional<GeneticProfileDatatypeMetadata> getMetadata(String studyId) {
            return Optional.of(metadata);
        }

        @Override
        protected Table getData(String studyId) {
            CloseableIterator<MafRecord> mafRecords = mafRecordService.getMafRecords(metadata.getStableId());
            return new Table(mafRecords, MafRecord.getHeader());
        }
    }
}
