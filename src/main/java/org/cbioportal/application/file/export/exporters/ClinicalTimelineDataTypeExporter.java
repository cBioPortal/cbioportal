package org.cbioportal.application.file.export.exporters;

import com.google.common.collect.Iterators;
import com.google.common.collect.PeekingIterator;
import org.cbioportal.application.file.export.services.ClinicalAttributeDataService;
import org.cbioportal.application.file.model.ClinicalAttributesMetadata;
import org.cbioportal.application.file.model.ClinicalEvent;
import org.cbioportal.application.file.model.ClinicalEventData;
import org.cbioportal.application.file.model.Table;
import org.cbioportal.application.file.model.TableRow;
import org.cbioportal.application.file.utils.CloseableIterator;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

/**
 * Export metadata and data for clinical timeline data type.
 */
public class ClinicalTimelineDataTypeExporter extends DataTypeExporter<ClinicalAttributesMetadata, Table> {

    private static final LinkedHashMap<String, Function<ClinicalEvent, String>> ROW = new LinkedHashMap<>();

    static {
        ROW.put("PATIENT_ID", ClinicalEvent::getPatientId);
        ROW.put("START_DATE", data -> data.getStartDate() == null ? null : String.valueOf(data.getStartDate()));
        ROW.put("STOP_DATE", data -> data.getStopDate() == null ? null : String.valueOf(data.getStopDate()));
        ROW.put("EVENT_TYPE", ClinicalEvent::getEventType);
    }

    private final ClinicalAttributeDataService clinicalDataAttributeDataService;

    public ClinicalTimelineDataTypeExporter(ClinicalAttributeDataService clinicalDataAttributeDataService) {
        this.clinicalDataAttributeDataService = clinicalDataAttributeDataService;
    }

    @Override
    protected Optional<ClinicalAttributesMetadata> getMetadata(String studyId, Set<String> sampleIds) {
        if (!clinicalDataAttributeDataService.hasClinicalTimelineData(studyId, sampleIds)) {
            return Optional.empty();
        }
        return Optional.of(new ClinicalAttributesMetadata(studyId, "CLINICAL", "TIMELINE"));
    }

    @Override
    protected Table getData(String studyId, Set<String> sampleIds) {
        List<String> clinicalEventKeys = clinicalDataAttributeDataService.getDistinctClinicalEventKeys(studyId);
        CloseableIterator<ClinicalEventData> clinicalEventDataIterator;
        if (clinicalEventKeys.isEmpty()) {
            clinicalEventDataIterator = CloseableIterator.empty();
        } else {
            clinicalEventDataIterator = clinicalDataAttributeDataService.getClinicalEventData(studyId, sampleIds);
        }

        CloseableIterator<ClinicalEvent> clinicalEventsIterator = clinicalDataAttributeDataService.getClinicalEvents(studyId, sampleIds);
        return getTable(clinicalEventsIterator, clinicalEventKeys, clinicalEventDataIterator);
    }

    private static Table getTable(CloseableIterator<ClinicalEvent> clinicalEventsIterator, List<String> clinicalEventKeys, CloseableIterator<ClinicalEventData> clinicalEventDataIterator) {
        var header = new LinkedHashSet<String>();
        header.addAll(ROW.sequencedKeySet());
        header.addAll(clinicalEventKeys);

        PeekingIterator<ClinicalEventData> peekingClinicalEventDataIterator = Iterators.peekingIterator(clinicalEventDataIterator);
        return new Table(new CloseableIterator<>() {
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
                    while (peekingClinicalEventDataIterator.hasNext() && peekingClinicalEventDataIterator.peek().getClinicalEventId() <= clinicalEvent.getClinicalEventId()) {
                        if (peekingClinicalEventDataIterator.peek().getClinicalEventId() < clinicalEvent.getClinicalEventId()) {
                            throw new IllegalStateException("Clinical event IDs are not matching. Check the order of clinical events and their data. Both should be in ascending order.");
                        }
                        ClinicalEventData clinicalEventData = peekingClinicalEventDataIterator.next();
                        if (clinicalEventData.getKey() == null) {
                            throw new IllegalStateException("Clinical event data key is null");
                        }
                        properties.put(clinicalEventData.getKey(), clinicalEventData.getValue());
                    }
                    for (String key : clinicalEventKeys) {
                        row.put(key, properties.get(key));
                    }
                }
                return () -> row;
            }
        }, header);
    }
}
