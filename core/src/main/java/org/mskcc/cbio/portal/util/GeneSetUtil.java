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

package org.mskcc.cbio.portal.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import org.mskcc.cbio.portal.model.SetOfGenes;

/**
 * Singleton to store arbitrary gene sets, to be displayed to the user.
 * <p/>
 * This will (eventually) be replaced with live gene sets from Broad MSigDB and Pathway Commons.
 *
 * @author Ethan Cerami
 */
public class GeneSetUtil {
    private ArrayList<SetOfGenes> geneSetList = new ArrayList<SetOfGenes>();
    private static GeneSetUtil geneSetUtil;

    /**
     * Gets Global Singleton.
     * @return GeneSetUtil.
     * @throws IOException IO Error.
     */
    public static GeneSetUtil getInstance() throws IOException {
        if (geneSetUtil == null) {
            geneSetUtil = new GeneSetUtil();
        }
        return geneSetUtil;
    }

    /**
     * Private Constructor.
     * @throws IOException IO Error.
     */
    private GeneSetUtil() throws IOException {
        InputStream in = this.getClass().getResourceAsStream("/gene_sets.txt");
        geneSetList = GeneSetReader.readGeneSets(in);
        in.close();
    }

    /**
     * Gets all Gene Sets.
     *
     * @return ArrayList of GeneSet Objects.
     */
    public ArrayList<SetOfGenes> getGeneSetList() {
        return geneSetList;
    }
}
