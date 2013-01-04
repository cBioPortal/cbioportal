/*
 * Copyright (c) 2012 Memorial Sloan-Kettering Cancer Center.
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation; either version 2.1 of the License, or
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
 * MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
 * documentation provided hereunder is on an "as is" basis, and
 * Memorial Sloan-Kettering Cancer Center
 * has no obligations to provide maintenance, support,
 * updates, enhancements or modifications.  In no event shall
 * Memorial Sloan-Kettering Cancer Center
 * be liable to any party for direct, indirect, special,
 * incidental or consequential damages, including lost profits, arising
 * out of the use of this software and its documentation, even if
 * Memorial Sloan-Kettering Cancer Center
 * has been advised of the possibility of such damage.  See
 * the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; if not, write to the Free Software Foundation,
 * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
 */

package org.mskcc.cbio.cgds.scripts.drug.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mskcc.cbio.cgds.dao.*;
import org.mskcc.cbio.cgds.model.CanonicalGene;
import org.mskcc.cbio.cgds.model.Drug;
import org.mskcc.cbio.cgds.model.DrugInteraction;
import org.mskcc.cbio.cgds.scripts.drug.AbstractDrugInfoImporter;
import org.mskcc.cbio.cgds.scripts.drug.DrugDataResource;

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;

public class PiHelperImporter extends AbstractDrugInfoImporter {
    private static final String separator = "\t";
    private static final Log log = LogFactory.getLog(PiHelperImporter.class);

    private InputStream drugInfoFile;
    private InputStream drugTargetsFile;
    private HashMap<String, Drug> nameToDrugMap = new HashMap<String, Drug>();

    // Ugly solution, but makes debugging much easier
    private final Drug DRUG_SKIP = new Drug();

    public PiHelperImporter(DrugDataResource dataResource) throws DaoException {
        super(dataResource);
    }

    public PiHelperImporter(DrugDataResource dataResource, DaoDrug drugDao, DaoInteraction daoInteraction) {
        super(dataResource, drugDao, daoInteraction);
    }

    public InputStream getDrugInfoFile() {
        return drugInfoFile;
    }

    public void setDrugInfoFile(InputStream drugInfoFile) {
        this.drugInfoFile = drugInfoFile;
    }

    public InputStream getDrugTargetsFile() {
        return drugTargetsFile;
    }

    public void setDrugTargetsFile(InputStream drugTargetsFile) {
        this.drugTargetsFile = drugTargetsFile;
    }

    @Override
    public void importData() throws Exception {
        // These are necessary files, hence the check below
        if(getDrugInfoFile() == null || getDrugTargetsFile() == null) {
            throw new IllegalArgumentException("Please provide drug and drug targets before you stat importing.");
        }
		MySQLbulkLoader.bulkLoadOff();
        importDrugs();
        importDrugTargets();
    }

    private void importDrugTargets() throws Exception {
        Scanner scanner = new Scanner(getDrugTargetsFile());
        DaoGeneOptimized daoGeneOptimized = DaoGeneOptimized.getInstance();
        DaoDrugInteraction daoDrugInteraction = DaoDrugInteraction.getInstance();

        int lineNo = 0, saved = 0;
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            if(line.startsWith("#")) continue;
            if((++lineNo) == 1) continue;

            String[] tokens = line.split(separator);
            assert tokens.length ==  5;
            if(tokens.length < 5) continue;
            /*
                0 - PiHelperId
                1 - Symbol
                2 - Drug
                3 - DataSources
                4 - References
             */

            String geneSymbol = tokens[1].trim();
            String drugName = tokens[2].trim();
            String datasources = tokens[3].trim();
            String refs = tokens[4].trim();

            Drug drug = nameToDrugMap.get(drugName);
            assert drug != null;

            if(drug == DRUG_SKIP)
                continue;

            List<CanonicalGene> genes = daoGeneOptimized.guessGene(geneSymbol);
            for (CanonicalGene gene : genes) {
                daoDrugInteraction.addDrugInteraction(
                        drug,
                        gene,
                        DRUG_INTERACTION_TYPE,
                        datasources,
                        "",
                        refs);
                saved++;
            }
        }

        scanner.close();

        log.info("Number of drug-targets imported: " + saved);
    }

    private void importDrugs() throws Exception {
        nameToDrugMap.clear();


        Scanner scanner = new Scanner(getDrugInfoFile());
        int lineNo = 0;
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            if(line.startsWith("#")) continue;
            if((++lineNo) == 1) continue;

            String[] t = line.split(separator);
            assert t.length ==  12;
            /*
                0 PiHelper_Drug_ID
                1 Drug_Name
                2 Drug_Synonyms
                3 Description
                4 Number_of_Targets
                5 ATC_Codes
                6 isFdaApproved
                7 isCancerDrug
                8 isNutraceutical
                9 Number_Of_Clinical_Trials
                10 DataSources
                11 References
             */

            Drug drug = new Drug(
                t[0].trim(),  // id
                t[1].trim(),  // name
                t[3].trim().replace("\"", ""),  // desc
                t[2].trim(),  // synonyms
                t[11].trim(), // refs
                t[10].trim(), // resource
                t[5].trim(), // atc
                Boolean.parseBoolean(t[6].trim()), // fda approval
                Boolean.parseBoolean(t[7].trim()), // is cancer drug
                Boolean.parseBoolean(t[8].trim()), // is Nutraceutical
                Integer.parseInt(t[9])
            );

            if(drug.isNutraceuitical()) { // We don't want these drugs within the database
                nameToDrugMap.put(drug.getName(), DRUG_SKIP);
            } else {
                getDrugDao().addDrug(drug);
                nameToDrugMap.put(drug.getName(), drug);
            }

        }

        scanner.close();

        log.info("Number of drugs imported: " + nameToDrugMap.keySet().size());
    }
}
