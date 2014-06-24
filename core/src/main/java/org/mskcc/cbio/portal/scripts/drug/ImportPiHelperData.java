/*
 * Copyright (c) 2012 Memorial Sloan-Kettering Cancer Center.
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
 * has been advised of the possibility of such damage.
*/

package org.mskcc.cbio.portal.scripts.drug;

import org.mskcc.cbio.portal.scripts.drug.internal.PiHelperImporter;

import java.io.FileInputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class ImportPiHelperData {
    public static void main(String arg[]) throws Exception {
        if(arg.length < 2) {
            System.err.println(
                    "Missing options!\nUsage: "
                            + ImportPiHelperData.class.getSimpleName()
                            + " drugs.tsv drugtargets.tsv"
            );
            return;
        }

        Calendar cal = Calendar.getInstance();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM");
        String today = format.format(cal.getTime());
        DrugDataResource pihelper = new DrugDataResource(
                "PiHelper",
                "https://bitbucket.org/armish/pihelper/downloads/pihelper_data_20121107.zip",
                today
        );

        PiHelperImporter piHelperImporter = new PiHelperImporter(pihelper);
        piHelperImporter.setDrugInfoFile(new FileInputStream(arg[0].trim()));
        piHelperImporter.setDrugTargetsFile(new FileInputStream(arg[1].trim()));

        piHelperImporter.importData();

        System.out.println("PiHelper import done!");
    }
}
