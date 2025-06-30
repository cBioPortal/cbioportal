package org.cbioportal.application.file.export.exporters;

import com.google.common.collect.Iterators;
import com.google.common.collect.PeekingIterator;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.SequencedMap;
import java.util.Set;
import java.util.function.Function;
import org.cbioportal.application.file.export.services.GeneticProfileDataService;
import org.cbioportal.application.file.export.services.GeneticProfileService;
import org.cbioportal.application.file.model.GenericEntityProperty;
import org.cbioportal.application.file.model.GeneticProfileData;
import org.cbioportal.application.file.model.GeneticProfileDatatypeMetadata;
import org.cbioportal.application.file.model.Table;
import org.cbioportal.application.file.model.TableRow;
import org.cbioportal.application.file.utils.CloseableIterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class GeneticAlterationTsvExporter extends GeneticProfileDatatypeExporter {

  private static final Logger LOG = LoggerFactory.getLogger(GeneticAlterationTsvExporter.class);

  protected final GeneticProfileDataService geneticProfileDataService;

  protected GeneticAlterationTsvExporter(
      GeneticProfileService geneticProfileService,
      GeneticProfileDataService geneticProfileDataService) {
    super(geneticProfileService);
    this.geneticProfileDataService = geneticProfileDataService;
  }

  @Override
  protected Exporter composeExporterFor(GeneticProfileDatatypeMetadata metadata) {
    setGenericEntitiesMetaProperties(metadata);
    return new GenericProfileExporter(metadata);
  }

  private class GenericProfileExporter extends GeneticProfileExporter {
    private final GeneticProfileDatatypeMetadata metadata;

    public GenericProfileExporter(GeneticProfileDatatypeMetadata metadata) {
      this.metadata = metadata;
    }

    @Override
    protected Optional<GeneticProfileDatatypeMetadata> getMetadata(
        String studyId, Set<String> sampleIds) {
      return Optional.of(metadata);
    }

    @Override
    protected CloseableIterator<SequencedMap<String, String>> getData(
        String studyId, Set<String> selectSampleIds) {
      List<String> sampleIdsList =
          geneticProfileDataService.getSampleStableIds(metadata.getStableId());
      for (String sampleStableId : sampleIdsList) {
        if (sampleStableId == null) {
          throw new IllegalStateException("Sample stable ID is null");
        }
      }
      LOG.debug("Fetching data for {} platform", metadata.getStableId());
      var geneticProfileData = geneticProfileDataService.getData(metadata.getStableId());
      LOG.debug("Fetched data for {} platform", metadata.getStableId());
      CloseableIterator<GenericEntityProperty> properties = CloseableIterator.empty();
      var header = new LinkedHashSet<String>();
      header.addAll(getRowMappers().keySet());
      if (this.metadata.getGenericEntitiesMetaProperties() != null
          && !this.metadata.getGenericEntitiesMetaProperties().isEmpty()) {
        properties =
            geneticProfileDataService.getGenericEntityMetaProperties(metadata.getStableId());
        header.addAll(this.metadata.getGenericEntitiesMetaProperties());
      }
      header.addAll(
          selectSampleIds == null
              ? sampleIdsList
              : sampleIdsList.stream().filter(selectSampleIds::contains).toList());
      return new Table(
          composeRows(
              geneticProfileData,
              sampleIdsList,
              selectSampleIds,
              metadata.getGenericEntitiesMetaProperties(),
              properties),
          header);
    }
  }

  protected CloseableIterator<TableRow> composeRows(
      CloseableIterator<GeneticProfileData> geneticProfileData,
      List<String> sampleStableIds,
      Collection<String> selectSampleIds,
      List<String> genericEntitiesMetaProperties,
      CloseableIterator<GenericEntityProperty> properties) {
    PeekingIterator<GeneticProfileData> geneticProfileDataPeekingIterator =
        Iterators.peekingIterator(geneticProfileData);
    PeekingIterator<GenericEntityProperty> propertyPeekingIterator =
        Iterators.peekingIterator(properties);
    return new TableRowCloseableIterator(
        geneticProfileData,
        properties,
        geneticProfileDataPeekingIterator,
        genericEntitiesMetaProperties,
        propertyPeekingIterator,
        sampleStableIds,
        selectSampleIds);
  }

  protected abstract LinkedHashMap<String, Function<GeneticProfileData, String>> getRowMappers();

  protected abstract void setGenericEntitiesMetaProperties(GeneticProfileDatatypeMetadata metadata);

  private class TableRowCloseableIterator implements CloseableIterator<TableRow> {
    private final CloseableIterator<GeneticProfileData> geneticProfileData;
    private final CloseableIterator<GenericEntityProperty> properties;
    private final PeekingIterator<GeneticProfileData> geneticProfileDataPeekingIterator;
    private final List<String> genericEntitiesMetaProperties;
    private final PeekingIterator<GenericEntityProperty> propertyPeekingIterator;
    private final List<String> sampleStableIds;
    private final Collection<String> selectSampleIds;

    public TableRowCloseableIterator(
        CloseableIterator<GeneticProfileData> geneticProfileData,
        CloseableIterator<GenericEntityProperty> properties,
        PeekingIterator<GeneticProfileData> geneticProfileDataPeekingIterator,
        List<String> genericEntitiesMetaProperties,
        PeekingIterator<GenericEntityProperty> propertyPeekingIterator,
        List<String> sampleStableIds,
        Collection<String> selectSampleIds) {
      this.geneticProfileData = geneticProfileData;
      this.properties = properties;
      this.geneticProfileDataPeekingIterator = geneticProfileDataPeekingIterator;
      this.genericEntitiesMetaProperties = genericEntitiesMetaProperties;
      this.propertyPeekingIterator = propertyPeekingIterator;
      this.sampleStableIds = sampleStableIds;
      this.selectSampleIds = selectSampleIds;
    }

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
      validateGeneticEntity(data);
      validateAscendingOrder(data);

      var row = createRow(data);
      if (hasGenericEntitiesMetaProperties()) {
        var propertyMap = processProperties(data);
        addPropertiesToRow(row, propertyMap);
      }
      addSampleValuesToRow(row, data.getValues());
      return () -> row;
    }

    private void validateGeneticEntity(GeneticProfileData data) {
      if (data.getGeneticEntity() == null || data.getGeneticEntity().getGeneticEntityId() == null) {
        throw new IllegalStateException("Genetic entity or its ID is null");
      }
    }

    private void validateAscendingOrder(GeneticProfileData data) {
      if (geneticProfileDataPeekingIterator.hasNext()
          && isOutOfOrder(data, geneticProfileDataPeekingIterator.peek())) {
        throw new IllegalStateException("Genetic entity ID is not in ascending order");
      }
    }

    private boolean isOutOfOrder(GeneticProfileData current, GeneticProfileData next) {
      return next.getGeneticEntity() != null
          && next.getGeneticEntity().getGeneticEntityId() != null
          && current.getGeneticEntity().getGeneticEntityId()
              > next.getGeneticEntity().getGeneticEntityId();
    }

    private LinkedHashMap<String, String> createRow(GeneticProfileData data) {
      var row = new LinkedHashMap<String, String>();
      getRowMappers().forEach((columnName, mapper) -> row.put(columnName, mapper.apply(data)));
      return row;
    }

    private boolean hasGenericEntitiesMetaProperties() {
      return genericEntitiesMetaProperties != null && !genericEntitiesMetaProperties.isEmpty();
    }

    private Map<String, String> processProperties(GeneticProfileData data) {
      var propertyMap = new HashMap<String, String>();
      while (propertyPeekingIterator.hasNext()
          && propertyPeekingIterator.peek().getGeneticEntityId()
              <= data.getGeneticEntity().getGeneticEntityId()) {
        var property = propertyPeekingIterator.next();
        validateProperty(property);
        propertyMap.put(property.getName(), property.getValue());
      }
      return propertyMap;
    }

    private void validateProperty(GenericEntityProperty property) {
      if (property.getName() == null || property.getValue() == null) {
        throw new IllegalStateException("Property name or value is null");
      }
    }

    private void addPropertiesToRow(
        LinkedHashMap<String, String> row, Map<String, String> propertyMap) {
      for (String propertyName : genericEntitiesMetaProperties) {
        row.put(propertyName, propertyMap.get(propertyName));
      }
    }

    private void addSampleValuesToRow(LinkedHashMap<String, String> row, List<String> values) {
      if (values.size() != sampleStableIds.size()) {
        throw new IllegalStateException(
            "Number of values does not match number of sample stable IDs");
      }
      for (int i = 0; i < sampleStableIds.size(); i++) {
        if (selectSampleIds == null || selectSampleIds.contains(sampleStableIds.get(i))) {
          row.put(sampleStableIds.get(i), values.get(i));
        }
      }
    }
  }
}
