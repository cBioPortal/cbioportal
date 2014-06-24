/** Copyright (c) 2012 Memorial Sloan-Kettering Cancer Center.
 *
 * This library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
 * MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
 * documentation provided hereunder is on an "as is" basis, and
 * Memorial Sloan-Kettering Cancer Center 
 * has no obligations to provide maintenance, support,
 * updates, enhancements or modifications.  In no event shall
 * Memorial Sloan-Kettering Cancer Center
 * be liable to any party for direct, indirect, special,
 * incidental or consequential damages, including lost profits, arising
 * out of the use of this software and its documentation, even if
 * Memorial Sloan-Kettering Cancer Center 
 * has been advised of the possibility of such damage.
*/

package org.mskcc.cbio.portal.web_api;

import org.mskcc.cbio.portal.model.Gene;
import org.mskcc.cbio.portal.model.MicroRna;
import org.mskcc.cbio.portal.model.GeneticAlterationType;
import org.mskcc.cbio.portal.servlet.ServletXssUtil;
import org.mskcc.cbio.portal.util.GeneComparator;
import org.mskcc.cbio.portal.dao.DaoMicroRna;
import org.mskcc.cbio.portal.dao.DaoException;
import org.mskcc.cbio.portal.dao.DaoGeneOptimized;
import org.owasp.validator.html.PolicyException;

import java.util.Date;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;

/**
 * Utility class for web api
 */
public class WebApiUtil {
    private static HashSet <String> microRnaIdSet;
    private static HashSet <String> variantMicroRnaIdSet;
    public static final String TAB = "\t";
    public static final String NEW_LINE = "\n";

    public static ArrayList <Gene> getGeneList (ArrayList<String> targetGeneList,
                    GeneticAlterationType alterationType, StringBuffer warningBuffer,
                    ArrayList<String> warningList) throws DaoException {
        DaoGeneOptimized daoGene = DaoGeneOptimized.getInstance();
        DaoMicroRna daoMicroRna = new DaoMicroRna();
        if (microRnaIdSet == null) {
            microRnaIdSet = daoMicroRna.getEntireSet();
            variantMicroRnaIdSet = daoMicroRna.getEntireVariantSet();
        }

	    ServletXssUtil xssUtil = null;

	    try {
		    xssUtil = ServletXssUtil.getInstance();
	    }
	    catch (Exception e) {
	    }

	    //  Iterate through all the genes specified by the client
        //  Genes might be specified as Integers, e.g. Entrez Gene Ids or Strings, e.g. HUGO
        //  Symbols or microRNA Ids or aliases.
        ArrayList <Gene> geneList = new ArrayList<Gene>();
        for (String geneId:  targetGeneList) {
            Gene gene = daoGene.getNonAmbiguousGene(geneId);
            if (gene == null) {
                //  If that fails, try as micro RNA ID.
                if (geneId.startsWith("hsa")) {
                    if (microRnaIdSet.contains(geneId)) {
                        //  Conditionally Expand Micro RNAs
                        if (alterationType.equals(GeneticAlterationType.COPY_NUMBER_ALTERATION)) {
                            //  Option 1:  Client has specified a variant ID and really wants CNA
                            //  data for this variant
                            if (variantMicroRnaIdSet.contains(geneId)) {
                                MicroRna microRna = new MicroRna(geneId);
                                geneList.add(microRna);
                            } else {
                                //  Option 2:  Client has specified a primary ID, and we need to map
                                //  to all variants
                                ArrayList <String> variantList = daoMicroRna.getVariantIds(geneId);
                                for (String variant:  variantList) {
                                    MicroRna microRna = new MicroRna(variant);
                                    geneList.add(microRna);
                                }
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
                        warningBuffer.append(msg).append ("\n");
                        warningList.add(msg);
                    }
                } else {
	                if (xssUtil != null) {
		                geneId = xssUtil.getCleanerInput(geneId);
	                }
                    String msg = "# Warning:  Unknown gene:  " + geneId;
                    warningBuffer.append(msg).append ("\n");
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
