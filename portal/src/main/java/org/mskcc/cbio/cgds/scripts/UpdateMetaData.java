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

package org.mskcc.cbio.cgds.scripts;

import org.mskcc.cbio.cgds.dao.DaoGeneticProfile;
import org.mskcc.cbio.cgds.util.ConsoleUtil;
import org.mskcc.cbio.cgds.util.GeneticProfileReader;
import org.mskcc.cbio.cgds.util.ProgressMonitor;
import org.mskcc.cbio.cgds.model.GeneticProfile;

import java.io.File;

/**
 * Command Line to Update Meta Data Associated with a Genomic Profile.
 */
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