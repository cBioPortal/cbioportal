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

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Encapsulates a Download Link.
 */
public class DownloadLink implements Serializable {
    private GeneticProfile profile;
    private ArrayList<String> geneList;
    private String caseIds;
    private String content;

    /**
     * Constructor.
     *
     * @param profile  GeneticProfile Object.
     * @param geneList ArrayList of Gene Symbols.
     * @param caseIds  Whitespace-delimited list of case Ids.
     * @param content  Content from the CGDS Server.
     */
    public DownloadLink(GeneticProfile profile, ArrayList<String> geneList,
                        String caseIds, String content) {
        this.profile = profile;
        this.geneList = geneList;
        this.caseIds = caseIds;
        this.content = content;
    }

    /**
     * Gets the Genetic Profile.
     *
     * @return GeneticProfile Object.
     */
    public GeneticProfile getProfile() {
        return profile;
    }

    /**
     * Gets the Gene List.
     *
     * @return ArrayList of Gene Symbols.
     */
    public ArrayList<String> getGeneList() {
        return geneList;
    }

    /**
     * Gets the Case IDs.
     *
     * @return whitespace-delimited list of case IDs.
     */
    public String getCaseIds() {
        return caseIds;
    }

    /**
     * Gets the content returned by the CGDS Server.
     *
     * @return CGDS Content.
     */
    public String getContent() {
        return content;
    }
}
