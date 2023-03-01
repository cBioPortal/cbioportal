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
 * Encapsulates an Interaction.
 *
 * @author Ethan Cerami.
 */
public class Interaction {
    private long geneA;
    private long geneB;
    private String interactionType;
    private String experimentTypes;
    private String pmids;
    private String source;

    /**
     * Gets Entrez Gene ID for Gene A.
     *
     * @return Entrez Gene ID for Gene A.
     */
    public long getGeneA() {
        return geneA;
    }

    /**
     * Sets Entrez Gene ID for Gene A.
     *
     * @param entrezGeneId Entrez Gene ID for Gene A.
     */
    public void setGeneA(long entrezGeneId) {
        this.geneA = entrezGeneId;
    }

    /**
     * Gets Entrez Gene ID for Gene B.
     *
     * @return symbol for Gene B.
     */
    public long getGeneB() {
        return geneB;
    }

    /**
     * Sets Entrez Gene ID for Gene B.
     *
     * @param entrezGeneID Entrez Gene ID for Gene B.
     */
    public void setGeneB(long entrezGeneID) {
        this.geneB = entrezGeneID;
    }

    /**
     * Gets the Interaction Type.
     *
     * @return interaction type.
     */
    public String getInteractionType() {
        return interactionType;
    }

    /**
     * Sets the Interaction Type.
     *
     * @param type interaction type.
     */
    public void setInteractionType(String type) {
        this.interactionType = type;
    }

    /**
     * Gets the Experiment Types.
     *
     * @return experiment types.
     */
    public String getExperimentTypes() {
        return experimentTypes;
    }

    /**
     * Sets the Experiment Types.
     *
     * @param expTypes experiment types.
     */
    public void setExperimentTypes(String expTypes) {
        this.experimentTypes = expTypes;
    }

    /**
     * Gets the PMIDs.
     *
     * @return PMIDs.
     */
    public String getPmids() {
        return pmids;
    }

    /**
     * Sets the PMIDs.
     *
     * @param p PMIDs.
     */
    public void setPmids(String p) {
        this.pmids = p;
    }

    /**
     * Gets the data source, where this interaction comes from, e.g. REACTOME.
     *
     * @return data source.
     */
    public String getSource() {
        return source;
    }

    /**
     * Sets the data source, where this interaction comes from, e.g. REACTOME.
     *
     * @param s data source
     */
    public void setSource(String s) {
        this.source = s;
    }

    @Override
    /**
     * Overrides toString()
     */
    public String toString() {
        return "Interaction:  " + geneA + " " + interactionType + " " + geneB + ", " + source;
    }

    /**
     * Provides a Cytoscape SIF Version of this Interaction.
     *
     * @return SIF Text.
     */
    public String toSif() {
        return geneA + " " + interactionType + " " + geneB;
    }
}