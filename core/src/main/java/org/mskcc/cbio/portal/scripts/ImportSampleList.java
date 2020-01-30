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

import java.io.*;
import java.util.*;
import org.mskcc.cbio.portal.dao.*;
import org.mskcc.cbio.portal.model.*;
import org.mskcc.cbio.portal.util.*;

/**
 * Command Line tool to Import Sample Lists.
 */
public class ImportSampleList extends ConsoleRunnable {

    public static void importSampleList(File dataFile)
        throws IOException, DaoException {
        ProgressMonitor.setCurrentMessage(
            "Read data from:  " + dataFile.getAbsolutePath()
        );
        Properties properties = new TrimmedProperties();
        properties.load(new FileInputStream(dataFile));

        String stableId = properties.getProperty("stable_id");

        if (stableId.contains(" ")) {
            throw new IllegalArgumentException(
                "stable_id cannot contain spaces:  " + stableId
            );
        }

        if (stableId == null || stableId.length() == 0) {
            throw new IllegalArgumentException("stable_id is not specified.");
        }

        String cancerStudyIdentifier = properties.getProperty(
            "cancer_study_identifier"
        );
        if (cancerStudyIdentifier == null) {
            throw new IllegalArgumentException(
                "cancer_study_identifier is not specified."
            );
        }
        SpringUtil.initDataSource();
        CancerStudy theCancerStudy = DaoCancerStudy.getCancerStudyByStableId(
            cancerStudyIdentifier
        );
        if (theCancerStudy == null) {
            throw new IllegalArgumentException(
                "cancer study identified by cancer_study_identifier '" +
                cancerStudyIdentifier +
                "' not found in dbms or inaccessible to user."
            );
        }

        String sampleListName = properties.getProperty("case_list_name");

        String sampleListCategoryStr = properties.getProperty(
            "case_list_category"
        );
        if (
            sampleListCategoryStr == null || sampleListCategoryStr.length() == 0
        ) {
            sampleListCategoryStr = "other";
        }
        SampleListCategory sampleListCategory = SampleListCategory.get(
            sampleListCategoryStr
        );

        String sampleListDescription = properties.getProperty(
            "case_list_description"
        );
        String sampleListStr = properties.getProperty("case_list_ids");
        if (sampleListName == null) {
            throw new IllegalArgumentException(
                "case_list_name is not specified."
            );
        } else if (sampleListDescription == null) {
            throw new IllegalArgumentException(
                "case_list_description is not specified."
            );
        }

        boolean itemsAddedViaPatientLink = false;
        // construct sample id list
        ArrayList<String> sampleIDsList = new ArrayList<String>();
        String[] sampleIds = sampleListStr.split("\t");
        for (String sampleId : sampleIds) {
            sampleId = StableIdUtil.getSampleId(sampleId);
            Sample s = DaoSample.getSampleByCancerStudyAndSampleId(
                theCancerStudy.getInternalId(),
                sampleId
            );
            if (s == null) {
                String warningMessage =
                    "Error: could not find sample " + sampleId;
                Patient p = DaoPatient.getPatientByCancerStudyAndPatientId(
                    theCancerStudy.getInternalId(),
                    sampleId
                );
                if (p != null) {
                    warningMessage +=
                        ". But found a patient with this ID. Will use its samples in the sample list.";
                    List<Sample> samples = DaoSample.getSamplesByPatientId(
                        p.getInternalId()
                    );
                    for (Sample sa : samples) {
                        if (!sampleIDsList.contains(sa.getStableId())) {
                            sampleIDsList.add(sa.getStableId());
                            itemsAddedViaPatientLink = true;
                        }
                    }
                }
                ProgressMonitor.logWarning(warningMessage);
            } else if (!sampleIDsList.contains(s.getStableId())) {
                sampleIDsList.add(s.getStableId());
            } else {
                ProgressMonitor.logWarning(
                    "Warning: duplicated sample ID " +
                    s.getStableId() +
                    " in case list " +
                    stableId
                );
            }
        }

        DaoSampleList daoSampleList = new DaoSampleList();
        SampleList sampleList = daoSampleList.getSampleListByStableId(stableId);
        if (sampleList != null) {
            throw new IllegalArgumentException(
                "Patient list with this stable Id already exists:  " + stableId
            );
        }

        sampleList = new SampleList();
        sampleList.setStableId(stableId);
        int cancerStudyId = theCancerStudy.getInternalId();
        sampleList.setCancerStudyId(cancerStudyId);
        sampleList.setSampleListCategory(sampleListCategory);
        sampleList.setName(sampleListName);
        sampleList.setDescription(sampleListDescription);
        sampleList.setSampleList(sampleIDsList);
        daoSampleList.addSampleList(sampleList);

        sampleList = daoSampleList.getSampleListByStableId(stableId);

        ProgressMonitor.setCurrentMessage(
            " --> stable ID:  " + sampleList.getStableId()
        );
        ProgressMonitor.setCurrentMessage(
            " --> sample list name:  " + sampleList.getName()
        );
        ProgressMonitor.setCurrentMessage(
            " --> number of samples in file:  " + sampleIds.length
        );
        String warningSamplesViaPatientLink =
            (
                itemsAddedViaPatientLink
                    ? "(nb: can be higher if samples were added via patient link)"
                    : ""
            );
        ProgressMonitor.setCurrentMessage(
            " --> number of samples stored in final sample list " +
            warningSamplesViaPatientLink +
            ":  " +
            sampleIDsList.size()
        );
    }

    public void run() {
        try {
            // check args
            if (args.length < 1) {
                // an extra --noprogress option can be given to avoid the messages regarding memory usage and % complete
                throw new UsageException(
                    "importCaseListData.pl ",
                    null,
                    "<data_file.txt or directory>"
                );
            }
            File dataFile = new File(args[0]);
            if (dataFile.isDirectory()) {
                File files[] = dataFile.listFiles();
                for (File file : files) {
                    if (
                        !file.getName().startsWith(".") &&
                        !file.getName().endsWith("~")
                    ) {
                        ImportSampleList.importSampleList(file);
                    }
                }
                if (files.length == 0) {
                    ProgressMonitor.logWarning(
                        "No patient lists found in directory, skipping import: " +
                        dataFile.getCanonicalPath()
                    );
                }
            } else {
                if (
                    !dataFile.getName().startsWith(".") &&
                    !dataFile.getName().endsWith("~")
                ) {
                    ImportSampleList.importSampleList(dataFile);
                } else {
                    ProgressMonitor.logWarning(
                        "File name starts with '.' or ends with '~', so it was skipped: " +
                        dataFile.getCanonicalPath()
                    );
                }
            }
        } catch (RuntimeException e) {
            throw e;
        } catch (IOException | DaoException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Makes an instance to run with the given command line arguments.
     *
     * @param args  the command line arguments to be used
     */
    public ImportSampleList(String[] args) {
        super(args);
    }

    /**
     * Runs the command as a script and exits with an appropriate exit code.
     *
     * @param args  the arguments given on the command line
     */
    public static void main(String[] args) {
        ConsoleRunnable runner = new ImportSampleList(args);
        runner.runInConsole();
    }
}
