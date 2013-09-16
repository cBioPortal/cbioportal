/** Copyright (c) 2012 Memorial Sloan-Kettering Cancer Center.
**
** This library is free software; you can redistribute it and/or modify it
** under the terms of the GNU Lesser General Public License as published
** by the Free Software Foundation; either version 2.1 of the License, or
** any later version.
**
** This library is distributed in the hope that it will be useful, but
** WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
** MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
** documentation provided hereunder is on an "as is" basis, and
** Memorial Sloan-Kettering Cancer Center 
** has no obligations to provide maintenance, support,
** updates, enhancements or modifications.  In no event shall
** Memorial Sloan-Kettering Cancer Center
** be liable to any party for direct, indirect, special,
** incidental or consequential damages, including lost profits, arising
** out of the use of this software and its documentation, even if
** Memorial Sloan-Kettering Cancer Center 
** has been advised of the possibility of such damage.  See
** the GNU Lesser General Public License for more details.
**
** You should have received a copy of the GNU Lesser General Public License
** along with this library; if not, write to the Free Software Foundation,
** Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
**/

package org.mskcc.cbio.portal.model;

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
