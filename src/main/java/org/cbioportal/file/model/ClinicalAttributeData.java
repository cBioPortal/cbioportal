package org.cbioportal.file.model;

import com.google.common.collect.Iterators;
import com.google.common.collect.PeekingIterator;

import java.util.Iterator;
import java.util.SequencedMap;
import java.util.SequencedSet;

public class ClinicalAttributeData {

    private final Iterator<SequencedMap<ClinicalAttribute, String>> rows;
    private final SequencedSet<ClinicalAttribute> attributes;

    public ClinicalAttributeData(Iterator<SequencedMap<ClinicalAttribute, String>> rows) {
        PeekingIterator<SequencedMap<ClinicalAttribute, String>> peekingIterator = Iterators.peekingIterator(rows);
        this.rows = peekingIterator;
        this.attributes = peekingIterator.peek().sequencedKeySet();
    }
    public ClinicalAttributeData(SequencedSet<ClinicalAttribute> attributes, Iterator<SequencedMap<ClinicalAttribute, String>> rows) {
       this.rows = rows;
       this.attributes = attributes;
    }
    public SequencedSet<ClinicalAttribute> getAttributes() {
        return attributes;
    }
    public Iterator<SequencedMap<ClinicalAttribute, String>> rows() {
        return rows;
    }
}
