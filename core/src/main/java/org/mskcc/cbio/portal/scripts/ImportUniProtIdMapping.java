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

import static com.google.common.base.Preconditions.checkNotNull;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.methods.GetMethod;
import org.mskcc.cbio.portal.dao.DaoException;
import org.mskcc.cbio.portal.dao.DaoUniProtIdMapping;
import org.mskcc.cbio.portal.dao.MySQLbulkLoader;
import org.mskcc.cbio.portal.util.ConsoleUtil;
import org.mskcc.cbio.portal.util.FileUtil;
import org.mskcc.cbio.portal.util.ProgressMonitor;
import org.mskcc.cbio.portal.web_api.ConnectionManager;

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
        Set<String> swissAccessions = getSwissProtAccessionHuman();
        
        MySQLbulkLoader.bulkLoadOn();
        
        BufferedReader reader = new BufferedReader(new FileReader(uniProtIdMapping));
        
        Map<String, Integer> mapUniprotAccEntrezGeneId = new HashMap<String, Integer>();
        Map<String, String> mapUniprotAccUniprotId = new HashMap<String, String>();
        for (String line = reader.readLine(); line != null; line = reader.readLine()) {
            progressMonitor.incrementCurValue();
            ConsoleUtil.showProgress(progressMonitor);
            
            String[] parts = line.split("\t");
            if (!swissAccessions.contains(parts[0])) {
                continue;
            }
            
            if (parts[1].equals("GeneID")) {
                mapUniprotAccEntrezGeneId.put(parts[0], Integer.valueOf(parts[2]));
            } else if (parts[1].equals("UniProtKB-ID")) {
                mapUniprotAccUniprotId.put(parts[0], parts[2]);
            } else {
                System.err.println("Wong mapping: "+line);
            }
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
    
    public static Set<String> getSwissProtAccessionHuman() throws IOException {
        String strURL = "http://www.uniprot.org/uniprot/?query="
                + "taxonomy%3ahuman+AND+reviewed%3ayes&force=yes&format=list";
        
        URL url = new URL(strURL);

        URLConnection pfamConn = url.openConnection();

        BufferedReader in = new BufferedReader(
                        new InputStreamReader(pfamConn.getInputStream()));

        String line;
        Set<String> accs = new HashSet<String>();

        // read all
        while((line = in.readLine()) != null)
        {
                accs.add(line);
        }

        in.close();

	return accs;
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
