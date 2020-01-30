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
import java.util.*;
import org.mskcc.cbio.portal.dao.*;
import org.mskcc.cbio.portal.model.*;
import org.mskcc.cbio.portal.util.*;

/**
 *
 * @author jgao
 */
public final class ImportPdbUniprotResidueMappingFromMA {

    private ImportPdbUniprotResidueMappingFromMA() {}

    /**
     *
     *
     * @param mappingFile pdb-uniprot-residue-mapping.txt.
     */
    public static void importMutationAssessorData(
        File mappingFile,
        double identpThrehold
    )
        throws DaoException, IOException {
        MySQLbulkLoader.bulkLoadOn();
        FileReader reader = new FileReader(mappingFile);
        BufferedReader buf = new BufferedReader(reader);
        String line = buf.readLine();
        int alignId = DaoPdbUniprotResidueMapping.getLargestAlignmentId();
        PdbUniprotAlignment pdbUniprotAlignment = new PdbUniprotAlignment();
        List<PdbUniprotResidueMapping> pdbUniprotResidueMappings = Collections.emptyList();
        Map<Integer, Integer> mappingUniPdbProtein = Collections.emptyMap();
        Map<Integer, Integer> mappingUniPdbAlignment = Collections.emptyMap();
        Map<Integer, Integer> mappingPdbUniProtein = Collections.emptyMap();
        Map<Integer, Integer> mappingPdbUniAlignment = Collections.emptyMap();

        while (line != null) {
            if (!line.startsWith("#")) {
                String parts[] = line.split("\t", -1);
                if (line.startsWith(">")) {
                    // alignment line, eg. >1a37   A       1433B_HUMAN     1       32      3       34      0.000000        29.000000       90.625000       MDKSELVQKAKLAEQAERYDDMAAAMKAVTEQ        MDKNELVQKAKLAEQAERYDDMAACMKSVTEQ        MDK+ELVQKAKLAEQAERYDDMAA MK+VTEQ

                    if (!pdbUniprotResidueMappings.isEmpty()) {
                        if (
                            pdbUniprotAlignment.getIdentityPerc() >=
                            identpThrehold
                        ) {
                            DaoPdbUniprotResidueMapping.addPdbUniprotAlignment(
                                pdbUniprotAlignment
                            );
                            for (PdbUniprotResidueMapping mapping : pdbUniprotResidueMappings) {
                                DaoPdbUniprotResidueMapping.addPdbUniprotResidueMapping(
                                    mapping
                                );
                            }
                            mappingUniPdbProtein.putAll(mappingUniPdbAlignment);
                            mappingPdbUniProtein.putAll(mappingPdbUniAlignment);
                        }
                    }

                    String pdbId = parts[0].substring(1);
                    if (
                        !pdbId.equals(pdbUniprotAlignment.getPdbId()) ||
                        !parts[1].equals(pdbUniprotAlignment.getChain()) ||
                        !parts[2].equals(pdbUniprotAlignment.getUniprotId())
                    ) {
                        mappingUniPdbProtein = new HashMap<Integer, Integer>();
                        mappingPdbUniProtein = new HashMap<Integer, Integer>();
                    }

                    pdbUniprotAlignment.setAlignmentId(++alignId);

                    pdbUniprotAlignment.setPdbId(pdbId);
                    pdbUniprotAlignment.setChain(parts[1]);
                    pdbUniprotAlignment.setUniprotId(parts[2]);

                    pdbUniprotAlignment.setPdbFrom(parts[3]);
                    pdbUniprotAlignment.setPdbTo(parts[4]);
                    pdbUniprotAlignment.setUniprotFrom(
                        Integer.parseInt(parts[5])
                    );
                    pdbUniprotAlignment.setUniprotTo(
                        Integer.parseInt(parts[6])
                    );
                    pdbUniprotAlignment.setEValue(Float.parseFloat(parts[7]));
                    pdbUniprotAlignment.setIdentity(Float.parseFloat(parts[8]));
                    pdbUniprotAlignment.setIdentityPerc(
                        Float.parseFloat(parts[9])
                    );
                    pdbUniprotAlignment.setUniprotAlign(parts[10]);
                    pdbUniprotAlignment.setPdbAlign(parts[11]);
                    pdbUniprotAlignment.setMidlineAlign(parts[12]);

                    pdbUniprotResidueMappings =
                        new ArrayList<PdbUniprotResidueMapping>();
                    mappingUniPdbAlignment = new HashMap<Integer, Integer>();
                    mappingPdbUniAlignment = new HashMap<Integer, Integer>();
                } else {
                    // residue mapping line, e.g. 1a37    A       M1      1433B_HUMAN     M3      M
                    int pdbPos = Integer.parseInt(parts[2].substring(1));
                    int uniprotPos = Integer.parseInt(parts[4].substring(1));
                    Integer prePdb = mappingUniPdbProtein.get(uniprotPos);
                    Integer preUni = mappingPdbUniProtein.get(pdbPos);
                    if (
                        (prePdb != null && prePdb != pdbPos) ||
                        (preUni != null && preUni != uniprotPos)
                    ) {
                        // mismatch
                        pdbUniprotResidueMappings.clear();
                        while (line != null && !line.startsWith(">")) {
                            line = buf.readLine();
                            ProgressMonitor.incrementCurValue();
                            ConsoleUtil.showProgress();
                        }
                        continue;
                    }

                    mappingUniPdbAlignment.put(uniprotPos, pdbPos);
                    mappingPdbUniAlignment.put(pdbPos, uniprotPos);

                    String match = parts[5].length() == 0 ? " " : parts[5];
                    PdbUniprotResidueMapping pdbUniprotResidueMapping = new PdbUniprotResidueMapping(
                        alignId,
                        pdbPos,
                        null,
                        uniprotPos,
                        match
                    );
                    pdbUniprotResidueMappings.add(pdbUniprotResidueMapping);
                }
            }

            line = buf.readLine();

            ProgressMonitor.incrementCurValue();
            ConsoleUtil.showProgress();
        }

        // last one
        if (!pdbUniprotResidueMappings.isEmpty()) {
            if (pdbUniprotAlignment.getIdentityPerc() >= identpThrehold) {
                DaoPdbUniprotResidueMapping.addPdbUniprotAlignment(
                    pdbUniprotAlignment
                );
                for (PdbUniprotResidueMapping mapping : pdbUniprotResidueMappings) {
                    DaoPdbUniprotResidueMapping.addPdbUniprotResidueMapping(
                        mapping
                    );
                }
            }
        }

        //  Flush database
        if (MySQLbulkLoader.isBulkLoad()) {
            MySQLbulkLoader.flushAll();
        }
    }

    public static void main(String[] args) throws Exception {
        if (args.length < 1) {
            System.out.println(
                "command line usage: importPdbUniprotResidueMappingMA.pl pdb-uniprot-residue-mapping.txt"
            );
            return;
        }

        ProgressMonitor.setConsoleMode(true);

        SpringUtil.initDataSource();

        double identpThrehold = 50;
        try {
            File file = new File(args[0]);
            System.out.println(
                "Reading PDB-UniProt residue mapping from:  " +
                file.getAbsolutePath()
            );
            int numLines = FileUtil.getNumLines(file);
            System.out.println(" --> total number of lines:  " + numLines);
            ProgressMonitor.setMaxValue(numLines);
            importMutationAssessorData(file, identpThrehold);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (DaoException e) {
            e.printStackTrace();
        } finally {
            ConsoleUtil.showWarnings();
            System.err.println("Done.");
        }
    }
}
