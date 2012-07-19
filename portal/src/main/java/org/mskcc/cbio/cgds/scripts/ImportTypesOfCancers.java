package org.mskcc.cbio.cgds.scripts;

import org.mskcc.cgds.dao.DaoException;
import org.mskcc.cgds.dao.DaoTypeOfCancer;
import org.mskcc.cgds.model.TypeOfCancer;
import org.mskcc.cgds.util.ConsoleUtil;
import org.mskcc.cgds.util.ProgressMonitor;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.Properties;

/**
 * Load all the types of cancer and their names from a file.
 *
 * @author Arthur Goldberg goldberg@cbio.mskcc.org
 */
public class ImportTypesOfCancers {
    public static void main(String[] args) throws IOException, DaoException {
        if (args.length != 1) {
            System.out.println("command line usage: importTypesOfCancer.pl <types_of_cancer.txt>");
            System.exit(1);
        }

        ProgressMonitor pMonitor = new ProgressMonitor();
        pMonitor.setConsoleMode(true);

        File file = new File(args[0]);
        load(pMonitor, file);
    }

    public static void load(ProgressMonitor pMonitor, File file) throws IOException, DaoException {
        Properties properties = new Properties();
        properties.load(new FileInputStream(file));
        DaoTypeOfCancer.deleteAllRecords();
        TypeOfCancer aTypeOfCancer = new TypeOfCancer();

        for (Iterator<Object> types = properties.keySet().iterator(); types.hasNext();) {

            String typeOfCancerId = (String) types.next();
            aTypeOfCancer.setTypeOfCancerId(typeOfCancerId);
            aTypeOfCancer.setName(properties.getProperty(typeOfCancerId));
            DaoTypeOfCancer.addTypeOfCancer(aTypeOfCancer);
        }
        pMonitor.setCurrentMessage("Loaded " + DaoTypeOfCancer.getCount() + " TypesOfCancers.");
        ConsoleUtil.showWarnings(pMonitor);
    }

}
