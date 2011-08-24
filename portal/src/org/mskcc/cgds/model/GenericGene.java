package org.mskcc.cgds.model;

import java.util.ArrayList;

public class GenericGene extends Gene {
    private String name;
    private ArrayList<CanonicalGene> geneList;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ArrayList<CanonicalGene> getGeneList() {
        return geneList;
    }

    public void setGeneList(ArrayList<CanonicalGene> geneList) {
        this.geneList = geneList;
    }

    public String toString() {
        StringBuffer buf = new StringBuffer();
        buf.append(name + "={");
        for (CanonicalGene canonicalGene : geneList) {
            buf.append(canonicalGene.getHugoGeneSymbol() + " ");
        }
        buf.deleteCharAt(buf.length() - 1);
        buf.append("}");
        return buf.toString();
    }

    public String getStandardSymbol() {
        return toString();
    }
}