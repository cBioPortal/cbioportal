package org.mskcc.cgds.web_api;

import org.mskcc.cgds.dao.DaoException;
import org.mskcc.cgds.dao.DaoMutationFrequency;
import org.mskcc.cgds.dao.DaoGeneOptimized;
import org.mskcc.cgds.model.CanonicalGene;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.text.DecimalFormat;

public class GetMutationFrequencies {
    public static final String TAB = "\t";

    public static String getMutationFrequencies( int cancerStudyId,
            HttpServletRequest httpServletRequest) throws DaoException, ProtocolException {
        StringBuffer buf = new StringBuffer();
        DaoMutationFrequency daoMutationFrequency = new DaoMutationFrequency();
        DecimalFormat formatter = new DecimalFormat("#,###,###.#####");
        String gene = httpServletRequest.getParameter("gene");
        if (gene != null) {
            DaoGeneOptimized daoGene = DaoGeneOptimized.getInstance();
            CanonicalGene canonicalGene = daoGene.getGene(gene);
            if (canonicalGene == null) {
                throw new ProtocolException ("Don't know gene:  " + gene);
            }
            appendHeader(buf);
            canonicalGene = daoMutationFrequency.getSomaticMutationFrequency(canonicalGene.getEntrezGeneId());
            buf.append (canonicalGene.getEntrezGeneId() + TAB + canonicalGene.getHugoGeneSymbolAllCaps() + TAB
                + formatter.format(canonicalGene.getSomaticMutationFrequency()) + "\n");
        } else {
            appendHeader(buf);
            ArrayList <CanonicalGene> geneList = daoMutationFrequency.getTop100SomaticMutatedGenes(cancerStudyId);
            for (CanonicalGene canonicalGene :  geneList) {
                buf.append (canonicalGene.getEntrezGeneId() + TAB + canonicalGene.getHugoGeneSymbolAllCaps() + TAB
                    + formatter.format(canonicalGene.getSomaticMutationFrequency()) + "\n");
            }
        }
        return buf.toString();
    }

    private static void appendHeader(StringBuffer buf) {
        buf.append("entrez_gene_id\tgene_symbol\tsomatic_mutation_rate\n");
    }
}