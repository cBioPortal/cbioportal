package org.cbioportal.application.file.export.exporters;

import com.google.common.collect.Iterators;
import com.google.common.collect.PeekingIterator;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
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

  public GeneticAlterationTsvExporter(
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
              : selectSampleIds.stream().filter(sampleIdsList::contains).toList());
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
            && geneticProfileDataPeekingIterator.peek().getGeneticEntity().getGeneticEntityId()
                != null
            && data.getGeneticEntity().getGeneticEntityId()
                > geneticProfileDataPeekingIterator
                    .peek()
                    .getGeneticEntity()
                    .getGeneticEntityId()) {
          throw new IllegalStateException("Genetic entity ID is not in ascending order");
        }
        List<String> values = data.getValues();
        if (values.size() != sampleStableIds.size()) {
          throw new IllegalStateException(
              "Number of values does not match number of sample stable IDs");
        }
        var row = new LinkedHashMap<String, String>();
        LinkedHashMap<String, Function<GeneticProfileData, String>> rowMappers = getRowMappers();
        for (String columnName : rowMappers.keySet()) {
          row.put(columnName, rowMappers.get(columnName).apply(data));
        }
        if (genericEntitiesMetaProperties != null && !genericEntitiesMetaProperties.isEmpty()) {
          var propertyMap = new HashMap<String, String>();
          GenericEntityProperty property = null;
          while (propertyPeekingIterator.hasNext()
              && propertyPeekingIterator.peek().getGeneticEntityId()
                  <= data.getGeneticEntity().getGeneticEntityId()) {
            if (propertyPeekingIterator.peek().getGeneticEntityId()
                < data.getGeneticEntity().getGeneticEntityId()) {
              throw new IllegalStateException(
                  String.format(
                      "%s property with genetic entity ID %d is not present in the result set.",
                      propertyPeekingIterator.peek().getName(),
                      propertyPeekingIterator.peek().getGeneticEntityId()));
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
          if (property != null
              && propertyPeekingIterator.hasNext()
              && property.getGeneticEntityId()
                  > propertyPeekingIterator.peek().getGeneticEntityId()) {
            throw new IllegalStateException(
                "Genetic entity ID is not in ascending order for properties");
          }
          // Add the properties to the row in the order of genericEntitiesMetaProperties
          for (String propertyName : genericEntitiesMetaProperties) {
            row.put(propertyName, propertyMap.get(propertyName));
          }
        }
        for (int i = 0; i < sampleStableIds.size(); i++) {
          if (selectSampleIds != null && !selectSampleIds.contains(sampleStableIds.get(i))) {
            continue;
          }
          row.put(sampleStableIds.get(i), values.get(i));
        }
        return () -> row;
      }
    };
  }

  protected abstract LinkedHashMap<String, Function<GeneticProfileData, String>> getRowMappers();

  protected abstract void setGenericEntitiesMetaProperties(GeneticProfileDatatypeMetadata metadata);
}
