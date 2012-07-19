package org.mskcc.cbio.cgds.web_api;

import org.mskcc.cbio.cgds.model.Gene;
import org.mskcc.cbio.cgds.model.MicroRna;
import org.mskcc.cbio.cgds.model.GeneticAlterationType;
import org.mskcc.cbio.cgds.util.GeneComparator;
import org.mskcc.cbio.cgds.dao.DaoMicroRna;
import org.mskcc.cbio.cgds.dao.DaoException;
import org.mskcc.cbio.cgds.dao.DaoGeneOptimized;

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
    public static final String WEP_API_HEADER = "# CGDS Kernel:  Data served up fresh at:  "
            + new Date() +"\n";
    public static final String TAB = "\t";
    public static final String NEW_LINE = "\n";


    public static void outputWebApiHeader(StringBuffer buf) {
        buf.append (WEP_API_HEADER);
    }

    public static ArrayList <Gene> getGeneList (ArrayList<String> targetGeneList,
                    GeneticAlterationType alterationType, StringBuffer warningBuffer,
                    ArrayList<String> warningList) throws DaoException {
        DaoGeneOptimized daoGene = DaoGeneOptimized.getInstance();
        DaoMicroRna daoMicroRna = new DaoMicroRna();
        if (microRnaIdSet == null) {
            microRnaIdSet = daoMicroRna.getEntireSet();
            variantMicroRnaIdSet = daoMicroRna.getEntireVariantSet();
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
                        String msg = "# Warning:  Unknown microRNA:  " + geneId;
                        warningBuffer.append(msg).append ("\n");
                        warningList.add(msg);
                    }
                } else {
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
