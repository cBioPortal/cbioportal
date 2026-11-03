/*
 * Copyright (c) 2016 Memorial Sloan-Kettering Cancer Center.
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.mskcc.cbio.portal.scripts;

import java.io.*;
import java.util.*;
import joptsimple.*;
import org.mskcc.cbio.portal.dao.DaoGenePanel;
import org.mskcc.cbio.portal.dao.DaoSampleProfile;
import org.mskcc.cbio.portal.model.CanonicalGene;
import org.mskcc.cbio.portal.model.GenePanel;
import org.mskcc.cbio.portal.util.ProgressMonitor;
import static org.mskcc.cbio.portal.util.GenePanelUtil.extractGenes;
import static org.mskcc.cbio.portal.util.GenePanelUtil.extractPropertyValue;

/**
 * @author heinsz
 */
public class ImportGenePanel extends ConsoleRunnable {

    private File genePanelFile;

    @Override
    public void run() {
        try {
            String progName = "ImportGenePanel";
            String description = "Import gene panel files.";

            OptionParser parser = new OptionParser();
            OptionSpec<String> data = parser.accepts("data",
                "gene panel file").withRequiredArg().describedAs("data_file.txt").ofType(String.class);
            parser.accepts("noprogress", "this option can be given to avoid the messages regarding memory usage and % complete");

            OptionSet options = null;
            try {
                options = parser.parse(args);
            } catch (OptionException e) {
                throw new UsageException(
                    progName, description, parser,
                    e.getMessage());
            }
            File genePanel_f = null;
            if (options.has(data)) {
                genePanel_f = new File(options.valueOf(data));
            } else {
                throw new UsageException(
                    progName, description, parser,
                    "'data' argument required.");
            }

            setFile(genePanel_f);
            importData();
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void importData() throws Exception {
        ProgressMonitor.setCurrentMessage("Reading data from:  " + genePanelFile.getAbsolutePath());
        Properties properties = new Properties();
        properties.load(new FileInputStream(genePanelFile));

        String stableId = extractPropertyValue("stable_id", properties, true);
        String description = extractPropertyValue("description", properties, false);
        Set<CanonicalGene> canonicalGenes = extractGenes(properties, false);

        GenePanel genePanel = DaoGenePanel.getGenePanelByStableId(stableId);
        boolean panelUsed = false;
        if (genePanel != null) {
            if (DaoSampleProfile.sampleProfileMappingExistsByPanel(genePanel.getInternalId())) {
                ProgressMonitor.logWarning("Gene panel " + stableId + " already exists in databasel and is being used! Cannot import the gene panel!");
                panelUsed = true;
            } else {
                DaoGenePanel.deleteGenePanel(genePanel);
                ProgressMonitor.logWarning("Gene panel " + stableId + " already exists in the database but is not being used. Overwriting old gene panel data.");
            }
        }

        if (!panelUsed) {
            if (canonicalGenes != null) {
                DaoGenePanel.addGenePanel(stableId, description, canonicalGenes);
            } else {
                ProgressMonitor.logWarning("Gene panel " + stableId + " cannot be imported because one or more genes in the panel are not found in the database, or are duplicated.");
            }
        }
    }

    public void setFile(File genePanelFile) {
        this.genePanelFile = genePanelFile;
    }

    /**
     * Makes an instance to run with the given command line arguments.
     *
     * @param args the command line arguments to be used
     */
    public ImportGenePanel(String[] args) {
        super(args);
    }

    /**
     * Runs the command as a script and exits with an appropriate exit code.
     *
     * @param args the arguments given on the command line
     */
    public static void main(String[] args) {
        ConsoleRunnable runner = new ImportGenePanel(args);
        runner.runInConsole();
    }
}
