/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.mskcc.cbio.portal.model;

/**
 *
 * @author dongli
 */
public class GenePanelListData {
    private int listId;
    private long geneId;

    /**
     * Constructor
     */
    public GenePanelListData() {
        this(-1,-1);
    }
    
    public GenePanelListData(GenePanelListData other) {
        this(other.getGenePanelListId(), other.getGeneId());
    }

    /**
     * Constructor
     *
     * @param cancerStudyId     database id of cancer study
     * @param stableId          stable id of the patient or sample
     * @param attrId            database id of the attribute
     * @param attrVal           value of the clinical attribute given above
     */
    public GenePanelListData(int listId,
                        long geneId) {

        this.listId = listId;
        this.geneId = geneId;
    }

    public int getGenePanelListId() {
        return listId;
    }

    public void setGenePanelListId(int listId) {
        this.listId = listId;
    }

    public long getGeneId() {
        return geneId;
    }

    public void setGeneId(int geneId) {
        this.geneId = geneId;
    }

    public String toString() {
        return String.format("GenePanelData[cancerStudyId=%d, %s, %s, %s]", listId, geneId);
    }
    
}
