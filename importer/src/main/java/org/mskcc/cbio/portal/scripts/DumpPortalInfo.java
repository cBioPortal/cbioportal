/*
 * Copyright (c) 2016 The Hyve B.V.
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.mskcc.cbio.portal.scripts;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import java.io.*;
import java.util.*;
import org.mskcc.cbio.portal.dao.DaoException;
import org.mskcc.cbio.portal.dao.DaoGeneOptimized;
import org.mskcc.cbio.portal.dao.DaoGenePanel;
import org.mskcc.cbio.portal.dao.DaoGeneset;
import org.mskcc.cbio.portal.dao.DaoInfo;
import org.mskcc.cbio.portal.dao.DaoTypeOfCancer;
import org.mskcc.cbio.portal.model.CanonicalGene;
import org.mskcc.cbio.portal.util.ProgressMonitor;

/**
 * Command line tool to generate JSON files used by the validation script.
 */
public class DumpPortalInfo extends ConsoleRunnable {

    // these names are defined in annotations to the methods of ApiController,
    // in org.mskcc.cbio.portal.web
    private static final String API_CANCER_TYPES = "/cancer-types";
    private static final String API_GENES = "/genes";
    private static final String API_GENE_ALIASES = "/genesaliases";
    private static final String API_GENESETS = "/genesets";
    private static final String API_GENESET_VERSION = "/genesets/version";
    private static final String API_GENE_PANELS = "/gene-panels";
    private static final ObjectMapper OBJECT_MAPPER = createPortalObjectMapper();

    static class GeneAlias implements Serializable {
        public String alias;
        public String entrezGeneId;
    }

    private static List<GeneAlias> extractGeneAliases(List<CanonicalGene> canonicalGenes) {
        List<GeneAlias> toReturn = new ArrayList<GeneAlias>();
        for (CanonicalGene canonicalGene : canonicalGenes) {
            String entrezGeneId = String.valueOf(canonicalGene.getEntrezGeneId()); 
            for (String alias : canonicalGene.getAliases()) {
                GeneAlias geneAlias = new GeneAlias();
                geneAlias.alias = alias;
                geneAlias.entrezGeneId = entrezGeneId;
                toReturn.add(geneAlias);
            }
        }
        return toReturn;
    }

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

    private static ObjectMapper createPortalObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        mapper.enable(SerializationFeature.WRITE_ENUMS_USING_TO_STRING);
        return mapper;
    }

    private static void writeJsonFile(
            Object value,
            File outputFile) throws IOException {
            try {
                OBJECT_MAPPER.writeValue(outputFile, value);
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
                        DaoTypeOfCancer.getAllTypesOfCancer(),
                        nameJsonFile(outputDir, API_CANCER_TYPES));
                DaoGeneOptimized daoGeneOptimized = DaoGeneOptimized.getInstance();
                List<CanonicalGene> allGenes = daoGeneOptimized.getAllGenes();
                writeJsonFile(
                        allGenes,
                        nameJsonFile(outputDir, API_GENES));
                writeJsonFile(
                        extractGeneAliases(allGenes),
                        nameJsonFile(outputDir, API_GENE_ALIASES));
                writeJsonFile(
                    DaoGeneset.getAllGenesets(),
                    nameJsonFile(outputDir, API_GENESETS));
                writeJsonFile(
                    List.of(DaoInfo.getGenesetVersion()),
                    nameJsonFile(outputDir, API_GENESET_VERSION));
                writeJsonFile(
                    DaoGenePanel.getAllGenePanels(),
                    nameJsonFile(outputDir, API_GENE_PANELS));
            } catch (DaoException e) {
                throw new RuntimeException(e);
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
