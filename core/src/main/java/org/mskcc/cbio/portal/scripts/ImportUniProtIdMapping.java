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

import static com.google.common.base.Preconditions.checkNotNull;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.mskcc.cbio.portal.dao.DaoException;
import org.mskcc.cbio.portal.dao.DaoUniProtIdMapping;
import org.mskcc.cbio.portal.dao.MySQLbulkLoader;
import org.mskcc.cbio.portal.util.ConsoleUtil;
import org.mskcc.cbio.portal.util.FileUtil;
import org.mskcc.cbio.portal.util.ProgressMonitor;

/**
 * Import data into the uniprot_id_mapping table.
 */
public final class ImportUniProtIdMapping {
    private final File uniProtIdMapping;
    private final ProgressMonitor progressMonitor;

    public ImportUniProtIdMapping(final File uniProtIdMapping, final ProgressMonitor progressMonitor) {
        checkNotNull(uniProtIdMapping, "uniProtIdMapping must not be null");
        checkNotNull(progressMonitor, "progressMonitor must not be null");
        this.uniProtIdMapping = uniProtIdMapping;
        this.progressMonitor = progressMonitor;
    }

    public void importData() throws DaoException, IOException {
        MySQLbulkLoader.bulkLoadOn();
        
        BufferedReader reader = new BufferedReader(new FileReader(uniProtIdMapping));
        
        Map<String, Integer> mapUniprotAccEntrezGeneId = new HashMap<String, Integer>();
        Map<String, String> mapUniprotAccUniprotId = new HashMap<String, String>();
        for (String line = reader.readLine(); line != null; line = reader.readLine()) {
            String[] parts = line.split("\t");
            if (parts[1].equals("GeneID")) {
                mapUniprotAccEntrezGeneId.put(parts[0], Integer.valueOf(parts[2]));
            } else if (parts[1].equals("UniProtKB-ID")) {
                mapUniprotAccUniprotId.put(parts[0], parts[2]);
            } else {
                System.err.println("Wong mapping: "+line);
            }
            
            progressMonitor.incrementCurValue();
            ConsoleUtil.showProgress(progressMonitor);
        }
        
        reader.close();
        
        for (Map.Entry<String, String> entry : mapUniprotAccUniprotId.entrySet()) {
            String uniprotAcc = entry.getKey();
            String uniprotId = entry.getValue();
            Integer entrezGeneId = mapUniprotAccEntrezGeneId.get(uniprotAcc);
            DaoUniProtIdMapping.addUniProtIdMapping(uniprotAcc, uniprotId, entrezGeneId);
        }
        
        MySQLbulkLoader.flushAll();
    }

    public static void main(final String[] args) {
        if (args.length < 1) {
            System.out.println("command line usage: importUniProtIdMapping.pl <uniprot_id_mapping.txt>");
            return;
        }
        ProgressMonitor progressMonitor = new ProgressMonitor();
        progressMonitor.setConsoleMode(true);
        try {
            DaoUniProtIdMapping.deleteAllRecords();
            File uniProtIdMapping = new File(args[0]);
            System.out.println("Reading uniprot id mappings from:  " + uniProtIdMapping.getAbsolutePath());
            int lines = FileUtil.getNumLines(uniProtIdMapping);
            System.out.println(" --> total number of lines:  " + lines);
            progressMonitor.setMaxValue(lines);
            ImportUniProtIdMapping importUniProtIdMapping = new ImportUniProtIdMapping(uniProtIdMapping, progressMonitor);
            importUniProtIdMapping.importData();
        }
        catch (DaoException e) {
            e.printStackTrace();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        finally {
            ConsoleUtil.showWarnings(progressMonitor);
            System.err.println("Done.");
        }
    }
}
