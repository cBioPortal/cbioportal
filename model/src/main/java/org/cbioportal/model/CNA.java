package org.cbioportal.model;

import java.util.HashMap;
import java.util.Map;

public enum CNA {
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