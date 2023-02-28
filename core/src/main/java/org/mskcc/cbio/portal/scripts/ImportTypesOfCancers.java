/*
 * Copyright (c) 2015 - 2021 Memorial Sloan-Kettering Cancer Center.
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
import org.mskcc.cbio.portal.dao.DaoException;
import org.mskcc.cbio.portal.dao.DaoTypeOfCancer;
import org.mskcc.cbio.portal.model.TypeOfCancer;
import org.mskcc.cbio.portal.scripts.ConsoleRunnable;
import org.mskcc.cbio.portal.util.ConsoleUtil;
import org.mskcc.cbio.portal.util.ProgressMonitor;
import org.mskcc.cbio.portal.util.SpringUtil;

/**
 * Load all the types of cancer and their names from a file.
 *
 * @author Arthur Goldberg goldberg@cbio.mskcc.org
 */
public class ImportTypesOfCancers extends ConsoleRunnable {

    public static final int EXPECTED_DATAFILE_COLUMN_COUNT = 4;

    /**
     * Executed by perl script importTypesOfCancer.pl - parses command line arguments and calls load()
     */
    public void run() {
        if (args.length < 1) {
            throw new UsageException( "importTypesOfCancer.pl", null, "<types_of_cancer.txt> <clobber>");
        }
        String filePath = args[0];
        boolean clobber = true;
        if (args.length > 1) {
            String clobberArgString = args[1]; // positional argument
            if (clobberArgString.equalsIgnoreCase("f") || clobberArgString.equalsIgnoreCase("false")) {
                clobber = false;
            }
        }
        try {
            load(new File(filePath), clobber);
        } catch (IOException|DaoException e) {
            throw new RuntimeException(e);
        }
    }

    public static void load(File file, boolean clobber) throws IOException, DaoException {
        ProgressMonitor.setCurrentMessage("Loading cancer types...");
        List<TypeOfCancer> typeOfCancerList = parseCancerTypesFromFile(file);
        SpringUtil.initDataSource();
        if (clobber) {
            ProgressMonitor.setCurrentMessage("Deleting all previous cancer types...");
            DaoTypeOfCancer.deleteAllRecords(); //TODO - remove this option - foreign key constraints may mean large cascade effects (possibly the deletion of all studies) - instead, change the option to 'deleteTypeOfCancerIfNotPresent' and add a loop through existing typeOfCancer records, removing those which are not in the parsed typeOfCancerList
        }
        writeRecordsToDatabase(typeOfCancerList, clobber);
        ProgressMonitor.setCurrentMessage("Done.");
        ConsoleUtil.showMessages();
    }

    private static List<TypeOfCancer> parseCancerTypesFromFile(File file) throws IOException {
        ProgressMonitor.setCurrentMessage(String.format("Reading cancer types from file '%s'...", file.getPath()));
        List<TypeOfCancer> typeOfCancerList = new ArrayList<TypeOfCancer>();
        Scanner scanner = new Scanner(file);
        while (scanner.hasNextLine()) {
            String nextLine = scanner.nextLine();
            String[] fields = nextLine.split("\t", -1);
            throwExceptionIfColumnCountIsWrong(file, nextLine, fields, EXPECTED_DATAFILE_COLUMN_COUNT);
            TypeOfCancer typeOfCancer = new TypeOfCancer();
            String typeOfCancerId = fields[0].trim();
            typeOfCancer.setTypeOfCancerId(typeOfCancerId.toLowerCase());
            typeOfCancer.setName(fields[1].trim());
            typeOfCancer.setDedicatedColor(fields[2].trim());
            typeOfCancer.setShortName(typeOfCancerId);
            typeOfCancer.setParentTypeOfCancerId(fields[3].trim().toLowerCase());
            typeOfCancerList.add(typeOfCancer);
        }
        return typeOfCancerList;
    }

    private static void throwExceptionIfColumnCountIsWrong(File file, String nextLine, String[] fields, int expectedDatafileColumnCount) throws IOException {
        if (fields.length != expectedDatafileColumnCount) {
            String msg = String.format("Cancer type file '%s' contains a line which is not %d-column tab-delimited." +
                    " Expected fields can be seen here: https://docs.cbioportal.org/5.1-data-loading/data-loading/file-formats#cancer-type" +
                    " The invalid line has %d columns:\n%s", file.getPath(), expectedDatafileColumnCount, fields.length, nextLine);
            for (int i = 0; i < fields.length ; i++) {
                msg = msg + String.format("  field#%d(len%d):'%s'\n", i, fields[i].length(), fields[i]);
            }
        throw new IOException(msg);
        }
    }

    private static void writeRecordsToDatabase(List<TypeOfCancer> typeOfCancerList, boolean clobber) throws IOException, DaoException {
        int numNewCancerTypes = 0;
        for (TypeOfCancer typeOfCancer : typeOfCancerList) {
            if (!clobber && typeOfCancerExistsInDatabase(typeOfCancer)) {
                ProgressMonitor.logWarning(String.format("Cancer type with id '%s' already exists. Skipping.", typeOfCancer.getTypeOfCancerId()));
            } else {
                DaoTypeOfCancer.addTypeOfCancer(typeOfCancer);
                numNewCancerTypes++;
            }
        }
        ProgressMonitor.setCurrentMessage(String.format(" --> Loaded %d new cancer types.", numNewCancerTypes));
    }

    private static boolean typeOfCancerExistsInDatabase(TypeOfCancer typeOfCancer) throws DaoException {
        return DaoTypeOfCancer.getTypeOfCancerById(typeOfCancer.getTypeOfCancerId()) != null;
    }

    /**
     * Makes an instance to run with the given command line arguments.
     *
     * @param args  the command line arguments to be used
     */
    public ImportTypesOfCancers(String[] args) {
        super(args);
    }

    /**
     * Runs the command as a script and exits with an appropriate exit code.
     *
     * @param args  the arguments given on the command line
     */
    public static void main(String[] args) {
        ConsoleRunnable runner = new ImportTypesOfCancers(args);
        runner.runInConsole();
    }
}
