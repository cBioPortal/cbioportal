package org.cbioportal.application.file.export.exporters;

import static org.cbioportal.application.file.export.writers.WriterHelper.writeData;
import static org.cbioportal.application.file.export.writers.WriterHelper.writeMetadata;

import com.google.common.collect.Iterators;
import com.google.common.collect.PeekingIterator;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import org.cbioportal.application.file.export.services.ClinicalAttributeDataService;
import org.cbioportal.application.file.model.ClinicalAttributesMetadata;
import org.cbioportal.application.file.model.ClinicalEvent;
import org.cbioportal.application.file.model.ClinicalEventData;
import org.cbioportal.application.file.model.Table;
import org.cbioportal.application.file.model.TableRow;
import org.cbioportal.application.file.utils.CloseableIterator;
import org.cbioportal.application.file.utils.FileWriterFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Export metadata and data for clinical timeline data type. It exports data for each event type
 * separately.
 */
public class ClinicalTimelineDataTypeExporter implements Exporter {

  private static final Logger LOG = LoggerFactory.getLogger(ClinicalTimelineDataTypeExporter.class);
  private static final LinkedHashMap<String, Function<ClinicalEvent, String>> ROW =
      new LinkedHashMap<>();

  static {
    ROW.put("PATIENT_ID", ClinicalEvent::getPatientId);
    ROW.put(
        "START_DATE",
        data -> data.getStartDate() == null ? null : String.valueOf(data.getStartDate()));
    ROW.put(
        "STOP_DATE",
        data -> data.getStopDate() == null ? null : String.valueOf(data.getStopDate()));
    ROW.put("EVENT_TYPE", ClinicalEvent::getEventType);
  }

  private final ClinicalAttributeDataService clinicalDataAttributeDataService;

  public ClinicalTimelineDataTypeExporter(
      ClinicalAttributeDataService clinicalDataAttributeDataService) {
    this.clinicalDataAttributeDataService = clinicalDataAttributeDataService;
  }

  @Override
  public boolean exportData(FileWriterFactory fileWriterFactory, ExportDetails exportDetails) {
    String studyId = exportDetails.getStudyId();
    Set<String> sampleIds = exportDetails.getSampleIds();
    if (!clinicalDataAttributeDataService.hasClinicalTimelineData(studyId, sampleIds)) {
      return false;
    }
    List<String> eventTypes = clinicalDataAttributeDataService.getDistinctEventTypes(studyId);
    ClinicalAttributesMetadata clinicalAttributesMetadata =
        new ClinicalAttributesMetadata(studyId, "CLINICAL", "TIMELINE");
    for (String eventType : eventTypes) {
      LOG.debug(
          "Exporting clinical timeline data for study {} and event type {}", studyId, eventType);
      CloseableIterator<ClinicalEvent> clinicalEventsIterator =
          clinicalDataAttributeDataService.getClinicalEvents(studyId, eventType, sampleIds);
      if (!clinicalEventsIterator.hasNext()) {
        LOG.debug("No clinical events found for study {} and event type {}", studyId, eventType);
        continue;
      }
      String commonFilePart =
          clinicalAttributesMetadata.getGeneticAlterationType().toLowerCase()
              + "_"
              + clinicalAttributesMetadata.getDatatype().toLowerCase()
              + "_"
              + sanitizeForFileName(eventType)
              + ".txt";
      String metaFilename = "meta_" + commonFilePart;
      String dataFilename = "data_" + commonFilePart;
      writeMetadata(
          fileWriterFactory, metaFilename, clinicalAttributesMetadata, dataFilename, exportDetails);
      try (Table data =
          getData(exportDetails.getStudyId(), eventType, exportDetails.getSampleIds())) {
        writeData(fileWriterFactory, dataFilename, data);
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
      LOG.debug(
          "Exported clinical timeline data for study {} and event type {}", studyId, eventType);
    }
    return true;
  }

  public static String sanitizeForFileName(String input) {
    // Windows: \ / : * ? " < > |
    // Unix/macOS: /
    return input.replaceAll("[ \\\\/:*?\"<>|]", "_");
  }

  protected Table getData(String studyId, String eventType, Set<String> sampleIds) {
    List<String> clinicalEventKeys =
        clinicalDataAttributeDataService.getDistinctClinicalEventKeys(studyId, eventType);
    CloseableIterator<ClinicalEventData> clinicalEventDataIterator;
    if (clinicalEventKeys.isEmpty()) {
      clinicalEventDataIterator = CloseableIterator.empty();
    } else {
      clinicalEventDataIterator =
          clinicalDataAttributeDataService.getClinicalEventData(studyId, eventType, sampleIds);
    }

    CloseableIterator<ClinicalEvent> clinicalEventsIterator =
        clinicalDataAttributeDataService.getClinicalEvents(studyId, eventType, sampleIds);
    return getTable(clinicalEventsIterator, clinicalEventKeys, clinicalEventDataIterator);
  }

  private static Table getTable(
      CloseableIterator<ClinicalEvent> clinicalEventsIterator,
      List<String> clinicalEventKeys,
      CloseableIterator<ClinicalEventData> clinicalEventDataIterator) {
    var header = new LinkedHashSet<String>();
    header.addAll(ROW.sequencedKeySet());
    header.addAll(clinicalEventKeys);

    PeekingIterator<ClinicalEventData> peekingClinicalEventDataIterator =
        Iterators.peekingIterator(clinicalEventDataIterator);
    return new Table(
        new CloseableIterator<>() {
          @Override
          public void close() throws IOException {
            clinicalEventDataIterator.close();
            clinicalEventsIterator.close();
          }

          @Override
          public boolean hasNext() {
            return clinicalEventsIterator.hasNext();
          }

          @Override
          public TableRow next() {
            ClinicalEvent clinicalEvent = clinicalEventsIterator.next();
            var row = new LinkedHashMap<String, String>();
            for (var entry : ROW.entrySet()) {
              row.put(entry.getKey(), entry.getValue().apply(clinicalEvent));
            }
            if (!clinicalEventKeys.isEmpty()) {
              var properties = new HashMap<String, String>();
              while (peekingClinicalEventDataIterator.hasNext()
                  && peekingClinicalEventDataIterator.peek().getClinicalEventId()
                      <= clinicalEvent.getClinicalEventId()) {
                if (peekingClinicalEventDataIterator.peek().getClinicalEventId()
                    < clinicalEvent.getClinicalEventId()) {
                  throw new IllegalStateException(
                      "Clinical event IDs are not matching. Check the order of clinical events and their data. Both should be in ascending order.");
                }
                ClinicalEventData clinicalEventData = peekingClinicalEventDataIterator.next();
                if (clinicalEventData.getKey() == null) {
                  throw new IllegalStateException("Clinical event data key is null");
                }
                properties.put(clinicalEventData.getKey(), clinicalEventData.getValue());
              }
              for (String key : clinicalEventKeys) {
                String value = properties.get(key);
                row.put(key, value == null || value.isBlank() ? DEFAULT_VALUES.get(key) : value);
              }
            }
            return () -> row;
          }
        },
        header);
  }

  private static final Map<String, String> DEFAULT_VALUES =
      Map.of(
          "STYLE_COLOR", "#1f77b4",
          "STYLE_SHAPE", "circle");
}
