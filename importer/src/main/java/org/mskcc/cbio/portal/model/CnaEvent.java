/*
 * Copyright (c) 2015 Memorial Sloan-Kettering Cancer Center.
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.mskcc.cbio.portal.model;

import org.mskcc.cbio.portal.dao.DaoGeneOptimized;

/**
 *
 * @author jgao
 */
public class CnaEvent {
    private int sampleId;
    private int cnaProfileId;
    private Event event;
    private String driverFilter;
    private String driverFilterAnnotation;
    private String driverTiersFilter;
    private String driverTiersFilterAnnotation;
    private String annotationJson;

    public static class Event {
        private long eventId;
        private CanonicalGene gene;
        private int alteration;

        public long getEventId() {
            return eventId;
        }

        public void setEventId(long eventId) {
            this.eventId = eventId;
        }

        public CanonicalGene getGene() {
            return gene;
        }

        public void setGene(CanonicalGene gene) {
            this.gene = gene;
        }

        public void setEntrezGeneId(long entrezGeneId) {
            setGene(DaoGeneOptimized.getInstance().getGene(entrezGeneId));
            if (gene == null) {
                throw new IllegalArgumentException("Could not find entrez gene id: " + entrezGeneId);
            } 
        }

        public int getAlteration() {
            return alteration;
        }

        public void setAlteration(int alteration) {
            if (alteration < CopyNumberStatus.HOMOZYGOUS_DELETION || alteration > CopyNumberStatus.COPY_NUMBER_AMPLIFICATION) {
                throw new IllegalArgumentException("Invalid alteration value: " + alteration
                        + ". Must be between " + CopyNumberStatus.HOMOZYGOUS_DELETION + " and " + CopyNumberStatus.COPY_NUMBER_AMPLIFICATION);
            }
            this.alteration = alteration;
        }

        @Override
        public int hashCode() {
            int hash = 3;
            hash = 97 * hash + (this.gene != null ? this.gene.hashCode() : 0);
            hash = 97 * hash + this.alteration;
            return hash;
        }

        /**
         * Compare by gene and alteration, 
         * but not by eventId 
         */
        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final Event other = (Event) obj;
            if (this.gene != other.gene && (this.gene == null || !this.gene.equals(other.gene))) {
                return false;
            }
            if (this.alteration != other.alteration) {
                return false;
            }
            return true;
        }
        
    }

    public CnaEvent(int sampleId, int cnaProfileId, long entrezGeneId, int alteration) {
        event = new Event();
        setEntrezGeneId(entrezGeneId);
        this.sampleId = sampleId;
        this.cnaProfileId = cnaProfileId;
        event.setAlteration(alteration);
    }

    public Integer getAlteration() {
        return event.alteration;
    }

    public void setAlteration(int alteration) {
        event.setAlteration(alteration);
    }

    public int getSampleId() {
        return sampleId;
    }

    public void setSampleId(int sampleId) {
        this.sampleId = sampleId;
    }

    public int getCnaProfileId() {
        return cnaProfileId;
    }

    public void setCnaProfileId(int cnaProfileId) {
        this.cnaProfileId = cnaProfileId;
    }

    public long getEntrezGeneId() {
        return event.getGene().getEntrezGeneId();
    }
    
    public String getGeneSymbol() {
        return event.getGene().getHugoGeneSymbolAllCaps();
    }

    public void setEntrezGeneId(long entrezGeneId) {
        event.setEntrezGeneId(entrezGeneId);
        if (event.gene == null) {
            throw new IllegalArgumentException("Could not find entrez gene id: "+entrezGeneId);
        } 
    }

    public long getEventId() {
        return event.getEventId();
    }

    public void setEventId(long eventId) {
        event.setEventId(eventId);
    }

    public Event getEvent() {
        return event;
    }

    public void setEvent(Event event) {
        this.event = event;
    }

    public String getDriverFilter() {
        return driverFilter;
    }

    public void setDriverFilter(String driverFilter) {
        this.driverFilter = driverFilter;
    }

    public String getDriverFilterAnnotation() {
        return driverFilterAnnotation;
    }

    public void setDriverFilterAnnotation(String driverFilterAnnotation) {
        this.driverFilterAnnotation = driverFilterAnnotation;
    }

    public String getDriverTiersFilter() {
        return driverTiersFilter;
    }

    public void setDriverTiersFilter(String driverTiersFilter) {
        this.driverTiersFilter = driverTiersFilter;
    }

    public String getDriverTiersFilterAnnotation() {
        return driverTiersFilterAnnotation;
    }

    public void setDriverTiersFilterAnnotation(String driverTiersFilterAnnotation) {
        this.driverTiersFilterAnnotation = driverTiersFilterAnnotation;
    }
    
    public String getAnnotationJson() {
        return annotationJson;
    }

    public void setAnnotationJson(String annotationJson) {
        this.annotationJson = annotationJson;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final CnaEvent other = (CnaEvent) obj;
        if (!(this.sampleId == other.sampleId)) {
            return false;
        }
        if (this.cnaProfileId != other.cnaProfileId) {
            return false;
        }
        if (this.event != other.event && (this.event == null || !this.event.equals(other.event))) {
            return false;
        }
        return true;
    }
    
    
}
