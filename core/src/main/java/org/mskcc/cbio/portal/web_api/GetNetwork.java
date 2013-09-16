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

package org.mskcc.cbio.portal.web_api;

import org.mskcc.cbio.portal.dao.DaoException;
import org.mskcc.cbio.portal.dao.DaoGeneOptimized;
import org.mskcc.cbio.portal.model.CanonicalGene;
import org.mskcc.cbio.portal.graph.NetworkOfInterest;

import java.util.ArrayList;

/**
 * Gets a Network of Interest, Based on Input Seed Gene List.
 *
 * @author Ethan Cerami.
 */
public class GetNetwork {
    

    /**
     * Gets Network of Interest, Based on Input Seed Gene List.
     *
     * @param targetGeneList    Target Gene List.
     * @return Tab-Delimited SIF Like Output.
     * @throws DaoException Database Error.
     * @throws ProtocolException Protocol Exception.
     */
    public static String getNetwork(ArrayList<String> targetGeneList) throws DaoException,
        ProtocolException {

        //  Convert Gene Symbols to Canonical Gene List.
        ArrayList<CanonicalGene> canonicalGeneList = new ArrayList<CanonicalGene>();
        DaoGeneOptimized daoGeneOptimized = DaoGeneOptimized.getInstance();
        for (String geneSymbol:  targetGeneList) {
            CanonicalGene canonicalGene = daoGeneOptimized.getGene(geneSymbol);
            if (canonicalGene != null) {
                canonicalGeneList.add(canonicalGene);
            }
        }
        if (canonicalGeneList.isEmpty()) {
            throw new ProtocolException ("You must specify at least one gene.");
        }

        //  Get the Network of Interest
        NetworkOfInterest noi = new NetworkOfInterest(canonicalGeneList,null);
        return noi.getTabDelim();
    }
}