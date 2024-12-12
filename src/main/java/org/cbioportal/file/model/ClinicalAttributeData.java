package org.cbioportal.file.model;

import com.google.common.collect.Iterators;
import com.google.common.collect.PeekingIterator;

import java.util.Iterator;
import java.util.SequencedMap;
import java.util.SequencedSet;

public class ClinicalAttributeData {

    private final PeekingIterator<SequencedMap<ClinicalAttribute, String>> rows;
    private final SequencedSet<ClinicalAttribute> attributes;

    public ClinicalAttributeData(Iterator<SequencedMap<ClinicalAttribute, String>> rows) {
       this.rows = Iterators.peekingIterator(rows); 
       this.attributes = this.rows.peek().sequencedKeySet();
    }
    public SequencedSet<ClinicalAttribute> getAttributes() {
        return attributes;
    }
    public Iterator<SequencedMap<ClinicalAttribute, String>> rows() {
        return rows;
    }
}
