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

import org.mskcc.cbio.portal.model.Patient;
import org.mskcc.cbio.portal.dao.DaoPatient;
import org.mskcc.cbio.portal.dao.DaoException;
import org.mskcc.cbio.portal.util.ConsoleUtil;
import org.mskcc.cbio.portal.util.ProgressMonitor;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Scanner;

/**
 * Load the patient list from a file.
 *
 * @author BEG
 */
public class ImportPatientList
{
    public static void main(String[] args) throws IOException, DaoException
    {
        if (args.length != 1) {
            System.out.println("command line usage: java ... org.mskcc.cbio.portal.scripts.ImportPatientList <patient_list.txt>");
            return;
        }

        ProgressMonitor pMonitor = new ProgressMonitor();
        pMonitor.setConsoleMode(true);

        File file = new File(args[0]);
        load(pMonitor, file);
    }

    public static void load(ProgressMonitor pMonitor, File file) throws IOException, DaoException
    {

        int patientCount = 0;
        Scanner scanner = new Scanner(file);
        while(scanner.hasNextLine()) {
            String[] tokens = scanner.nextLine().split("\t", -1);
            assert tokens.length == 1;

            Patient aPatient = new Patient(tokens[0].trim());
            DaoPatient.addPatient(aPatient);
            ++patientCount;
        }

        pMonitor.setCurrentMessage("Loaded " + patientCount + " patients.");
        ConsoleUtil.showWarnings(pMonitor);
    }

}
