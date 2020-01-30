/*
 * Copyright (c) 2016 Memorial Sloan-Kettering Cancer Center.
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
import joptsimple.*;
import org.cbioportal.model.Gene;
import org.mskcc.cbio.portal.model.GenePanel;
import org.mskcc.cbio.portal.repository.GenePanelRepositoryLegacy;
import org.mskcc.cbio.portal.util.ProgressMonitor;
import org.mskcc.cbio.portal.util.SpringUtil;

/**
 *
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
            OptionSpec<String> data = parser
                .accepts("data", "gene panel file")
                .withRequiredArg()
                .describedAs("data_file.txt")
                .ofType(String.class);
            parser.accepts(
                "noprogress",
                "this option can be given to avoid the messages regarding memory usage and % complete"
            );

            OptionSet options = null;
            try {
                options = parser.parse(args);
            } catch (OptionException e) {
                throw new UsageException(
                    progName,
                    description,
                    parser,
                    e.getMessage()
                );
            }
            File genePanel_f = null;
            if (options.has(data)) {
                genePanel_f = new File(options.valueOf(data));
            } else {
                throw new UsageException(
                    progName,
                    description,
                    parser,
                    "'data' argument required."
                );
            }

            setFile(genePanel_f);
            SpringUtil.initDataSource();
            importData();
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void importData() throws Exception {
        ProgressMonitor.setCurrentMessage(
            "Reading data from:  " + genePanelFile.getAbsolutePath()
        );
        Properties properties = new Properties();
        properties.load(new FileInputStream(genePanelFile));

        GenePanelRepositoryLegacy genePanelRepositoryLegacy = (GenePanelRepositoryLegacy) SpringUtil
            .getApplicationContext()
            .getBean("genePanelRepositoryLegacy");

        String stableId = getPropertyValue("stable_id", properties, true);
        String description = getPropertyValue("description", properties, false);
        Set<Integer> genes = getGenes(
            "gene_list",
            properties,
            genePanelRepositoryLegacy
        );

        GenePanel genePanel = new GenePanel();
        List<GenePanel> genePanelResult = genePanelRepositoryLegacy.getGenePanelByStableId(
            stableId
        );
        boolean panelUsed = false;
        if (genePanelResult != null && genePanelResult.size() > 0) {
            genePanel = genePanelResult.get(0);
            if (
                genePanelRepositoryLegacy.sampleProfileMappingExistsByPanel(
                    genePanel.getInternalId()
                )
            ) {
                ProgressMonitor.logWarning(
                    "Gene panel " +
                    stableId +
                    " already exists in databasel and is being used! Cannot import the gene panel!"
                );
                panelUsed = true;
            } else {
                genePanelRepositoryLegacy.deleteGenePanel(
                    genePanel.getInternalId()
                );
                ProgressMonitor.logWarning(
                    "Gene panel " +
                    stableId +
                    " already exists in the database but is not being used. Overwriting old gene panel data."
                );
            }
        }

        if (!panelUsed) {
            Map<String, Object> map = new HashMap<String, Object>();
            map.put("stableId", stableId);
            map.put("description", description);
            genePanelRepositoryLegacy.insertGenePanel(map);
            genePanel =
                genePanelRepositoryLegacy
                    .getGenePanelByStableId(stableId)
                    .get(0);

            if (genes.size() > 0) {
                map = new HashMap<String, Object>();
                map.put("panelId", genePanel.getInternalId());
                map.put("genes", genes);
                genePanelRepositoryLegacy.insertGenePanelList(map);
            }
        }
    }

    private static String getPropertyValue(
        String propertyName,
        Properties properties,
        boolean noSpaceAllowed
    )
        throws IllegalArgumentException {
        String propertyValue = properties.getProperty(propertyName).trim();

        if (propertyValue == null || propertyValue.length() == 0) {
            throw new IllegalArgumentException(
                propertyName + " is not specified."
            );
        }

        if (noSpaceAllowed && propertyValue.contains(" ")) {
            throw new IllegalArgumentException(
                propertyName + " cannot contain spaces: " + propertyValue
            );
        }

        return propertyValue;
    }

    private static Set<Integer> getGenes(
        String propertyName,
        Properties properties,
        GenePanelRepositoryLegacy genePanelRepositoryLegacy
    ) {
        String propertyValue = properties.getProperty(propertyName).trim();
        if (propertyValue == null || propertyValue.length() == 0) {
            throw new IllegalArgumentException(
                propertyName + " is not specified."
            );
        }

        Set<Integer> geneIds = new HashSet<>();
        String[] genes = propertyValue.split("\t");
        for (String panelGene : genes) {
            Gene gene = null;
            try {
                Integer geneId = Integer.parseInt(panelGene);
                gene = genePanelRepositoryLegacy.getGeneByEntrezGeneId(geneId);
                if (gene != null) {
                    geneIds.add(geneId);
                } else {
                    throw new RuntimeException(
                        "Could not find gene in the database: " +
                        String.valueOf(geneId)
                    );
                }
            } catch (NumberFormatException e) {
                gene = genePanelRepositoryLegacy.getGeneByHugoSymbol(panelGene);
                if (gene == null) {
                    gene = genePanelRepositoryLegacy.getGeneByAlias(panelGene);
                }

                if (gene != null) {
                    geneIds.add(gene.getEntrezGeneId());
                }
            }
        }

        return geneIds;
    }

    public void setFile(File genePanelFile) {
        this.genePanelFile = genePanelFile;
    }

    /**
     * Makes an instance to run with the given command line arguments.
     *
     * @param args  the command line arguments to be used
     */
    public ImportGenePanel(String[] args) {
        super(args);
    }

    /**
     * Runs the command as a script and exits with an appropriate exit code.
     *
     * @param args  the arguments given on the command line
     */
    public static void main(String[] args) {
        ConsoleRunnable runner = new ImportGenePanel(args);
        runner.runInConsole();
    }
}
