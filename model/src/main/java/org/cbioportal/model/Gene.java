package org.cbioportal.model;

import java.io.Serializable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Gene implements Serializable {

    private Integer entrezGeneId;
    private String hugoGeneSymbol;
    private String type;
    private String cytoband;
    private Integer length;

    public Integer getEntrezGeneId() {
        return entrezGeneId;
    }

    public void setEntrezGeneId(Integer entrezGeneId) {
        this.entrezGeneId = entrezGeneId;
    }

    public String getHugoGeneSymbol() {
        return hugoGeneSymbol;
    }

    public void setHugoGeneSymbol(String hugoGeneSymbol) {
        this.hugoGeneSymbol = hugoGeneSymbol;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getCytoband() {
        return cytoband;
    }

    public void setCytoband(String cytoband) {
        this.cytoband = cytoband;
    }

    public Integer getLength() {
        return length;
    }

    public void setLength(Integer length) {
        this.length = length;
    }
    
    public String getChromosome() {
        if (cytoband == null) {
            return null;
        }
        if (cytoband.toUpperCase().startsWith("X")) {
            return "X";
        }
        if (cytoband.toUpperCase().startsWith("Y")) {
            return "Y";
        }
        
        Pattern p = Pattern.compile("([0-9]+).*");
        Matcher m = p.matcher(cytoband);
        if (m.find()) {
            return m.group(1);
        }
        
        return null;
    }
}