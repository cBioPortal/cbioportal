/*
 * Copyright (c) 2015 Memorial Sloan-Kettering Cancer Center.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF MERCHANTABILITY OR FITNESS
 * FOR A PARTICULAR PURPOSE. The software and documentation provided hereunder
 * is on an "as is" basis, and Memorial Sloan-Kettering Cancer Center has no
 * obligations to provide maintenance, support, updates, enhancements or
 * modifications. In no event shall Memorial Sloan-Kettering Cancer Center be
 * liable to any party for direct, indirect, special, incidental or
 * consequential damages, including lost profits, arising out of the use of this
 * software and its documentation, even if Memorial Sloan-Kettering Cancer
 * Center has been advised of the possibility of such damage.
 */

/*
 * This file is part of cBioPortal.
 *
 * cBioPortal is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.mskcc.cbio.portal.scripts;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.*;
import java.net.*;
import java.util.*;
import org.apache.commons.httpclient.*;
import org.apache.commons.httpclient.methods.GetMethod;
import org.mskcc.cbio.portal.dao.*;
import org.mskcc.cbio.portal.util.*;
import org.mskcc.cbio.portal.web_api.ConnectionManager;

/**
 * Import data into the uniprot_id_mapping table.
 */
public final class ImportUniProtIdMapping {
    private final File uniProtIdMapping;

    public ImportUniProtIdMapping(final File uniProtIdMapping) {
        checkNotNull(uniProtIdMapping, "uniProtIdMapping must not be null");
        this.uniProtIdMapping = uniProtIdMapping;
    }

    public void importData() throws DaoException, IOException {
        Set<String> swissAccessions = null;
        String species = GlobalProperties.getSpecies();
        if (!(species.equals("human") || species.equals("mouse"))) {
            throw new Error("Species not supported: " + species);
        }
        swissAccessions = ImportUniProtIdMapping.getSwissProtAccession(species);

        MySQLbulkLoader.bulkLoadOn();

        BufferedReader reader = new BufferedReader(
            new FileReader(uniProtIdMapping)
        );

        Map<String, Integer> mapUniprotAccEntrezGeneId = new HashMap<String, Integer>();
        Map<String, String> mapUniprotAccUniprotId = new HashMap<String, String>();
        for (
            String line = reader.readLine();
            line != null;
            line = reader.readLine()
        ) {
            ProgressMonitor.incrementCurValue();
            ConsoleUtil.showProgress();

            String[] parts = line.split("\t");
            if (!swissAccessions.contains(parts[0])) {
                continue;
            }

            if (parts[1].equals("GeneID")) {
                mapUniprotAccEntrezGeneId.put(
                    parts[0],
                    Integer.valueOf(parts[2])
                );
            } else if (parts[1].equals("UniProtKB-ID")) {
                mapUniprotAccUniprotId.put(parts[0], parts[2]);
            } else {
                System.err.println("Wong mapping: " + line);
            }
        }

        reader.close();

        for (Map.Entry<String, String> entry : mapUniprotAccUniprotId.entrySet()) {
            String uniprotAcc = entry.getKey();
            String uniprotId = entry.getValue();
            Integer entrezGeneId = mapUniprotAccEntrezGeneId.get(uniprotAcc);
            DaoUniProtIdMapping.addUniProtIdMapping(
                uniprotAcc,
                uniprotId,
                entrezGeneId
            );
        }

        MySQLbulkLoader.flushAll();
    }

    public static Set<String> getSwissProtAccession(String species)
        throws IOException {
        String strURL = null;
        if (species.equals("human")) {
            strURL =
                "http://www.uniprot.org/uniprot/?query=" +
                "taxonomy%3ahuman+AND+reviewed%3ayes&force=yes&format=list";
        } else if (species.equals("mouse")) {
            strURL =
                "http://www.uniprot.org/uniprot/?query=" +
                "taxonomy%3amouse+AND+reviewed%3ayes&force=yes&format=list";
        } else {
            throw new Error("Species not supported:" + species);
        }

        URL url = new URL(strURL);

        URLConnection pfamConn = url.openConnection();

        BufferedReader in = new BufferedReader(
            new InputStreamReader(pfamConn.getInputStream())
        );

        String line;
        Set<String> accs = new HashSet<String>();

        // read all
        while ((line = in.readLine()) != null) {
            accs.add(line);
        }

        in.close();

        return accs;
    }

    public static void main(final String[] args) {
        if (args.length < 1) {
            System.out.println(
                "command line usage: importUniProtIdMapping.pl <uniprot_id_mapping.txt>"
            );
            return;
        }
        ProgressMonitor.setConsoleMode(true);
        SpringUtil.initDataSource();
        try {
            DaoUniProtIdMapping.deleteAllRecords();
            File uniProtIdMapping = new File(args[0]);
            System.out.println(
                "Reading uniprot id mappings from:  " +
                uniProtIdMapping.getAbsolutePath()
            );
            int lines = FileUtil.getNumLines(uniProtIdMapping);
            System.out.println(" --> total number of lines:  " + lines);
            ProgressMonitor.setMaxValue(lines);
            ImportUniProtIdMapping importUniProtIdMapping = new ImportUniProtIdMapping(
                uniProtIdMapping
            );
            importUniProtIdMapping.importData();
        } catch (DaoException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            ConsoleUtil.showWarnings();
            System.err.println("Done.");
        }
    }
}
