/*
 * Copyright (c) 2016 The Hyve B.V.
 * This code is licensed under the GNU Affero General Public License (AGPL),
 * version 3, or (at your option) any later version.
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

import java.util.ArrayList;
import java.util.List;
import org.mskcc.cbio.portal.dao.DaoCancerStudy;
import org.mskcc.cbio.portal.dao.DaoSample;
import org.mskcc.cbio.portal.dao.DaoSampleList;
import org.mskcc.cbio.portal.model.CancerStudy;
import org.mskcc.cbio.portal.model.Sample;
import org.mskcc.cbio.portal.model.SampleList;
import org.mskcc.cbio.portal.model.SampleListCategory;
import org.mskcc.cbio.portal.util.ConsoleUtil;
import org.mskcc.cbio.portal.util.ProgressMonitor;
import org.mskcc.cbio.portal.util.SpringUtil;

/**
 * Command Line tool to Add new case lists by generating them based on some rules.
 */
public class AddCaseList extends ConsoleRunnable {

    /**
     * Add case list of type "all"
     *
     * @param theCancerStudy
     * @param pMonitor
     * @throws Exception
     */
    private static void addAllCasesList(CancerStudy theCancerStudy)
        throws Exception {
        String cancerStudyIdentifier = theCancerStudy.getCancerStudyStableId();
        String stableId = cancerStudyIdentifier + "_all";
        ProgressMonitor.setCurrentMessage(
            "Adding case list:  " + stableId + "..."
        );

        String sampleListCategoryStr = "other"; //TODO : check if this is important...
        SampleListCategory sampleListCategory = SampleListCategory.get(
            sampleListCategoryStr
        );

        String sampleListDescription = "All cases in study";
        String sampleListName = sampleListDescription;

        // construct sample id list
        ArrayList<String> sampleIDsList = new ArrayList<String>();

        List<String> sampleIds = DaoSample.getSampleStableIdsByCancerStudy(
            theCancerStudy.getInternalId()
        );
        for (String sampleId : sampleIds) {
            Sample s = DaoSample.getSampleByCancerStudyAndSampleId(
                theCancerStudy.getInternalId(),
                sampleId
            );
            sampleIDsList.add(s.getStableId());
        }
        addCaseList(
            stableId,
            theCancerStudy,
            sampleListCategory,
            sampleListName,
            sampleListDescription,
            sampleIDsList
        );
    }

    /**
     * Generic method to add a case list
     *
     * @param stableId
     * @param theCancerStudy
     * @param sampleListCategory
     * @param sampleListName
     * @param sampleListDescription
     * @param sampleIDsList
     * @param pMonitor
     * @throws Exception
     */
    private static void addCaseList(
        String stableId,
        CancerStudy theCancerStudy,
        SampleListCategory sampleListCategory,
        String sampleListName,
        String sampleListDescription,
        ArrayList<String> sampleIDsList
    )
        throws Exception {
        DaoSampleList daoSampleList = new DaoSampleList();
        SampleList sampleList = daoSampleList.getSampleListByStableId(stableId);
        if (sampleList != null) {
            ProgressMonitor.logWarning(
                "Case list with this stable Id already exists:  " +
                stableId +
                ". Will keep the existing one."
            );
        } else {
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
                " --> case list name:  " + sampleList.getName()
            );
            ProgressMonitor.setCurrentMessage(
                " --> number of cases:  " + sampleIDsList.size()
            );
        }
    }

    public void run() {
        try {
            // check args
            String progName = "addCaseList.pl";
            String argSpec = "<study identifier> <case list type>";
            if (this.args.length < 2) {
                // an extra --noprogress option can be given to avoid the messages regarding memory usage and % complete
                throw new UsageException(progName, null, argSpec);
            }

            String cancerStudyIdentifier = args[0];
            String caseListType = args[1];
            if (cancerStudyIdentifier == null) {
                throw new UsageException(
                    progName,
                    null,
                    argSpec,
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

            if (caseListType.equals("all")) {
                //Add "all" case list:
                AddCaseList.addAllCasesList(theCancerStudy);
            }
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
    public AddCaseList(String[] args) {
        super(args);
    }

    /**
     * Runs the command as a script and exits with an appropriate exit code.
     *
     * @param args  the arguments given on the command line
     */
    public static void main(String[] args) {
        ConsoleRunnable runner = new AddCaseList(args);
        runner.runInConsole();
    }
}
