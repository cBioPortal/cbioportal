/*
 * Copyright (c) 2015 - 2016 Memorial Sloan-Kettering Cancer Center.
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

package org.mskcc.cbio.portal.web_api;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import org.mskcc.cbio.portal.dao.DaoException;
import org.mskcc.cbio.portal.dao.DaoGeneOptimized;
import org.mskcc.cbio.portal.model.Gene;
import org.mskcc.cbio.portal.model.GeneticAlterationType;
import org.mskcc.cbio.portal.model.MicroRna;
import org.mskcc.cbio.portal.servlet.ServletXssUtil;
import org.mskcc.cbio.portal.util.GeneComparator;

/**
 * Utility class for web api
 */
public class WebApiUtil {
    private static HashSet<String> microRnaIdSet;
    private static HashSet<String> variantMicroRnaIdSet;
    public static final String TAB = "\t";
    public static final String NEW_LINE = "\n";

    public static List<Gene> getGeneList(
        List<String> targetGeneList,
        GeneticAlterationType alterationType,
        StringBuffer warningBuffer,
        List<String> warningList
    )
        throws DaoException {
        DaoGeneOptimized daoGene = DaoGeneOptimized.getInstance();

        ServletXssUtil xssUtil = null;

        try {
            xssUtil = ServletXssUtil.getInstance();
        } catch (Exception e) {}

        //  Iterate through all the genes specified by the client
        //  Genes might be specified as Integers, e.g. Entrez Gene Ids or Strings, e.g. HUGO
        //  Symbols or microRNA Ids or aliases.
        List<Gene> geneList = new ArrayList<Gene>();
        for (String geneId : targetGeneList) {
            Gene gene = daoGene.getNonAmbiguousGene(geneId);
            if (gene == null) {
                //  If that fails, try as micro RNA ID.
                if (geneId.startsWith("hsa")) {
                    if (microRnaIdSet.contains(geneId)) {
                        //  Conditionally Expand Micro RNAs
                        if (
                            alterationType ==
                            GeneticAlterationType.COPY_NUMBER_ALTERATION
                        ) {
                            //  Option 1:  Client has specified a variant ID and really wants CNA
                            //  data for this variant
                            if (variantMicroRnaIdSet.contains(geneId)) {
                                MicroRna microRna = new MicroRna(geneId);
                                geneList.add(microRna);
                            }
                        } else {
                            MicroRna microRna = new MicroRna(geneId);
                            geneList.add(microRna);
                        }
                    } else {
                        if (xssUtil != null) {
                            geneId = xssUtil.getCleanerInput(geneId);
                        }
                        String msg = "# Warning:  Unknown microRNA:  " + geneId;
                        warningBuffer.append(msg).append("\n");
                        warningList.add(msg);
                    }
                } else {
                    if (xssUtil != null) {
                        geneId = xssUtil.getCleanerInput(geneId);
                    }
                    String msg = "# Warning:  Unknown gene:  " + geneId;
                    warningBuffer.append(msg).append("\n");
                    warningList.add(msg);
                }
            } else {
                geneList.add(gene);
            }
        }
        Collections.sort(geneList, new GeneComparator());
        return geneList;
    }
}
