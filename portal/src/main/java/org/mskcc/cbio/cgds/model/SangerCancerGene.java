package org.mskcc.cbio.cgds.model;

import java.util.HashMap;
import java.util.ArrayList;

/**
 * Encapsulates a Gene from the Sanger Cancer Gene Census.
 *
 */
public class SangerCancerGene {
    private CanonicalGene gene;
    private boolean cancerSomaticMutation;
    private boolean cancerGermlineMutation;
    private String tumorTypesSomaticMutation;
    private String tumorTypesGermlineMutation;
    private String cancerSyndrome;
    private String tissueType;
    private String mutationType;
    private String translocationPartner;
    private boolean otherGermlineMut;
    private String otherDisease;

    public CanonicalGene getGene() {
        return gene;
    }

    public void setGene(CanonicalGene gene) {
        this.gene = gene;
    }

    public boolean isCancerSomaticMutation() {
        return cancerSomaticMutation;
    }

    public void setCancerSomaticMutation(boolean cancerSomaticMutation) {
        this.cancerSomaticMutation = cancerSomaticMutation;
    }

    public boolean isCancerGermlineMutation() {
        return cancerGermlineMutation;
    }

    public void setCancerGermlineMutation(boolean cancerGermlineMutation) {
        this.cancerGermlineMutation = cancerGermlineMutation;
    }

    public String getTumorTypesSomaticMutation() {
        return tumorTypesSomaticMutation;
    }

    public ArrayList<String> getTumorTypesSomaticMutationList() {
        return extractParts(tumorTypesSomaticMutation);
    }

    public void setTumorTypesSomaticMutation(String tumorTypesSomaticMutation) {
        this.tumorTypesSomaticMutation = tumorTypesSomaticMutation;
    }

    public String getTumorTypesGermlineMutation() {
        return tumorTypesGermlineMutation;
    }

    public ArrayList<String> getTumorTypesGermlineMutationList() {
        return extractParts(tumorTypesGermlineMutation);
    }

    public void setTumorTypesGermlineMutation(String tumorTypesGermlineMutation) {
        this.tumorTypesGermlineMutation = tumorTypesGermlineMutation;
    }

    public String getCancerSyndrome() {
        return cancerSyndrome;
    }

    public void setCancerSyndrome(String cancerSyndrome) {
        this.cancerSyndrome = cancerSyndrome;
    }

    public String getTissueType() {
        return tissueType;
    }

    public ArrayList<String> getTissueTypeList() {
        return extractParts(tissueType);
    }

    public void setTissueType(String tissueType) {
        this.tissueType = tissueType;
    }

    public String getMutationType() {
        return mutationType;
    }

    public ArrayList<String> getMutationTypeList() {
        return extractParts(mutationType);
    }

    public void setMutationType(String mutationType) {
        this.mutationType = mutationType;
    }

    public String getTranslocationPartner() {
        return translocationPartner;
    }

    public void setTranslocationPartner(String translocationPartner) {
        this.translocationPartner = translocationPartner;
    }

    public boolean getOtherGermlineMut() {
        return otherGermlineMut;
    }

    public void setOtherGermlineMut(boolean otherGermlineMut) {
        this.otherGermlineMut = otherGermlineMut;
    }

    public String getOtherDisease() {
        return otherDisease;
    }

    public void setOtherDisease(String otherDisease) {
        this.otherDisease = otherDisease;
    }

    private ArrayList<String> extractParts(String target) {
        String parts[] = target.split(",");
        SangerCancerGeneAbbreviationMap abbrevMap = SangerCancerGeneAbbreviationMap.getInstance();
        ArrayList <String> partList = new ArrayList<String>();
        for (String part:  parts) {
            String translation = abbrevMap.getTranslation(part.trim());
            if (translation != null) {
                partList.add(translation);
            } else {
                partList.add(part);
            }
        }
        return partList;
    }
}
