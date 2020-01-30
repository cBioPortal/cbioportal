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
import org.mskcc.cbio.portal.model.*;
import org.mskcc.cbio.portal.util.*;

/**
 * Command Line Tool to Import a Single Cancer Study.
 */
public class ImportCancerStudy extends ConsoleRunnable {

    public void run() {
        try {
            if (args.length < 1) {
                // an extra --noprogress option can be given to avoid the messages regarding memory usage and % complete
                throw new UsageException(
                    "importCancerStudy.pl",
                    null,
                    "<cancer_study.txt>"
                );
            }

            File file = new File(args[0]);
            SpringUtil.initDataSource();
            CancerStudy cancerStudy = CancerStudyReader.loadCancerStudy(file);
            CancerStudyTags cancerStudyTags = CancerStudyTagsReader.loadCancerStudyTags(
                file,
                cancerStudy
            );
            String message =
                "Loaded the following cancer study:" +
                "\n --> Study ID:  " +
                cancerStudy.getInternalId() +
                "\n --> Name:  " +
                cancerStudy.getName() +
                "\n --> Description:  " +
                cancerStudy.getDescription();

            if (cancerStudyTags != null) {
                message += "\n --> Study Tags:  " + cancerStudyTags.getTags();
            }
            ProgressMonitor.setCurrentMessage(message);
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Makes an instance to run with the given command line arguments.
     *
     * @param args  the command line arguments to be used
     */
    public ImportCancerStudy(String[] args) {
        super(args);
    }

    /**
     * Runs the command as a script and exits with an appropriate exit code.
     *
     * @param args  the arguments given on the command line
     */
    public static void main(String[] args) {
        ConsoleRunnable runner = new ImportCancerStudy(args);
        runner.runInConsole();
    }
}
