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

import org.mskcc.cbio.portal.oncoPrintSpecLanguage.GeneWithSpec;

/**
 * Encapsulates a Gene with a Score.
 * Score is used to rank genes.
 */
public class GeneWithScore {
    private String gene;
    private int entrezGeneId;
    private double score;
    // apg: was: private GeneCounterPrefs counterPrefs;
    private GeneWithSpec aGeneWithSpec;

    /**
     * Constructor.
     */
    public GeneWithScore() {
       // apg: was: counterPrefs = new GeneCounterPrefs();
       // TODO: decide if this constructor is needed: aGeneWithSpec = new GeneWithSpec();
    }

    /**
     * Gets the Gene Symbol.
     *
     * @return gene symbol.
     */
    public String getGene() {
        return gene;
    }

    /**
     * Sets the Gene Symbol.
     *
     * @param gene gene symbol.
     */
    public void setGene(String gene) {
        this.gene = gene;
    }

    /**
     * Gets Entrez Gene Id.
     *
     * @return Entrez Gene Id.
     */
    public int getEntrezGeneId() {
        return entrezGeneId;
    }

    /**
     * Sets Entrez Gene Id.
     *
     * @param entrezGeneId Entrez Gene Id;
     */
    public void setEntrezGeneId(int entrezGeneId) {
        this.entrezGeneId = entrezGeneId;
    }

    /**
     * Gets the score.
     *
     * @return gene score.
     */
    public double getScore() {
        return score;
    }

    /**
     * Sets the gene score.
     *
     * @param score gene score.
     */
    public void setScore(double score) {
        this.score = score;
    }

    /**
     * Gets the Counter Prefs.
     * @return Counter Prefs.
     */
    /* apg: was
    public GeneCounterPrefs getCounterPrefs() {
        return counterPrefs;
    }
     */
   public GeneWithSpec getaGeneWithSpec() {
      return aGeneWithSpec;
   }


    /**
     * Sets the Counter Prefs;
     * @param counterPrefs Counter Prefs.
     */
    /* apg: was
    public void setCounterPrefs(GeneCounterPrefs counterPrefs) {
        this.counterPrefs = counterPrefs;
    }
    */
   public void setaGeneWithSpec(GeneWithSpec aGeneWithSpec) {
      this.aGeneWithSpec = aGeneWithSpec;
   }
    
}