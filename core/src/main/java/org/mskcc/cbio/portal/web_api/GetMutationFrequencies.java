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

import org.mskcc.cbio.portal.dao.DaoException;
import org.mskcc.cbio.portal.dao.DaoMutationFrequency;
import org.mskcc.cbio.portal.dao.DaoGeneOptimized;
import org.mskcc.cbio.portal.model.CanonicalGene;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.text.DecimalFormat;

/**
 * class to get mutation frequencies
 * @author jgao
 */
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
            buf.append(canonicalGene.getEntrezGeneId()).append(TAB)
                    .append(canonicalGene.getHugoGeneSymbolAllCaps()).append(TAB)
                    .append(formatter.format(canonicalGene.getSomaticMutationFrequency())).append ("\n");
        } else {
            appendHeader(buf);
            ArrayList <CanonicalGene> geneList = daoMutationFrequency.getTop100SomaticMutatedGenes(cancerStudyId);
            for (CanonicalGene canonicalGene :  geneList) {
                buf.append(canonicalGene.getEntrezGeneId()).append(TAB)
                        .append(canonicalGene.getHugoGeneSymbolAllCaps()).append(TAB)
                        .append(formatter.format(canonicalGene.getSomaticMutationFrequency())).append ("\n");
            }
        }
        return buf.toString();
    }

    private static void appendHeader(StringBuffer buf) {
        buf.append("entrez_gene_id\tgene_symbol\tsomatic_mutation_rate\n");
    }
}