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

import org.mskcc.cbio.portal.dao.*;
import org.mskcc.cbio.portal.util.*;
import org.mskcc.cbio.portal.model.*;
import org.springframework.util.Assert;

import java.io.*;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.*;
import java.util.stream.IntStream;

public class ImportCosmicData {
    private File file;

    public ImportCosmicData(File file) {
        this.file = file;
    }

    private final static String geneEntryName = "GENE";
    private final static String strandEntryName = "STRAND";
    private final static String cdsEntryName = "CDS";
    private final static String aaEntryName = "AA";
    private final static String cntEntryName = "CNT";

    public void importData() throws IOException, DaoException {
        DaoGeneOptimized daoGeneOptimized = DaoGeneOptimized.getInstance();
        // Pattern: must have gene, strand, cds, aa and cnt in any order, with other strings possibly in between.
        Pattern p = Pattern.compile("(?=.*GENE=[^;]+.*)(?=.*STRAND=(.).*)(?=.*CDS=[^;]+.*)(?=.*AA=p\\.[^;]+.*)(?=.*CNT=[0-9]+.*)");
        Pattern id_pat = Pattern.compile("(?=.*LEGACY_ID=[^;]+.*)");
        MySQLbulkLoader.bulkLoadOn();
        FileReader reader = new FileReader(file);
        BufferedReader buf = new BufferedReader(reader);
        String line;
        while ((line = buf.readLine()) != null) {
            ProgressMonitor.incrementCurValue();
            ConsoleUtil.showProgress();
            if (!line.startsWith("#")) {
                String parts[] = line.split("\t",-1);
                if (parts.length<8) {
                    System.err.println("Wrong line in cosmic: "+line);
                    continue;
                }

                String id = parts[2];
                String infoColumnValue = parts[7];
                if (!id.matches("COS(M|V)[0-9]+")) {
                    System.err.println("Wrong cosmic ID: "+id);
                } else {
                    if (id.matches("COSM[0-9]+")) {  //COSM can be taken as is
                        id = id.substring(4);
                    } else {  //COSV does not map correctly, COSM still present in info-column
                        Matcher id_match = id_pat.matcher(infoColumnValue);
                        if (!id_match.find()) {
                            System.err.println("Cannot parse Legacy ID: "+line);
                        }
                        String id_items[] = infoColumnValue.split(";");
                        for (String s: id_items) {
                            if (s.startsWith("LEGACY_ID=")) {
                                id = s.substring(14);
                            }
                        }
                        
                    }
                }
                
                Matcher m = p.matcher(infoColumnValue);
                if (m.find()) {
                    Map<String, String> fieldValues = evaluateFieldValues(infoColumnValue);

                    String gene = fieldValues.get(geneEntryName);

                    if (gene.contains("_ENST")) {
                        gene = gene.substring(0,gene.indexOf("_ENST"));
                    }
//                    if (gene.contains("_HUMAN")) {
//                        gene = gene.substring(0,gene.indexOf("_HUMAN"));
//                    }
                    CanonicalGene canonicalGene = daoGeneOptimized.getNonAmbiguousGene(gene, true);
                    if (canonicalGene==null) {
                        System.err.println("Gene symbol in COSMIC not recognized: "+gene);
                        continue;
                    }

                    String aa = fieldValues.get(aaEntryName);
                    String keyword = MutationKeywordUtils.guessCosmicKeyword(aa);
                    if (keyword == null) {
                        System.out.println("Mutation keyword in COSMIC not recognized: "+aa);
                        continue;
                    }

                    String count_field = fieldValues.get(cntEntryName);
                    int count = Integer.parseInt(count_field);

                    CosmicMutationFrequency cmf = new CosmicMutationFrequency(id,
                        canonicalGene.getEntrezGeneId(), aa, gene + " " + keyword, count);

                    cmf.setChr(parts[0]);
                    cmf.setStartPosition(Long.parseLong(parts[1]));
                    cmf.setReferenceAllele(parts[3]);
                    cmf.setTumorSeqAllele(parts[4]);
                    cmf.setStrand(fieldValues.get(strandEntryName));
                    cmf.setCds(fieldValues.get(cdsEntryName));

                    DaoCosmicData.addCosmic(cmf);
                }
            }
        }
        buf.close();
        if (MySQLbulkLoader.isBulkLoad()) {
            MySQLbulkLoader.flushAll();
        }
    }

    private Map<String, String> evaluateFieldValues(String infoColumnValue) {
        String[] fields = infoColumnValue.split(";");
        HashMap<String, String> fieldValues = new HashMap<>();
        IntStream.range(0, fields.length).forEach(index -> {
            if (fields[index].startsWith("GENE="))
                fieldValues.put(geneEntryName, removePrefix(fields[index]));
            if (fields[index].startsWith("STRAND="))
                fieldValues.put(strandEntryName,  removePrefix(fields[index]));
            if (fields[index].startsWith("CDS="))
                fieldValues.put(cdsEntryName,  removePrefix(fields[index]));
            if (fields[index].startsWith("AA="))
                fieldValues.put(aaEntryName,  removePrefix(fields[index]));
            if (fields[index].startsWith("CNT="))
                fieldValues.put(cntEntryName,  removePrefix(fields[index]));
        });
        Assert.isTrue(fieldValues.keySet().size() == 5, "The value of one of the required fields could not be found.");
        return fieldValues;
    }

    private String removePrefix(String field) {
        String[] elements = field.split("=");
        String fieldName = elements[0];
        String fieldValue = elements[1];
        if (fieldName.equals("AA")) {
            fieldValue = fieldValue.replaceAll("^p\\.", "");
        };
        return fieldValue;
    }

    public static void main(String[] args) throws Exception {
        if (args.length == 0) {
            System.out.println("command line usage:  importCosmicData.pl <CosmicCodingMuts.vcf>");
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
