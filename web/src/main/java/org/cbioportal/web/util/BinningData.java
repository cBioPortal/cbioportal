package org.cbioportal.web.util;

import com.google.common.collect.ImmutableList;
import org.cbioportal.model.Binnable;
import org.cbioportal.model.ClinicalData;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Collections.unmodifiableList;

public class BinningData<T extends Binnable> {
    public final List<T> samples;
    public final List<T> patients;
    public final List<T> conflictingPatientAttributes;
    private final List<T> allData;

    public BinningData(
        List<T> samples, 
        List<T> patients, 
        List<T> conflictingPatientAttributes
    ) {
        this.samples = unmodifiableList(samples);
        this.patients = unmodifiableList(patients);
        this.conflictingPatientAttributes = unmodifiableList(conflictingPatientAttributes);
        this.allData = Stream.of(
            this.samples,
            this.patients,
            this.conflictingPatientAttributes
        ).flatMap(Collection::stream).collect(Collectors.toList());
    }
    
    public List<T> getAllData() {
        return allData;
    }

}