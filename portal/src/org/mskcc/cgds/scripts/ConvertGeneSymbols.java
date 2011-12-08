package org.mskcc.cgds.scripts;

import org.mskcc.cgds.dao.DaoException;
import org.mskcc.cgds.dao.DaoGeneOptimized;
import org.mskcc.cgds.model.CanonicalGene;

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
            System.exit(1);
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