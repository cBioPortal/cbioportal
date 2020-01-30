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

import java.io.File;
import org.mskcc.cbio.portal.dao.*;
import org.mskcc.cbio.portal.model.GeneticProfile;
import org.mskcc.cbio.portal.util.*;

/**
 * Command Line to Update Meta Data Associated with a Genomic Profile.
 */
public class UpdateMetaData {

    public static void main(String[] args) throws Exception {
        String usageLine =
            "command line usage:  updateMetaData.pl <meta_data_file.txt>";
        if (args.length < 1) {
            System.err.println(usageLine);
            return;
        }

        ProgressMonitor.setConsoleMode(true);
        SpringUtil.initDataSource();
        File descriptorFile = new File(args[0]);

        GeneticProfile geneticProfile = GeneticProfileReader.loadGeneticProfileFromMeta(
            descriptorFile
        );

        GeneticProfile existingGeneticProfile = DaoGeneticProfile.getGeneticProfileByStableId(
            geneticProfile.getStableId()
        );

        // TODO: handle null existingGeneticProfile
        System.out.println(
            "Found Genetic Profile:  " + existingGeneticProfile.getStableId()
        );
        System.out.println(
            "Changing name from:  " + existingGeneticProfile.getProfileName()
        );
        System.out.println(
            "                to:  " + geneticProfile.getProfileName()
        );
        System.out.println(
            "Changing desc from:  " +
            existingGeneticProfile.getProfileDescription()
        );
        System.out.println(
            "                to:  " + geneticProfile.getProfileDescription()
        );

        boolean flag = DaoGeneticProfile.updateNameAndDescription(
            existingGeneticProfile.getGeneticProfileId(),
            geneticProfile.getProfileName(),
            geneticProfile.getProfileDescription()
        );

        if (flag) {
            System.out.println("Success!");
        } else {
            System.out.println("Update Failed!");
        }

        ConsoleUtil.showWarnings();
        System.err.println("Done.");
    }
}
