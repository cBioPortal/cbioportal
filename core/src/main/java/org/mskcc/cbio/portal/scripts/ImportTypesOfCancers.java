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

package org.mskcc.cbio.portal.scripts;

import org.mskcc.cbio.portal.dao.*;
import org.mskcc.cbio.portal.util.*;
import org.mskcc.cbio.portal.model.TypeOfCancer;

import java.io.*;
import java.util.*;

/**
 * Load all the types of cancer and their names from a file.
 *
 * @author Arthur Goldberg goldberg@cbio.mskcc.org
 */
public class ImportTypesOfCancers {
    public static void main(String[] args) throws IOException, DaoException {
        if (args.length < 1) {
            System.out.println("command line usage: importTypesOfCancer.pl <types_of_cancer.txt> <clobber>");
            return;
        }

        ProgressMonitor pMonitor = new ProgressMonitor();
        pMonitor.setConsoleMode(true);

        File file = new File(args[0]);
	// default to clobber = true (existing behavior)
	boolean clobber = (args.length == 2 && (args[1].equalsIgnoreCase("f") || args[1].equalsIgnoreCase("false"))) ? false : true;	
        load(pMonitor, file, clobber);
    }

    public static void load(ProgressMonitor pMonitor, File file) throws IOException, DaoException {
		SpringUtil.initDataSource();
        ImportTypesOfCancers.load(pMonitor, file, true);
    }

    public static void load(ProgressMonitor pMonitor, File file, boolean clobber) throws IOException, DaoException {
        if (clobber) DaoTypeOfCancer.deleteAllRecords();
        TypeOfCancer aTypeOfCancer = new TypeOfCancer();
        Scanner scanner = new Scanner(file);

        while(scanner.hasNextLine()) {
            String[] tokens = scanner.nextLine().split("\t", -1);
            assert tokens.length == 5;

            String typeOfCancerId = tokens[0].trim();
            aTypeOfCancer.setTypeOfCancerId(typeOfCancerId.toLowerCase());
            aTypeOfCancer.setName(tokens[1].trim());
            aTypeOfCancer.setClinicalTrialKeywords(tokens[2].trim().toLowerCase());
            aTypeOfCancer.setDedicatedColor(tokens[3].trim());
            aTypeOfCancer.setShortName(typeOfCancerId);
            aTypeOfCancer.setParentTypeOfCancerId(tokens[4].trim().toLowerCase());
            DaoTypeOfCancer.addTypeOfCancer(aTypeOfCancer);
        }
        pMonitor.setCurrentMessage("Loaded " + DaoTypeOfCancer.getCount() + " TypesOfCancers.");
        ConsoleUtil.showWarnings(pMonitor);
    }

}
