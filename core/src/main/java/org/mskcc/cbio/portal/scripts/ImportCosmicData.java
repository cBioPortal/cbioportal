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

import java.io.*;
import java.util.regex.*;
import org.mskcc.cbio.portal.dao.*;
import org.mskcc.cbio.portal.model.*;
import org.mskcc.cbio.portal.util.*;

public class ImportCosmicData {
    private File file;

    public ImportCosmicData(File file) {
        this.file = file;
    }

    public void importData() throws IOException, DaoException {
        DaoGeneOptimized daoGeneOptimized = DaoGeneOptimized.getInstance();
        Pattern p = Pattern.compile(
            "GENE=([^;]+);STRAND=(.);CDS=([^;]+);AA=p\\.([^;]+);CNT=([0-9]+)"
        );
        MySQLbulkLoader.bulkLoadOn();
        FileReader reader = new FileReader(file);
        BufferedReader buf = new BufferedReader(reader);
        String line;
        while ((line = buf.readLine()) != null) {
            ProgressMonitor.incrementCurValue();
            ConsoleUtil.showProgress();
            if (!line.startsWith("#")) {
                String parts[] = line.split("\t", -1);
                if (parts.length < 8) {
                    System.err.println("Wrong line in cosmic: " + line);
                    continue;
                }

                String id = parts[2];
                if (!id.matches("COSM[0-9]+")) {
                    System.err.println("Wrong cosmic ID: " + id);
                } else {
                    id = id.substring(4);
                }

                Matcher m = p.matcher(parts[7]);
                if (m.find()) {
                    String gene = m.group(1);
                    //                    if (gene.contains("_ENST")) {
                    //                        gene = gene.substring(0,gene.indexOf("_ENST"));
                    //                    }
                    //                    if (gene.contains("_HUMAN")) {
                    //                        gene = gene.substring(0,gene.indexOf("_HUMAN"));
                    //                    }
                    CanonicalGene canonicalGene = daoGeneOptimized.getNonAmbiguousGene(
                        gene,
                        true
                    );
                    if (canonicalGene == null) {
                        System.err.println(
                            "Gene symbol in COSMIC not recognized: " + gene
                        );
                        continue;
                    }

                    String aa = m.group(4);
                    String keyword = MutationKeywordUtils.guessCosmicKeyword(
                        aa
                    );
                    if (keyword == null) {
                        continue;
                    }

                    int count = Integer.parseInt(m.group(5));

                    CosmicMutationFrequency cmf = new CosmicMutationFrequency(
                        id,
                        canonicalGene.getEntrezGeneId(),
                        aa,
                        gene + " " + keyword,
                        count
                    );

                    cmf.setChr(parts[0]);
                    cmf.setStartPosition(Long.parseLong(parts[1]));
                    cmf.setReferenceAllele(parts[3]);
                    cmf.setTumorSeqAllele(parts[4]);
                    cmf.setStrand(m.group(2));
                    cmf.setCds(m.group(3));

                    DaoCosmicData.addCosmic(cmf);
                }
            }
        }
        buf.close();
        if (MySQLbulkLoader.isBulkLoad()) {
            MySQLbulkLoader.flushAll();
        }
    }

    public static void main(String[] args) throws Exception {
        if (args.length == 0) {
            System.out.println(
                "command line usage:  importCosmicData.pl <CosmicCodingMuts.vcf>"
            );
            return;
        }
        SpringUtil.initDataSource();
        DaoCosmicData.deleteAllRecords();
        ProgressMonitor.setConsoleMode(true);

        File file = new File(args[0]);
        System.out.println("Reading data from:  " + file.getAbsolutePath());
        int numLines = FileUtil.getNumLines(file);
        System.out.println(" --> total number of lines:  " + numLines);
        ProgressMonitor.setMaxValue(numLines);
        ImportCosmicData parser = new ImportCosmicData(file);
        parser.importData();
        ConsoleUtil.showWarnings();
        System.err.println("Done.");
    }
}
