
package org.mskcc.cbio.cgds.model;

import java.util.HashMap;
import java.util.Map;

import org.mskcc.cbio.cgds.dao.DaoGeneOptimized;
import org.mskcc.cbio.cgds.dao.DaoException;

/**
 *
 * @author jgao
 */
public class CnaEvent {
    private String caseId;
    private int cnaProfileId;
    private long eventId;
    private long entrezGeneId;
    private CNA alteration;
    
    public static enum CNA {
        AMP ((short)2, "Amplified"),
        GAIN ((short)1, "Gained"),
        HETLOSS ((short)-1, "Hemizygously deleted"),
        HOMDEL ((short)-2, "Homozygously deleted");
        
        private short code;
        private String desc;
        
        private CNA(short code, String desc) {
            this.code = code;
            this.desc = desc;
        }
        
        private static Map<Short, CNA> cache = new HashMap<Short, CNA>();
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

    public CnaEvent(String caseId, int cnaProfileId, long entrezGeneId, short alteration) {
        this.caseId = caseId;
        this.cnaProfileId = cnaProfileId;
        this.entrezGeneId = entrezGeneId;
        this.alteration = CNA.getByCode(alteration);
        if (this.alteration == null) {
            throw new IllegalArgumentException("wrong copy number alteration");
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
        return entrezGeneId;
    }
    
    public String getGeneSymbol() {
        try {
            return DaoGeneOptimized.getInstance().getGene(entrezGeneId).getHugoGeneSymbolAllCaps();
        } catch (DaoException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    public void setEntrezGeneId(long entrezGeneId) {
        this.entrezGeneId = entrezGeneId;
    }

    public long getEventId() {
        return eventId;
    }

    public void setEventId(long eventId) {
        this.eventId = eventId;
    }
}
