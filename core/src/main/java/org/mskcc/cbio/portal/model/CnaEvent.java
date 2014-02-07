
package org.mskcc.cbio.portal.model;

import java.util.HashMap;
import java.util.Map;

import org.mskcc.cbio.portal.dao.DaoGeneOptimized;

/**
 *
 * @author jgao
 */
public class CnaEvent {
    private String caseId;
    private int cnaProfileId;;
    private Event event;
    
    public static enum CNA {
        AMP ((short)2, "Amplified"),
        GAIN ((short)1, "Gained"),
        DIPLOID ((short)0, "Diploid"),
        HETLOSS ((short)-1, "Heterozygously deleted"),
        HOMDEL ((short)-2, "Homozygously deleted");
        
        private short code;
        private String desc;
        
        private CNA(short code, String desc) {
            this.code = code;
            this.desc = desc;
        }
        
        private final static Map<Short, CNA> cache = new HashMap<Short, CNA>();
        static {
            for (CNA cna : CNA.values()) {
                cache.put(cna.code, cna);
            }
        }
        
        public static CNA getByCode(short code) {
            return cache.get(code);
        }
        
        public short getCode() {
            return code;
        }
        
        public String getDescription() {
            return desc;
        }
    }
    
    public static class Event {
        private long eventId;
        private CanonicalGene gene;
        private CNA alteration;

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
                throw new IllegalArgumentException("Could not find entrez gene id: "+entrezGeneId);
            } 
        }

        public CNA getAlteration() {
            return alteration;
        }

        public void setAlteration(CNA alteration) {
            this.alteration = alteration;
        }
        
        public void setAlteration(short alteration) {
            this.alteration = CNA.getByCode(alteration);
            if (this.alteration == null) {
                throw new IllegalArgumentException("wrong copy number alteration");
            }
        }

        @Override
        public int hashCode() {
            int hash = 3;
            hash = 97 * hash + (this.gene != null ? this.gene.hashCode() : 0);
            hash = 97 * hash + (this.alteration != null ? this.alteration.hashCode() : 0);
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

    public CnaEvent(String caseId, int cnaProfileId, long entrezGeneId, short alteration) {
        event = new Event();
        setEntrezGeneId(entrezGeneId);
        this.caseId = caseId;
        this.cnaProfileId = cnaProfileId;
        event.setAlteration(alteration);
    }

    public CNA getAlteration() {
        return event.alteration;
    }

    public void setAlteration(CNA alteration) {
        event.setAlteration(alteration);
    }

    public void setAlteration(short alteration) {
        event.setAlteration(CNA.getByCode(alteration));
    }

    public String getCaseId() {
        return caseId;
    }

    public void setCaseId(String caseId) {
        this.caseId = caseId;
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
        if ((this.caseId == null) ? (other.caseId != null) : !this.caseId.equals(other.caseId)) {
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
