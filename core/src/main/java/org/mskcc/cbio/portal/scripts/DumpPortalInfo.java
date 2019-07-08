/*
 * Copyright (c) 2016 The Hyve B.V.
 *
 * This code is licensed under the GNU Affero General Public License (AGPL),
 * version 3, or (at your option) any later version.
 *
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

import java.util.List;
import java.io.Serializable;
import java.io.File;
import java.io.IOException;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.JsonProcessingException;

import org.mskcc.cbio.portal.util.SpringUtil;
import org.mskcc.cbio.portal.util.ProgressMonitor;
import org.mskcc.cbio.portal.service.ApiService;
import org.cbioportal.service.GenesetService;
import org.cbioportal.service.GenePanelService;

/**
 * Command line tool to generate JSON files used by the validation script.
 */
public class DumpPortalInfo extends ConsoleRunnable {

    // these names are defined in annotations to the methods of ApiController,
    // in org.mskcc.cbio.portal.web
    private static final String API_CANCER_TYPES = "/cancertypes";
    private static final String API_GENES = "/genes";
    private static final String API_GENE_ALIASES = "/genesaliases";
    private static final String API_GENESETS = "/genesets";
    private static final String API_GENE_PANELS = "/gene-panels";
    private static final int MAX_PAGE_SIZE = 10000000;
    private static final int MIN_PAGE_NUMBER = 0;

    private static File nameJsonFile(File dirName, String apiName) {
        // Determine the first alphabetic character
        int i;
        for (
                i = 0;
                !Character.isLetter(apiName.charAt(i));
                i++) {}
        // make a string without the initial non-alphanumeric characters
        String fileName = apiName.substring(i).replace('/', '_') + ".json";
        return new File(dirName, fileName);
    }

    private static void writeJsonFile(
            List<? extends Serializable> objectList,
            File outputFile) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
            try {
                mapper.writeValue(outputFile, objectList);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(
                        "Error converting API data to JSON file: " +
                                e.toString(),
                        e);
            }
    }

    public void run() {
        try {
            // check args
            if (args.length != 1 ||
                    args[0].equals("-h") || args[0].equals("--help")) {
                throw new UsageException(
                        "dumpPortalInfo.pl",
                        "Generate a folder of files describing the portal " +
                                "configuration.\n" +
                                "\n" +
                                "This is a subset of the information provided " +
                                "by the web API,\n" +
                                "intended for offline use of the validation " +
                                "script for study data.",
                        "<name for the output directory>");
            }
            String outputDirName = args[0];
            ProgressMonitor.setCurrentMessage(
                    "Writing portal info files to directory '" +
                    outputDirName + "'...\n");

            // initialize application context, including database connection
            SpringUtil.initDataSource();
            ApiService apiService = SpringUtil.getApplicationContext().getBean(
                    ApiService.class);
            GenesetService genesetService = SpringUtil.getApplicationContext().getBean(
                GenesetService.class);
            GenePanelService genePanelService = SpringUtil.getApplicationContext().getBean(
                GenePanelService.class);

            File outputDir = new File(outputDirName);
            // this will do nothing if the directory already exists:
            // the files will simply be overwritten
            outputDir.mkdir();
            if (!outputDir.isDirectory()) {
                throw new IOException(
                        "Could not create directory '" +
                        outputDir.getPath() + "'");
            }

            try {
                writeJsonFile(
                        apiService.getCancerTypes(),
                        nameJsonFile(outputDir, API_CANCER_TYPES));
                writeJsonFile(
                        apiService.getGenes(),
                        nameJsonFile(outputDir, API_GENES));
                writeJsonFile(
                        apiService.getGenesAliases(),
                        nameJsonFile(outputDir, API_GENE_ALIASES));
                writeJsonFile(
                    genesetService.getAllGenesets("SUMMARY", MAX_PAGE_SIZE, MIN_PAGE_NUMBER),
                    nameJsonFile(outputDir, API_GENESETS));
                writeJsonFile(
                    genePanelService.getAllGenePanels("SUMMARY", MAX_PAGE_SIZE, MIN_PAGE_NUMBER, null, "ASC"),
                    nameJsonFile(outputDir, API_GENE_PANELS));
            } catch (IOException e) {
                throw new IOException(
                        "Error writing portal info file: " + e.toString(),
                        e);
            }
        }
        catch (RuntimeException e) {
            throw e;
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Makes an instance to run with the given command line arguments.
     *
     * @param args  the command line arguments to be used
     */
    public DumpPortalInfo(String[] args) {
        super(args);
    }

    /**
     * Runs the command as a script and exits with an appropriate exit code.
     *
     * @param args  the arguments given on the command line
     */
    public static void main(String[] args) {
        ConsoleRunnable runner = new DumpPortalInfo(args);
        runner.runInConsole();
    }
}
