package org.mskcc.cgds.web_api;

import org.mskcc.cgds.model.Gene;
import org.mskcc.cgds.model.MicroRna;
import org.mskcc.cgds.model.GeneticAlterationType;
import org.mskcc.cgds.util.GeneComparator;
import org.mskcc.cgds.dao.DaoMicroRna;
import org.mskcc.cgds.dao.DaoException;
import org.mskcc.cgds.dao.DaoGeneOptimized;

import java.util.Date;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;

public class WebApiUtil {
    private static HashSet <String> microRnaIdSet;
    private static HashSet <String> variantMicroRnaIdSet;
    public static String WEP_API_HEADER = "# CGDS Kernel:  Data served up fresh at:  "
            + new Date() +"\n";

    public static void outputWebApiHeader(StringBuffer buf) {
        buf.append (WEP_API_HEADER);
    }

    public static ArrayList <Gene> getGeneList (ArrayList<String> targetGeneList,
                    GeneticAlterationType alterationType, StringBuffer warningBuffer) throws DaoException {
        DaoGeneOptimized daoGene = DaoGeneOptimized.getInstance();
        DaoMicroRna daoMicroRna = new DaoMicroRna();
        if (microRnaIdSet == null) {
            microRnaIdSet = daoMicroRna.getEntireSet();
            variantMicroRnaIdSet = daoMicroRna.getEntireVariantSet();
        }

        //  Iterate through all the genes specified by the client
        //  Genes might be specified as Integers, e.g. Entrez Gene Ids or Strings, e.g. HUGO
        //  Symbols or microRNA Ids.
        ArrayList <Gene> geneList = new ArrayList<Gene>();
        for (String geneId:  targetGeneList) {
            Gene gene = null;
            try {
                //  First, try as Entrez Gene Id
                gene = daoGene.getGene(Integer.parseInt(geneId));
            } catch (NumberFormatException e) {
                //  If that fails, try HUGO Gene Symbol
                gene = daoGene.getGene(geneId);
            }
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
                        warningBuffer.append ("# Warning:  Unknown microRNA:  " + geneId + "\n");
                    }
                } else {
                    warningBuffer.append ("# Warning:  Unknown gene:  " + geneId + "\n");
                }
            } else {
                geneList.add(gene);
            }
        }
        Collections.sort(geneList, new GeneComparator());
        return geneList;
    }
}
