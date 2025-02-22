package org.cbioportal.legacy.web.util;

import static java.util.Collections.unmodifiableList;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.cbioportal.legacy.model.Binnable;

public class BinningData<T extends Binnable> {
  public final List<T> samples;
  public final List<T> patients;
  public final List<T> conflictingPatientAttributes;
  private final List<T> allData;

  public BinningData(List<T> samples, List<T> patients, List<T> conflictingPatientAttributes) {
    this.samples = unmodifiableList(samples);
    this.patients = unmodifiableList(patients);
    this.conflictingPatientAttributes = unmodifiableList(conflictingPatientAttributes);
    this.allData =
        Stream.of(this.samples, this.patients, this.conflictingPatientAttributes)
            .flatMap(Collection::stream)
            .collect(Collectors.toList());
  }

  public List<T> getAllData() {
    return allData;
  }
}
