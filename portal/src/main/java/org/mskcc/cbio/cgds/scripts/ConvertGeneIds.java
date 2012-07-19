package org.mskcc.cbio.cgds.scripts;

import org.mskcc.cgds.dao.DaoException;
import org.mskcc.cgds.dao.DaoGeneOptimized;
import org.mskcc.cgds.model.CanonicalGene;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.File;
import java.io.IOException;

/**
 * Command Line Tool to Convert Gene IDs.
 */
public class ConvertGeneIds {

    public static void convertGeneIds(File file, String goCategory) throws IOException, DaoException {
        BufferedReader buf = new BufferedReader (new FileReader(file));
        String line = buf.readLine();
        DaoGeneOptimized daoGene = DaoGeneOptimized.getInstance();
        System.out.println ("GO_CATEGORY");
        while (line != null) {
            line = line.trim();
            CanonicalGene canonicalGene = daoGene.getNonAmbiguousGene(line);
            if (canonicalGene != null) {
                System.out.println (canonicalGene.getHugoGeneSymbolAllCaps() + " = " + goCategory);
            }
            line = buf.readLine();
        }
    }

    public static void main(String[] args) throws Exception {
        if (args.length< 2) {
            System.out.println ("command line usage:  convertGeneIds.pl file_name attribute_name");
            System.exit(1);
        }
        String file = args[0];
        String attribute = args[1];
        convertGeneIds(new File (file), attribute);
    }
}
