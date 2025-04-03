package org.cbioportal.application.file.model;

import com.google.common.collect.Iterables;
import com.google.common.collect.Iterators;
import com.google.common.collect.PeekingIterator;
import org.cbioportal.application.file.utils.CloseableIterator;

import java.io.Closeable;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class ClinicalAttributesTable implements CloseableIterator<SequencedMap<String, String>>, HeaderInfo {

    private final List<ClinicalAttribute> attributes;
    private final Closeable closeable;
    private PeekingIterator<? extends ClinicalAttributeValue> rowIterator;
    private final LinkedHashSet<String> header;

    public ClinicalAttributesTable(List<ClinicalAttribute> attributes, CloseableIterator<? extends ClinicalAttributeValue> data) {
        this.attributes = attributes;
        this.header = attributes.stream().map(ClinicalAttribute::getAttributeId).collect(Collectors.toCollection(LinkedHashSet::new));
        this.closeable = data;
        this.rowIterator = Iterators.peekingIterator(data);
    }

    @Override
    public boolean hasNext() {
        return rowIterator.hasNext();
    }

    @Override
    public SequencedMap<String, String> next() {
        if (rowIterator.hasNext()) {
            ClinicalAttributeValue clinicalAttributeValue = rowIterator.next();
            var attributeValueMap = new HashMap<>(clinicalAttributeValue.getValue());
            while (rowIterator.hasNext()
                && rowIterator.peek().getKey().equals(clinicalAttributeValue.getKey())) {
                clinicalAttributeValue = rowIterator.next();
                attributeValueMap.putAll(clinicalAttributeValue.getValue());
            }
            attributeValueMap.putAll(clinicalAttributeValue.getKey());
            var result = new LinkedHashMap<String, String>();
            header.forEach(attributeId -> result.put(attributeId, attributeValueMap.remove(attributeId)));
            if (!attributeValueMap.isEmpty()) {
                throw new IllegalStateException("The following attributes do not have metadata: " + attributeValueMap.keySet());
            }
            return result;
        }
        throw new IllegalStateException("No more elements");
    }

    @Override
    public Iterable<Iterable<String>> getComments() {
        return List.of(
            Iterables.transform(attributes, ClinicalAttribute::getDisplayName),
            Iterables.transform(attributes, ClinicalAttribute::getDescription),
            Iterables.transform(attributes, ClinicalAttribute::getDatatype),
            Iterables.transform(attributes, ClinicalAttribute::getPriority)
        );
    }

    @Override
    public SequencedSet<String> getHeader() {
        return header;
    }

    @Override
    public void close() throws IOException {
        closeable.close();
    }
}
