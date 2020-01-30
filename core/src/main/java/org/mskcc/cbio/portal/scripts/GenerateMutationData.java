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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.mskcc.cbio.portal.dao.DaoException;
import org.mskcc.cbio.portal.dao.DaoGeneOptimized;
import org.mskcc.cbio.portal.model.CanonicalGene;
import org.mskcc.cbio.portal.util.ConsoleUtil;
import org.mskcc.cbio.portal.util.ProgressMonitor;

/**
 * Class to Generate Mutation Data, Ready for Import into CGDS.
 *
 * To work correctly, this script requires four data files:
 *
 * 1)  a list of all cases.
 * 2)  a list of all sequenced genes.
 * 3)  a list of all cases, which were sequenced.
 * 4)  a list of mutations found.
 *
 * This script primarily handles the following functions:
 * 1)  outputs negative results;  for example, if gene X is sequenced in case Y we report this as
 * a zero.
 * 2)  multiple mutations within the same gene in the same case are output as one line.
 */
public class GenerateMutationData {
    public static final String MAP_KEY_DELIMETER = ":::";
    private File allCasesFile;
    private File sequencedGeneFile;
    private File sequencedCaseFile;
    private File knownMutationFile;

    /**
     * Constructor.
     * @param allCasesFile          File containing all cases in project.
     * @param sequencedGeneFile     File containing all sequenced genes.
     * @param sequencedCaseFile     File containing all cases, which were sequenced.
     * @param knownMutationFile     File containing discovered mutations.
     */
    public GenerateMutationData(
        File allCasesFile,
        File sequencedGeneFile,
        File sequencedCaseFile,
        File knownMutationFile
    ) {
        this.allCasesFile = allCasesFile;
        this.sequencedGeneFile = sequencedGeneFile;
        this.sequencedCaseFile = sequencedCaseFile;
        this.knownMutationFile = knownMutationFile;
    }

    /**
     * Executes the process.
     * @throws IOException IO Error.
     */
    public String execute() throws IOException {
        List<String> allCasesList = getList(allCasesFile);
        List<String> sequencedGeneList = getList(sequencedGeneFile);
        List<String> sequencedCaseList = getList(sequencedCaseFile);

        // create mutation map
        Map<String, String> mutationsMap = new HashMap<String, String>();

        // overlay no mutation on top of mutation map
        applyNoMutationData(sequencedGeneList, sequencedCaseList, mutationsMap);

        // overlay known mutations on top of mutation map
        applyKnownMutationsData(knownMutationFile, mutationsMap);

        // generate mutation file for import
        return generateMutationFile(mutationsMap);
    }

    /**
     * Extracts a list of "things", e.g. caseIds or genes from a file.
     *
     * @param file File containing cases.
     * @return Arrayist <String> of "things", e.g. caseIds or genes.
     */
    private ArrayList<String> getList(File file) throws IOException {
        // list to return
        ArrayList<String> toReturn = new ArrayList<String>();

        FileReader reader = new FileReader(file);
        BufferedReader buf = new BufferedReader(reader);

        String line = buf.readLine();
        while (line != null) {
            if (line.trim().length() > 0 && !line.startsWith("#")) {
                toReturn.add(line.trim());
            }
            line = buf.readLine();
        }

        // outta here
        buf.close();
        return toReturn;
    }

    /**
     * Returns hash map of all gene, case combinations set to NaN.
     *
     * @param allCasesList List
     * @return Map<String, String>
     */
    private Map<String, String> getMutationMap(List<String> allCasesList)
        throws DaoException, IOException {
        // map to return
        Map<String, String> toReturn = new HashMap<String, String>();

        DaoGeneOptimized daoGene = DaoGeneOptimized.getInstance();
        ArrayList<CanonicalGene> geneList = daoGene.getAllGenes();

        for (CanonicalGene canonicalGene : geneList) {
            for (String caseId : allCasesList) {
                toReturn.put(
                    Long.toString(canonicalGene.getEntrezGeneId()) +
                    GenerateMutationData.MAP_KEY_DELIMETER +
                    caseId,
                    "NaN"
                );
            }
        }

        // outta here
        return toReturn;
    }

    /**
     * Applies no mutation data to mutation map.
     *
     * @param sequencedGeneList List<String>
     * @param sequencedCaseList List<String>
     * @param mutationsMap Map<String,String>
     */
    private void applyNoMutationData(
        List<String> sequencedGeneList,
        List<String> sequencedCaseList,
        Map<String, String> mutationsMap
    ) {
        for (String sequencedGene : sequencedGeneList) {
            for (String sequencedCase : sequencedCaseList) {
                String key = createKey(sequencedGene, sequencedCase);
                mutationsMap.put(key, "0");
            }
        }
    }

    private String createKey(String sequencedGene, String sequencedCase) {
        if (
            sequencedGene == null ||
            sequencedCase == null ||
            sequencedGene.length() == 0 ||
            sequencedCase.length() == 0
        ) {
            throw new IllegalArgumentException(
                "One or more parameters is null or empty."
            );
        }
        String key =
            sequencedGene +
            GenerateMutationData.MAP_KEY_DELIMETER +
            sequencedCase;
        return key;
    }

    /**
     * Applies know mutations to mutation map
     *
     * @param knownMutationsFile File
     * @param mutationsMap Map<String,String>
     */
    private void applyKnownMutationsData(
        File knownMutationsFile,
        Map<String, String> mutationsMap
    )
        throws IOException {
        // setup reader
        FileReader reader = new FileReader(knownMutationsFile);
        BufferedReader buf = new BufferedReader(reader);

        // read header
        String line = buf.readLine();
        if (!line.startsWith("Entrez_Gene_Id")) {
            ProgressMonitor.logWarning(
                "Missing header in: " +
                knownMutationsFile.getCanonicalPath() +
                " aborting mutation file import..."
            );
            return;
        }

        // concatentate all know mutations by gene/case
        Map<String, String> knownMutationsMap = new HashMap<String, String>();
        line = buf.readLine();
        while (line != null) {
            if (!line.startsWith("#")) {
                String[] parts = line.split("\t");
                String key = createKey(parts[0], parts[1]);
                String mutation = knownMutationsMap.get(key);
                mutation =
                    (mutation == null) ? parts[2] : mutation + "," + parts[2];
                knownMutationsMap.put(key, mutation);
            }
            line = buf.readLine();
        }
        buf.close();

        // apply known mutations to mutations map
        Set<String> keys = knownMutationsMap.keySet();
        for (String key : keys) {
            if (!mutationsMap.containsKey(key)) {
                String[] parts = key.split(":::");
                ProgressMonitor.logWarning(
                    "Missing gene/case combination: " +
                    parts[0] +
                    " " +
                    parts[1] +
                    " in mutationMap.  In other words, it looks like we have a mutation " +
                    "call for this gene/case combination, but the case is not listed as being " +
                    " sequenced in the first place!"
                );
                continue;
            }
            mutationsMap.put(key, knownMutationsMap.get(key));
        }
    }

    /**
     * Generates mutation file for import
     *
     * @param mutationsMap Map<String,String>
     */
    private String generateMutationFile(Map<String, String> mutationsMap)
        throws IOException {
        // write header
        StringBuffer out = new StringBuffer();
        out.append("Entrez_Gene_Id\tTumor_Case\tPROT_STRING\n");

        // write data
        Set<String> keys = mutationsMap.keySet();
        for (String key : keys) {
            String[] parts = key.split(GenerateMutationData.MAP_KEY_DELIMETER);
            out.append(
                parts[0] + "\t" + parts[1] + "\t" + mutationsMap.get(key) + "\n"
            );
        }
        return out.toString();
    }

    public static void main(String[] args) throws Exception {
        // check args
        if (args.length < 4) {
            System.out.println(
                "command line usage:  generateMutationData.pl " +
                "<case-list> <sequenced-gene-list> <sequenced-cases> <known-mutation-file>"
            );
            // an extra --noprogress option can be given to avoid the messages regarding memory usage and % complete
            return;
        }

        // setup some vars
        ProgressMonitor.setConsoleModeAndParseShowProgress(args);
        ProgressMonitor.setCurrentMessage("Generating mutation data file...");
        File allCasesFile = new File(args[0]);
        File sequencedGeneFile = new File(args[1]);
        File sequencedCaseFile = new File(args[2]);
        File knownMutationFile = new File(args[3]);

        try {
            // construct our lists
            GenerateMutationData util = new GenerateMutationData(
                allCasesFile,
                sequencedGeneFile,
                sequencedCaseFile,
                knownMutationFile
            );
            String out = util.execute();
            System.out.println(out);
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }

        ConsoleUtil.showWarnings();
        System.err.println("Done.");
    }
}
