/*
 * Copyright (c) 2015 Memorial Sloan-Kettering Cancer Center.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF MERCHANTABILITY OR FITNESS
 * FOR A PARTICULAR PURPOSE. The software and documentation provided hereunder
 * is on an "as is" basis, and Memorial Sloan-Kettering Cancer Center has no
 * obligations to provide maintenance, support, updates, enhancements or
 * modifications. In no event shall Memorial Sloan-Kettering Cancer Center be
 * liable to any party for direct, indirect, special, incidental or
 * consequential damages, including lost profits, arising out of the use of this
 * software and its documentation, even if Memorial Sloan-Kettering Cancer
 * Center has been advised of the possibility of such damage.
 */

/*
 * This file is part of cBioPortal.
 *
 * cBioPortal is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

package org.mskcc.cbio.portal.model;

/**
 * Encapsulates Clinical Data.
 *
 * @author Dong Li <dresdnerg@cbio.mskcc.org>
 */
public class GenePanel {
    private int listId;
    private String stableId;
    private String cancerStudyId;
    private String discription;
    private String genelist;

    /**
     * Constructor
     */
    public GenePanel() {
        this(-1, "", "", "","");
    }
    
    public GenePanel(GenePanel other) {
        this(other.getGenePanelListId(), other.getStableId(), other.getCancerStudyId(), other.getDiscription(), other.getGenelist());
    }

    public GenePanel(int listId,
                        String stableId,
                        String cancerStudyId,
                        String discription,
                        String genelist) {

        this.listId = listId;
        this.stableId = stableId;
        this.cancerStudyId = cancerStudyId;
        this.discription = discription;
        this.genelist = genelist;
    }

    public int getGenePanelListId() {
        return listId;
    }

    public void setGenePanelListId(int listId) {
        this.listId = listId;
    }

    public String getStableId() {
        return stableId;
    }

    public void setStableId(String stableId) {
        this.stableId = stableId;
    }

    public String getCancerStudyId() {
        return cancerStudyId;
    }

    public void setCancerStudyId(String cancerStudyId) {
        this.cancerStudyId = cancerStudyId;
    }

    public String getDiscription() {
        return discription;
    }

    public void setAttrVal(String discription) {
        this.discription = discription;
    }
    
    public String getGenelist() {
        return genelist;
    }

    public void setGenelist(String genelist) {
        this.genelist = genelist;
    }

    public String toString() {
        return String.format("GenePanelData[cancerStudyId=%d, %s, %s, %s, %s]", listId, stableId, cancerStudyId, discription, genelist);
    }
}
