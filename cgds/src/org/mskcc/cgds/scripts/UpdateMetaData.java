package org.mskcc.cgds.scripts;

import org.mskcc.cgds.dao.MySQLbulkLoader;
import org.mskcc.cgds.dao.DaoGeneticProfile;
import org.mskcc.cgds.model.GeneticAlterationType;
import org.mskcc.cgds.model.GeneticProfile;
import org.mskcc.cgds.util.ConsoleUtil;
import org.mskcc.cgds.util.FileUtil;
import org.mskcc.cgds.util.GeneticProfileReader;
import org.mskcc.cgds.util.ProgressMonitor;

import java.io.File;

public class UpdateMetaData {

    public static void main(String[] args) throws Exception {
       String usageLine = "command line usage:  updateMetaData.pl <meta_data_file.txt>";
        if (args.length < 1) {
            System.err.println(usageLine);
            System.exit(1);
        }

        ProgressMonitor pMonitor = new ProgressMonitor();
        pMonitor.setConsoleMode(true);
        File descriptorFile = new File(args[0]);

        GeneticProfile geneticProfile = GeneticProfileReader.loadGeneticProfileFromMeta(descriptorFile);

        DaoGeneticProfile daoGeneticProfile = new DaoGeneticProfile();
        GeneticProfile existingGeneticProfile = daoGeneticProfile.getGeneticProfileByStableId
                (geneticProfile.getStableId());
        
        // TODO: handle null existingGeneticProfile
        System.out.println ("Found Genetic Profile:  " + existingGeneticProfile.getStableId());
        System.out.println ("Changing name from:  " + existingGeneticProfile.getProfileName());
        System.out.println ("                to:  " + geneticProfile.getProfileName());
        System.out.println ("Changing desc from:  " + existingGeneticProfile.getProfileDescription());
        System.out.println ("                to:  " + geneticProfile.getProfileDescription());

        boolean flag = daoGeneticProfile.updateNameAndDescription
                (existingGeneticProfile.getGeneticProfileId(),
                geneticProfile.getProfileName(), geneticProfile.getProfileDescription());

        if (flag) {
            System.out.println ("Success!");
        } else {
            System.out.println ("Update Failed!");
        }

        ConsoleUtil.showWarnings(pMonitor);
        System.err.println("Done.");
    }

}