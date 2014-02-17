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

package org.mskcc.cbio.portal.scripts;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import org.mskcc.cbio.portal.dao.DaoPdbUniprotAlignment;
import org.mskcc.cbio.portal.dao.DaoException;
import org.mskcc.cbio.portal.dao.MySQLbulkLoader;
import org.mskcc.cbio.portal.model.PdbUniprotAlignment;
import org.mskcc.cbio.portal.util.ConsoleUtil;
import org.mskcc.cbio.portal.util.FileUtil;
import org.mskcc.cbio.portal.util.ProgressMonitor;

/**
 *
 * @author jgao
 */
public final class ImportPdbUniprotResidueMapping {
    private ImportPdbUniprotResidueMapping() {}

    /**
     * 
     *
     * @param mappingFile pdb-uniprot-residue-mapping.txt.
     * @param pMonitor Progress Monitor.
     */
    public static void importData(File mappingFile, ProgressMonitor pMonitor) throws DaoException, IOException {
        MySQLbulkLoader.bulkLoadOn();
        FileReader reader = new FileReader(mappingFile);
        BufferedReader buf = new BufferedReader(reader);
        String line = buf.readLine();
        while(line.startsWith("#")) {
            line = buf.readLine();
        }
        
        buf.readLine(); // skip the head line
        
        int alignId = 0;
        
        while (line != null) {
            pMonitor.incrementCurValue();
            ConsoleUtil.showProgress(pMonitor);
            
            line = buf.readLine();
            String[] parts = line.split("\t");
            
            PdbUniprotAlignment pdbUniprotAlignment = new PdbUniprotAlignment();
            pdbUniprotAlignment.setAlignmentId(++alignId);
            pdbUniprotAlignment.setPdbId(parts[0]);
            pdbUniprotAlignment.setChain(parts[1]);
            pdbUniprotAlignment.setUniprotAcc(parts[2]);
            pdbUniprotAlignment.setPdbFrom(Integer.parseInt(parts[5]));
            pdbUniprotAlignment.setPdbTo(Integer.parseInt(parts[6]));
            pdbUniprotAlignment.setUniprotFrom(Integer.parseInt(parts[7]));
            pdbUniprotAlignment.setUniprotTo(Integer.parseInt(parts[8]));
            
            DaoPdbUniprotAlignment.addPdbUniprotAlignment(pdbUniprotAlignment);
        }

        //  Flush database
        if (MySQLbulkLoader.isBulkLoad()) {
           MySQLbulkLoader.flushAll();
        }
    }
    
    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("command line usage:  importPdbUniprotResidueMapping.pl <pdb-chain-uniprot.tsv>");
            return;
        }
    
        ProgressMonitor pMonitor = new ProgressMonitor();
        pMonitor.setConsoleMode(true);

        try {
            DaoPdbUniprotAlignment.deleteAllRecords();
            
            File file = new File(args[0]);
            System.out.println("Reading PDB-UniProt residue mapping from:  " + file.getAbsolutePath());
            int numLines = FileUtil.getNumLines(file);
            System.out.println(" --> total number of lines:  " + numLines);
            pMonitor.setMaxValue(numLines);
            importData(file, pMonitor);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (DaoException e) {
            e.printStackTrace();
        } finally {
            ConsoleUtil.showWarnings(pMonitor);
            System.err.println("Done.");
        }
    }
}
