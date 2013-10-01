/** Copyright (c) 2012 Memorial Sloan-Kettering Cancer Center.
**
** This library is free software; you can redistribute it and/or modify it
** under the terms of the GNU Lesser General Public License as published
** by the Free Software Foundation; either version 2.1 of the License, or
** any later version.
**
** This library is distributed in the hope that it will be useful, but
** WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
** MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
** documentation provided hereunder is on an "as is" basis, and
** Memorial Sloan-Kettering Cancer Center 
** has no obligations to provide maintenance, support,
** updates, enhancements or modifications.  In no event shall
** Memorial Sloan-Kettering Cancer Center
** be liable to any party for direct, indirect, special,
** incidental or consequential damages, including lost profits, arising
** out of the use of this software and its documentation, even if
** Memorial Sloan-Kettering Cancer Center 
** has been advised of the possibility of such damage.  See
** the GNU Lesser General Public License for more details.
**
** You should have received a copy of the GNU Lesser General Public License
** along with this library; if not, write to the Free Software Foundation,
** Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
**/

package org.mskcc.cbio.portal.scripts;

import org.mskcc.cbio.portal.dao.DaoException;
import org.mskcc.cbio.portal.dao.DaoTypeOfCancer;
import org.mskcc.cbio.portal.model.TypeOfCancer;
import org.mskcc.cbio.portal.util.ConsoleUtil;
import org.mskcc.cbio.portal.util.ProgressMonitor;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.Properties;
import java.util.Scanner;

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
        DaoTypeOfCancer.deleteAllRecords();
        TypeOfCancer aTypeOfCancer = new TypeOfCancer();
        Scanner scanner = new Scanner(file);

        while(scanner.hasNextLine()) {
            String[] tokens = scanner.nextLine().split("\t", -1);
            assert tokens.length == 3;

            String typeOfCancerId = tokens[0].trim();
            aTypeOfCancer.setTypeOfCancerId(typeOfCancerId);
            aTypeOfCancer.setName(tokens[1].trim());
            aTypeOfCancer.setClinicalTrialKeywords(tokens[2].trim().toLowerCase());
            DaoTypeOfCancer.addTypeOfCancer(aTypeOfCancer);
        }
        pMonitor.setCurrentMessage("Loaded " + DaoTypeOfCancer.getCount() + " TypesOfCancers.");
        ConsoleUtil.showWarnings(pMonitor);
    }

}
