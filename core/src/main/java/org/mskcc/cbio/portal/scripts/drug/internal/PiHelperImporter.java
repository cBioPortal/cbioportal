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

package org.mskcc.cbio.portal.scripts.drug.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mskcc.cbio.portal.dao.*;
import org.mskcc.cbio.portal.model.CanonicalGene;
import org.mskcc.cbio.portal.model.Drug;
import org.mskcc.cbio.portal.scripts.drug.AbstractDrugInfoImporter;
import org.mskcc.cbio.portal.scripts.drug.DrugDataResource;

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;

public class PiHelperImporter extends AbstractDrugInfoImporter {
    private static final String separator = "\t";

    private InputStream drugInfoFile;
    private InputStream drugTargetsFile;
    private HashMap<String, Drug> nameToDrugMap = new HashMap<String, Drug>();

    // Ugly solution, but makes debugging much easier
    private final Drug DRUG_SKIP = new Drug();

    public PiHelperImporter(DrugDataResource dataResource) throws DaoException {
        super.setDataResource(dataResource);
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

        int lineNo = 0, saved = 0;
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            if(line.startsWith("#")) continue;
            if((++lineNo) == 1) continue;

            String[] tokens = line.split(separator, -1);
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

            if(drug==null || drug == DRUG_SKIP)
                continue;

            CanonicalGene gene = daoGeneOptimized.getNonAmbiguousGene(geneSymbol);
        }

        scanner.close();

        System.out.println("Number of drug-targets imported: " + saved);
    }

    private void importDrugs() throws Exception {
        nameToDrugMap.clear();

        Scanner scanner = new Scanner(getDrugInfoFile());
        int lineNo = 0;
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            if(line.startsWith("#")) continue;
            if((++lineNo) == 1) continue;

            try {
                importDrug(line);
            } catch (Exception e) {
                System.err.println("Failed to load drug "+line);
                e.printStackTrace();
            }

        }

        scanner.close();

        System.out.println("Number of drugs imported: " + nameToDrugMap.keySet().size());
    }
    
    private void importDrug(String line) throws DaoException {
        String[] t = line.split(separator, -1);
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
                nameToDrugMap.put(drug.getName(), drug);
            }
    }
}
