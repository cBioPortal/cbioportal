package org.cbioportal.application.file.export.exporters;

import com.google.common.collect.Iterators;
import com.google.common.collect.PeekingIterator;
import org.cbioportal.application.file.export.services.GeneticProfileDataService;
import org.cbioportal.application.file.export.services.GeneticProfileService;
import org.cbioportal.application.file.model.GenericEntityProperty;
import org.cbioportal.application.file.model.GeneticProfileData;
import org.cbioportal.application.file.model.GeneticProfileDatatypeMetadata;
import org.cbioportal.application.file.model.Table;
import org.cbioportal.application.file.model.TableRow;
import org.cbioportal.application.file.utils.CloseableIterator;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.SequencedMap;
import java.util.function.Function;

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

    private static final LinkedHashMap<String, Function<GeneticProfileData, String>> ROW = new LinkedHashMap<>();

    static {
        ROW.put("ENTITY_STABLE_ID", data -> data.getGeneticEntity() == null ? null : data.getGeneticEntity().getStableId());
    }
    private class LimitValueGenericProfileExporter extends GeneticProfileExporter {
        private final GeneticProfileDatatypeMetadata metatdata;

        public LimitValueGenericProfileExporter(GeneticProfileDatatypeMetadata metadata) {
            this.metatdata = metadata;
            this.metatdata.setGenericEntitiesMetaProperties(
                geneticProfileDataService.getDistinctGenericEntityMetaPropertyNames(metadata.getStableId()));
        }

        private static CloseableIterator<TableRow> composeRows(CloseableIterator<GeneticProfileData> geneticProfileData, List<String> sampleStableIds, List<String> genericEntitiesMetaProperties, CloseableIterator<GenericEntityProperty> properties) {
            PeekingIterator<GeneticProfileData> geneticProfileDataPeekingIterator = Iterators.peekingIterator(geneticProfileData);
            PeekingIterator<GenericEntityProperty> propertyPeekingIterator = Iterators.peekingIterator(properties);
            return new CloseableIterator<>() {
                @Override
                public void close() throws IOException {
                    geneticProfileData.close();
                    properties.close();
                }

                @Override
                public boolean hasNext() {
                    return geneticProfileDataPeekingIterator.hasNext();
                }

                @Override
                public TableRow next() {
                    var data = geneticProfileDataPeekingIterator.next();
                    if (data.getGeneticEntity() == null) {
                        throw new IllegalStateException("Genetic entity is null");
                    }
                    if (data.getGeneticEntity().getGeneticEntityId() == null) {
                        throw new IllegalStateException("Genetic entity ID is null");
                    }
                    if (geneticProfileDataPeekingIterator.hasNext()
                        && geneticProfileDataPeekingIterator.peek().getGeneticEntity() != null
                        && geneticProfileDataPeekingIterator.peek().getGeneticEntity().getGeneticEntityId() != null
                        && data.getGeneticEntity().getGeneticEntityId() > geneticProfileDataPeekingIterator.peek().getGeneticEntity().getGeneticEntityId()) {
                        throw new IllegalStateException("Genetic entity ID is not in ascending order");
                    }
                    if (data.getValues().size() != sampleStableIds.size()) {
                        throw new IllegalStateException("Number of values does not match number of sample stable IDs");
                    }
                    var row = new LinkedHashMap<String, String>();
                    for (String columnName : ROW.keySet()) {
                        row.put(columnName, ROW.get(columnName).apply(data));
                    }
                    if (!genericEntitiesMetaProperties.isEmpty()) {
                        var propertyMap = new HashMap<String, String>();
                        GenericEntityProperty property = null;
                        while (propertyPeekingIterator.hasNext() && propertyPeekingIterator.peek().getGeneticEntityId() <= data.getGeneticEntity().getGeneticEntityId()) {
                            if (propertyPeekingIterator.peek().getGeneticEntityId() < data.getGeneticEntity().getGeneticEntityId()) {
                                throw new IllegalStateException(String.format("%s property with genetic entity ID %d is not present in the result set.", propertyPeekingIterator.peek().getName(), propertyPeekingIterator.peek().getGeneticEntityId()));
                            }
                            property = propertyPeekingIterator.next();
                            if (property.getName() == null) {
                                throw new IllegalStateException("Property name is null");
                            }
                            if (property.getValue() == null) {
                                throw new IllegalStateException("Property value is null");
                            }
                            propertyMap.put(property.getName(), property.getValue());
                        }
                        if (property != null && propertyPeekingIterator.hasNext() && property.getGeneticEntityId() > propertyPeekingIterator.peek().getGeneticEntityId()) {
                            throw new IllegalStateException("Genetic entity ID is not in ascending order for properties");
                        }
                        // Add the properties to the row in the order of genericEntitiesMetaProperties
                        for (String propertyName : genericEntitiesMetaProperties) {
                            row.put(propertyName, propertyMap.get(propertyName));
                        }
                    }
                    for (int i = 0; i < sampleStableIds.size(); i++) {
                        row.put(sampleStableIds.get(i), data.getValues().get(i));
                    }
                    return () -> row;
                }
            };
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
            CloseableIterator<GenericEntityProperty> properties = CloseableIterator.empty();
            if (!this.metatdata.getGenericEntitiesMetaProperties().isEmpty()) {
                properties = geneticProfileDataService.getGenericEntityMetaProperties(metatdata.getStableId());
            }
            var header = new LinkedHashSet<String>();
            header.addAll(ROW.keySet());
            header.addAll(this.metatdata.getGenericEntitiesMetaProperties());
            header.addAll(sampleStableIds);
            return new Table(composeRows(geneticProfileData, sampleStableIds, metatdata.getGenericEntitiesMetaProperties(), properties), header);
        }
    }
}
