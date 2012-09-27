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

package org.mskcc.cbio.cgds.scripts;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.mskcc.cbio.cgds.dao.DaoException;
import org.mskcc.cbio.cgds.dao.DaoGeneOptimized;
import org.mskcc.cbio.cgds.dao.MySQLbulkLoader;
import org.mskcc.cbio.cgds.model.CanonicalGene;
import org.mskcc.cbio.cgds.util.ConsoleUtil;
import org.mskcc.cbio.cgds.util.FileUtil;
import org.mskcc.cbio.cgds.util.ProgressMonitor;

/**
 * Command Line Tool to Import Background Gene Data.
 */
public class ImportMicroRNAIDs {
    private ProgressMonitor pMonitor;
    private File geneFile;

    public ImportMicroRNAIDs(File geneFile, ProgressMonitor pMonitor) {
        this.geneFile = geneFile;
        this.pMonitor = pMonitor;
    }

    public void importData() throws IOException, DaoException {
        MySQLbulkLoader.bulkLoadOff();
        FileReader reader = new FileReader(geneFile);
        BufferedReader buf = new BufferedReader(reader);
        String line = buf.readLine(); // skip first line
        DaoGeneOptimized daoGene = DaoGeneOptimized.getInstance();
        
        List<CanonicalGene> mirnas = new ArrayList<CanonicalGene>();
        
        while ((line=buf.readLine()) != null) {
            if (pMonitor != null) {
                pMonitor.incrementCurValue();
                ConsoleUtil.showProgress(pMonitor);
            }
            if (!line.startsWith("#")) {
                String parts[] = line.split("\t");
                
                String geneSymbol = parts[2];
                
                Set<String> aliases = new HashSet<String>();
                setAliases(parts[0],aliases);
                
                if (!parts[0].equalsIgnoreCase(parts[1])) {
                    setAliases(parts[1],aliases);
                }
                
                CanonicalGene mirna = new CanonicalGene(geneSymbol,aliases);
                mirnas.add(mirna);
            }
        }
        
        removePreviousMicroRNARecord(daoGene, mirnas);
        
        for (CanonicalGene mirna : mirnas) {
            daoGene.addGene(mirna);
        }       
    }
    
    private void setAliases(String hsa, Set<String> aliases) {
        aliases.add(hsa);
        if (hsa.startsWith("hsa-")) {
            String mir = hsa.substring(4).toUpperCase();
            aliases.add(mir);
            aliases.add(getHUGOInNCBIFile(mir));
        }
    }
    
    private String getHUGOInNCBIFile(String mir) {
        StringBuilder sb = new StringBuilder();
        sb.append("MIR");
        if (mir.startsWith("LET")) {
            sb.append("LET");
        }
        
        int ix = mir.indexOf("-");
        sb.append(mir.substring(ix+1));
        return sb.toString();
    }
    
    /**
     * 
     * @param daoGene
     * @param id
     * @param mirnas 
     */
    private void removePreviousMicroRNARecord(DaoGeneOptimized daoGene, List<CanonicalGene> mirnas) {
        for (CanonicalGene mirna : mirnas) {
            Set<String> aliases = new HashSet<String>();
            aliases.addAll(mirna.getAliases());
            for (String mirnaid : mirna.getAliases()) {
                List<CanonicalGene> pres = new ArrayList<CanonicalGene>(daoGene.guessGene(mirnaid));
                for (CanonicalGene pre : pres) {
                    String preCap = pre.getHugoGeneSymbolAllCaps();
                    if (!preCap.startsWith("MIR")&&!preCap.startsWith("LET")) {
                        continue;
                    }
//                    aliases.add(pre.getStandardSymbol());
//                    aliases.add(Long.toString(pre.getEntrezGeneId()));
//                    aliases.addAll(pre.getAliases());
                    try {
                        daoGene.deleteGene(pre);
                    } catch (DaoException e) {
                        e.printStackTrace();
                    }
                }
            }
            if(aliases.size()>2) {
                mirna.setAliases(aliases);
            }
        }
        
    }

    public static void main(String[] args) throws Exception {
  
        if (args.length == 0) {
            System.out.println("command line usage:  importMicroRNAIDs.pl <microrna.txt>");
            System.exit(1);
        }
        ProgressMonitor pMonitor = new ProgressMonitor();
        pMonitor.setConsoleMode(true);

        File geneFile = new File(args[0]);
        System.out.println("Reading data from:  " + geneFile.getAbsolutePath());
        int numLines = FileUtil.getNumLines(geneFile);
        System.out.println(" --> total number of lines:  " + numLines);
        pMonitor.setMaxValue(numLines);
        ImportMicroRNAIDs parser = new ImportMicroRNAIDs(geneFile, pMonitor);
        parser.importData();
        ConsoleUtil.showWarnings(pMonitor);
        System.err.println("Done.");
    }
}
