package org.mskcc.portal.mut_diagram;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newHashMap;

import java.util.List;
import java.util.Map;

/**
 * Sequence, derived from Pfam graphics response in JSON format.
 */
public final class Sequence {
    private int length;
    private List<Markup> markups;
    private List<Motif> motifs; 
    private List<Region> regions;
    private Map<String, Object> metadata;
    private Options options;

    public void setLength(final int length) {
        this.length = length;
    }   

    public int getLength() {
        return length;
    }

    public void setMarkups(final List<Markup> markups) {
        this.markups = markups;
    }

    public List<Markup> getMarkups() {
        return markups;
    }

    public void setMotifs(final List<Motif> motifs) {
        this.motifs = motifs;
    }

    public List<Motif> getMotifs() {
        return motifs;
    }

    public void setRegions(final List<Region> regions) {
        this.regions = regions;
    }

    public List<Region> getRegions() {
        return regions;
    }

    public void setMetadata(final Map<String, Object> metadata) {
        this.metadata = metadata;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public void setOptions(final Options options) {
        this.options = options;
    }

    public Options getOptions() {
        return options;
    }

    public Sequence deepCopy() {
        Sequence sequence = new Sequence();
        sequence.setLength(length);
        if (markups != null) {
            sequence.setMarkups(newArrayList(markups));
        }
        if (metadata != null) {
            sequence.setMetadata(newHashMap(metadata));
        }
        if (motifs != null) {
            sequence.setMotifs(newArrayList(motifs));
        }
        if (options != null) {
            Options copy = new Options();
            copy.setBaseUrl(options.getBaseUrl());
            sequence.setOptions(copy);
        }
        if (regions != null) {
            sequence.setRegions(newArrayList(regions));
        }
        return sequence;
    }

    /**
     * Return a deep copy of this sequence with the additional list of markups.
     *
     * @param markups list of markups to add
     * @return a deep copy of this sequence with the additional list of markups
     */
    public Sequence withMarkups(final List<Markup> markups) {
        Sequence sequence = deepCopy();
        if (markups != null && !markups.isEmpty()) {
            if (sequence.getMarkups() == null) {
                sequence.setMarkups(newArrayList(markups));
            }
            else {
                sequence.getMarkups().addAll(markups);
            }
        }
        return sequence;
    }
}
