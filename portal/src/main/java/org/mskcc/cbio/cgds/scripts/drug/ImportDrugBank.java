package org.mskcc.cbio.cgds.scripts.drug;

import au.com.bytecode.opencsv.CSVReader;
import org.mskcc.cgds.scripts.drug.internal.DrugBankImporter;

import java.io.File;
import java.io.FileReader;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class ImportDrugBank {
    public static void main(String arg[]) throws Exception {
        if(arg.length < 2) {
            System.err.println(
                    "Missing options!\nUsage: "
                            + ImportDrugBank.class.getSimpleName()
                            + " drugbank.xml target_links.csv"
            );
            System.exit(-1);
        }

        Calendar cal = Calendar.getInstance();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM");
        File file = new File(arg[0]);
        DrugDataResource drugBank
                = new DrugDataResource("DrugBank", "file://" + file.getAbsolutePath(), format.format(cal.getTime()));

        DrugBankImporter drugBankImporter = new DrugBankImporter(drugBank);
        CSVReader csvReader = new CSVReader(new FileReader(arg[1]));
        drugBankImporter.importDrugBankGeneList(csvReader);

        drugBankImporter.importData();

        System.out.println("DrugBank import done!");
    }
}
