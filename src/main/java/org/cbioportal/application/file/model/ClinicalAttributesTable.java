package org.cbioportal.application.file.model;

import com.google.common.collect.Iterables;
import com.google.common.collect.Iterators;
import com.google.common.collect.PeekingIterator;
import java.io.Closeable;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.SequencedMap;
import java.util.SequencedSet;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.cbioportal.application.file.utils.CloseableIterator;

public class ClinicalAttributesTable
    implements CloseableIterator<SequencedMap<String, String>>, HeaderInfo {

  private final List<ClinicalAttribute> attributes;
  private final Closeable closeable;
  private final LinkedHashSet<String> header;
  private final PeekingIterator<ClinicalAttributeValue> rowIterator;

  public ClinicalAttributesTable(
      List<ClinicalAttribute> attributes, CloseableIterator<ClinicalAttributeValue> data) {
    Set<String> duplicates =
        attributes.stream()
            .map(ClinicalAttribute::getAttributeId)
            .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()))
            .entrySet()
            .stream()
            .filter(entry -> entry.getValue() > 1)
            .map(Map.Entry::getKey)
            .collect(Collectors.toSet());
    if (!duplicates.isEmpty()) {
      throw new IllegalArgumentException("The following attributes are duplicated: " + duplicates);
    }
    this.attributes = attributes;
    this.header =
        attributes.stream()
            .map(ClinicalAttribute::getAttributeId)
            .collect(Collectors.toCollection(LinkedHashSet::new));
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
      if (rowIterator.hasNext()
          && clinicalAttributeValue.getRowKey().compareTo(rowIterator.peek().getRowKey()) > 0) {
        throw new IllegalStateException(
            "The keys are not in ascending order:"
                + clinicalAttributeValue.getRowKey()
                + " and "
                + rowIterator.peek().getRowKey());
      }
      var attributeValueMap = new HashMap<String, String>();
      attributeValueMap.put(
          clinicalAttributeValue.getAttributeId(), clinicalAttributeValue.getAttributeValue());
      while (rowIterator.hasNext()
          && rowIterator.peek().getRowKey().equals(clinicalAttributeValue.getRowKey())) {
        clinicalAttributeValue = rowIterator.next();
        attributeValueMap.put(
            clinicalAttributeValue.getAttributeId(), clinicalAttributeValue.getAttributeValue());
      }
      var result = new LinkedHashMap<String, String>();
      header.forEach(attributeId -> result.put(attributeId, attributeValueMap.remove(attributeId)));
      if (!attributeValueMap.isEmpty()) {
        throw new IllegalStateException(
            "The following attributes do not have metadata: " + attributeValueMap.keySet());
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
        Iterables.transform(attributes, ClinicalAttribute::getPriority));
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
