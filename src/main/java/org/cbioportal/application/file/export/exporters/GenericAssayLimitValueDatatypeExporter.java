package org.cbioportal.application.file.export.exporters;

import org.cbioportal.application.file.export.services.GeneticProfileDataService;
import org.cbioportal.application.file.export.services.GeneticProfileService;
import org.cbioportal.application.file.model.*;
import org.cbioportal.application.file.utils.CloseableIterator;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;
import java.util.SequencedMap;

public class GenericAssayLimitValueDatatypeExporter extends GeneticProfileDatatypeExporter {

    private final GeneticProfileDataService geneticProfileDataService;

    public GenericAssayLimitValueDatatypeExporter(GeneticProfileService geneticProfileService, GeneticProfileDataService geneticProfileDataService) {
        super(geneticProfileService);
        this.geneticProfileDataService = geneticProfileDataService;
    }

    @Override
    protected Exporter composeExporterFor(GeneticProfileDatatypeMetadata metadata) {
        return new LimitValueGenericProfileExporter(metadata);
    }

    @Override
    protected String getGeneticAlterationType() {
        return "GENERIC_ASSAY";
    }

    @Override
    protected String getDatatype() {
        return "LIMIT-VALUE";
    }

    private class LimitValueGenericProfileExporter extends GeneticProfileExporter {
        private final GeneticProfileDatatypeMetadata metatdata;

        public LimitValueGenericProfileExporter(GeneticProfileDatatypeMetadata metadata) {
            this.metatdata = metadata;
            this.metatdata.setGenericEntitiesMetaProperties(
                geneticProfileDataService.getGenericEntityMetaProperties(metadata.getStableId()));
        }

        @Override
        protected Optional<GeneticProfileDatatypeMetadata> getMetadata(String studyId) {
            return Optional.of(metatdata);
        }

        @Override
        protected CloseableIterator<SequencedMap<String, String>> getData(String studyId) {
            var sampleStableIds = geneticProfileDataService.getSampleStableIds(metatdata.getStableId());
            for (String sampleStableId : sampleStableIds) {
                if (sampleStableId == null) {
                    throw new IllegalStateException("Sample stable ID is null");
                }
            }
            var geneticProfileData = geneticProfileDataService.getData(metatdata.getStableId());
            return new Table(composeRows(geneticProfileData, sampleStableIds, metatdata.getGenericEntitiesMetaProperties()));
        }

        private static CloseableIterator<TableRow> composeRows(CloseableIterator<GeneticProfileData> geneticProfileData, List<String> sampleStableIds, List<String> genericEntitiesMetaProperties) {
            return new CloseableIterator<>() {
                @Override
                public void close() throws IOException {
                    geneticProfileData.close();
                }

                @Override
                public boolean hasNext() {
                    return geneticProfileData.hasNext();
                }

                @Override
                public TableRow next() {
                    var data = geneticProfileData.next();
                    if (data.getValues().size() != sampleStableIds.size()) {
                        throw new IllegalStateException("Number of values does not match number of sample stable IDs");
                    }
                    return () -> {
                        var row = new LinkedHashMap<String, String>();
                        row.put("ENTITY_STABLE_ID", data.getGeneticEntity() == null ? null : data.getGeneticEntity().getStableId());
                        for (String property : genericEntitiesMetaProperties) {
                            row.put(property, data.getProperties() == null ? null : data.getProperties().stream().filter(p -> property.equals(p.getName())).map(GeneticEntityProperty::getValue).findFirst().orElse(null));
                        }
                        for (int i = 0; i < sampleStableIds.size(); i++) {
                            row.put(sampleStableIds.get(i), data.getValues().get(i));
                        }
                        return row;
                    };
                }
            };
        }
    }
}
