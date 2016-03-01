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
import org.mskcc.cbio.portal.util.ConsoleUtil;
import org.mskcc.cbio.portal.util.ProgressMonitor;
import org.mskcc.cbio.portal.service.ApiService;

/**
 * Command line tool to generate JSON files used by the validation script.
 */
public class DumpPortalInfo {

    // exit status codes for the script
    private static final int EX_USAGE = 64;
    private static final int EX_CANTCREAT = 73;
    private static final int EX_IOERR = 74;

    // these names are defined in annotations to the methods of ApiController,
    // in org.mskcc.cbio.portal.web
    private static final String API_CANCER_TYPES = "/cancertypes";
    private static final String API_SAMPLE_ATTRS = "/clinicalattributes/samples";
    private static final String API_PATIENT_ATTRS = "/clinicalattributes/patients";
    private static final String API_GENES = "/genes";
    private static final String API_GENE_ALIASES = "/genesaliases";

    public ApiService apiService;

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
                        e.getMessage());
            }
    }

    public static void main(String[] args) throws Exception {

        // check args
        if (args.length != 1 ||
                args[0].equals("-h") || args[0].equals("--help")) {
            System.err.print(
                    "Command line usage:  dumpPortalInfo.pl" +
                    " <name for the output directory>\n" +
                    "\n" +
                    "Generate a folder of files describing the portal " +
                    "configuration.\n" +
                    "\n" +
                    "This is a subset of the information provided by the " +
                    "web API,\n" +
                    "intended for offline use of the validation script for " +
                    "study data.\n");
            System.exit(EX_USAGE);
        }
        String outputDirName = args[0];
        System.err.printf(
                "Writing portal info files to directory '%s'...\n",
                outputDirName);

        // initialize progress monitor to print status output
        ProgressMonitor.setConsoleMode(true);
        // initialize application context, including database connection
        SpringUtil.initDataSource();
        ApiService apiService = SpringUtil.getApplicationContext().getBean(
                ApiService.class);

        File outputDir = new File(outputDirName);
        // this will do nothing if the directory already exists:
        // the files will simply be overwritten
        outputDir.mkdir();
        if (!outputDir.isDirectory()) {
            System.err.printf(
                    "Could not create directory '%s'.\n",
                    outputDir.getPath());
            System.exit(EX_CANTCREAT);
        }

        try {
            writeJsonFile(
                    apiService.getCancerTypes(),
                    nameJsonFile(outputDir, API_CANCER_TYPES));
            writeJsonFile(
                    apiService.getSampleClinicalAttributes(),
                    nameJsonFile(outputDir, API_SAMPLE_ATTRS));
            writeJsonFile(
                    apiService.getPatientClinicalAttributes(),
                    nameJsonFile(outputDir, API_PATIENT_ATTRS));
            writeJsonFile(
                    apiService.getGenes(),
                    nameJsonFile(outputDir, API_GENES));
            writeJsonFile(
                    apiService.getGenesAliases(),
                    nameJsonFile(outputDir, API_GENE_ALIASES));
        } catch (IOException e) {
            System.err.println(
                    "Error writing portal info file: " +
                    e.getMessage());
            System.exit(EX_IOERR);
        }

        ConsoleUtil.showWarnings();
        System.err.println("Done.");

   }

}
