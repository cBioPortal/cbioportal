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

package org.mskcc.cbio.portal.scripts;

import org.mskcc.cbio.portal.dao.DaoException;
import org.mskcc.cbio.portal.dao.DaoGeneOptimized;
import org.mskcc.cbio.portal.model.CanonicalGene;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

/**
 * Given a list of Genes in a File (specified as Gene Symbols or Aliases),
 * convert these genes to Standard HUGO Gene Symbols that the Portal understands.
 *
 * @author Ethan Cerami.
 */
public class ConvertGeneSymbols {

    public static void main(String[] args) throws DaoException, IOException {
        if (args.length < 1) {
            System.out.println("command line usage:  updateGeneSymbols.pl " + "<file_name.txt>");
            return;
        }
        String fileName = args[0];
        FileReader reader = new FileReader(fileName);
        BufferedReader bufferedReader = new BufferedReader(reader);
        String line = bufferedReader.readLine();
        DaoGeneOptimized daoGene = DaoGeneOptimized.getInstance();
        while (line != null) {
            String geneId = line.trim();
            List<CanonicalGene> geneList = daoGene.guessGene(geneId);
            if (geneList != null && geneList.size() == 1) {
                CanonicalGene gene = geneList.get(0);
                System.out.println(gene.getHugoGeneSymbolAllCaps());
            }
            line = bufferedReader.readLine();
        }
    }
}