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

import static com.google.common.base.Preconditions.checkNotNull;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.methods.GetMethod;
import org.mskcc.cbio.cgds.dao.DaoException;
import org.mskcc.cbio.cgds.dao.DaoUniProtIdMapping;
import org.mskcc.cbio.cgds.util.ConsoleUtil;
import org.mskcc.cbio.cgds.util.FileUtil;
import org.mskcc.cbio.cgds.util.ProgressMonitor;
import org.mskcc.cbio.portal.remote.ConnectionManager;

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
        Set<String> swissProtAccs = getSwissProtAccessionHuman();
        int rows = 0;
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(uniProtIdMapping));
            Map<Integer,Set<String>> mapEntrezSwissProt = new HashMap<Integer,Set<String>>();
            Map<Integer,Set<String>> mapEntrezUniprot = new HashMap<Integer,Set<String>>();
            while (reader.ready()) {
                String line = reader.readLine();
                String[] tokens = line.split("\t");
                int entrezGeneId = Integer.parseInt(tokens[0]);
                String uniProtId = tokens[1];
                if (swissProtAccs.contains(uniProtId)) {
                    Set<String> swiss = mapEntrezSwissProt.get(entrezGeneId);
                    if (swiss==null) {
                        swiss = new HashSet<String>();
                        mapEntrezSwissProt.put(entrezGeneId, swiss);
                    }
                    swiss.add(uniProtId);
                } else {
                    Set<String> uniprot = mapEntrezUniprot.get(entrezGeneId);
                    if (uniprot==null) {
                        uniprot = new HashSet<String>();
                        mapEntrezUniprot.put(entrezGeneId, uniprot);
                    }
                    uniprot.add(uniProtId);
                }
                progressMonitor.incrementCurValue();
                ConsoleUtil.showProgress(progressMonitor);
            }
            mapEntrezUniprot.keySet().removeAll(mapEntrezSwissProt.entrySet());
            mapEntrezUniprot.putAll(mapEntrezSwissProt);
            for (Map.Entry<Integer,Set<String>> entry : mapEntrezUniprot.entrySet()) {
                int entrezGeneId = entry.getKey();
                String uniprot = pickOneUniprot(entry.getValue());
                if (uniprot != null) {
                    rows += DaoUniProtIdMapping.addUniProtIdMapping(entrezGeneId, uniprot);
                }
            }
            System.out.println("Total number of uniprot id mappings saved: " + rows);
        }
        finally {
            try {
                reader.close();
            }
            catch (Exception e) {
                // ignore
            }
        }
    }
    
    private String pickOneUniprot(Set<String> uniprotIds) throws IOException {
        if (uniprotIds.size()==1) {
            return uniprotIds.iterator().next();
        }
        
        int maxLength = 0;
        String ret = null;
        for (String id : uniprotIds) {
            int len = getLengthOfUniprotEntry(id);
            if (len > maxLength) {
                ret = id;
                maxLength = len;
            }
        }
        
        return ret;
    }
    
    private int getLengthOfUniprotEntry(String uniprotId) throws IOException {
        String strURL = "http://www.uniprot.org/uniprot/"+uniprotId+".fasta";
        MultiThreadedHttpConnectionManager connectionManager =
                ConnectionManager.getConnectionManager();
        HttpClient client = new HttpClient(connectionManager);
        GetMethod method = new GetMethod(strURL);
        
        try {
            int statusCode = client.executeMethod(method);
            if (statusCode == HttpStatus.SC_OK) {
                BufferedReader bufReader = new BufferedReader(
                        new InputStreamReader(method.getResponseBodyAsStream()));
                String line = bufReader.readLine();
                if (!line.startsWith(">")) {
                    return 0;
                }
                
                int len = 0;
                for (line=bufReader.readLine(); line!=null; line=bufReader.readLine()) {
                    len += line.length();
                }
                return len;
            } else {
                //  Otherwise, throw HTTP Exception Object
                throw new HttpException(statusCode + ": " + HttpStatus.getStatusText(statusCode)
                        + " Base URL:  " + strURL);
            }
        } finally {
            //  Must release connection back to Apache Commons Connection Pool
            method.releaseConnection();
        }
    }
    
    private Set<String> getSwissProtAccessionHuman() throws IOException {
        String strURL = "http://www.uniprot.org/uniprot/?query="
                + "taxonomy%3ahuman+AND+reviewed%3ayes&force=yes&format=list";
        
        MultiThreadedHttpConnectionManager connectionManager =
                ConnectionManager.getConnectionManager();
        HttpClient client = new HttpClient(connectionManager);
        GetMethod method = new GetMethod(strURL);
        
        try {
            int statusCode = client.executeMethod(method);
            if (statusCode == HttpStatus.SC_OK) {
                BufferedReader bufReader = new BufferedReader(
                        new InputStreamReader(method.getResponseBodyAsStream()));
                Set<String> accs = new HashSet<String>();
                for (String line=bufReader.readLine(); line!=null; line=bufReader.readLine()) {
                    accs.add(line);
                }
                return accs;
            } else {
                //  Otherwise, throw HTTP Exception Object
                throw new HttpException(statusCode + ": " + HttpStatus.getStatusText(statusCode)
                        + " Base URL:  " + strURL);
            }
        } finally {
            //  Must release connection back to Apache Commons Connection Pool
            method.releaseConnection();
        }
    }

    public static void main(final String[] args) {
        if (args.length < 1) {
            System.out.println("command line usage: importUniProtIdMapping.pl <uniprot_id_mapping.txt>");
            System.exit(1);
        }
        ProgressMonitor progressMonitor = new ProgressMonitor();
        progressMonitor.setConsoleMode(true);
        try {
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
