package org.mskcc.cgds.scripts;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.io.IOException;

import java.net.URL;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.methods.GetMethod;

import org.mskcc.cgds.dao.DaoException;
import org.mskcc.cgds.dao.DaoUniProtIdMapping;
import org.mskcc.cgds.util.ConsoleUtil;
import org.mskcc.cgds.util.FileUtil;
import org.mskcc.cgds.util.ProgressMonitor;
import org.mskcc.portal.remote.ConnectionManager;

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
            while (reader.ready()) {
                String line = reader.readLine();
                String[] tokens = line.split("\t");
                String uniProtId = tokens[1];
                if (swissProtAccs.contains(uniProtId)) {
                    int entrezGeneId = Integer.parseInt(tokens[0]);
                    rows += DaoUniProtIdMapping.addUniProtIdMapping(entrezGeneId, uniProtId);
                }
                progressMonitor.incrementCurValue();
                ConsoleUtil.showProgress(progressMonitor);
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
