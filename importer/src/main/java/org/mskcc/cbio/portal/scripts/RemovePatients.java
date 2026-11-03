/*
 * SPDX-License-Identifier: Apache-2.0
 */

package org.mskcc.cbio.portal.scripts;

import java.util.*;
import java.util.stream.Collectors;
import joptsimple.OptionException;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;
import org.mskcc.cbio.portal.dao.DaoCancerStudy;
import org.mskcc.cbio.portal.dao.DaoException;
import org.mskcc.cbio.portal.dao.DaoPatient;
import org.mskcc.cbio.portal.model.CancerStudy;
import org.mskcc.cbio.portal.util.ProgressMonitor;

/**
 * Command Line Tool to Remove Patients in Cancer Studies
 */
public class RemovePatients extends ConsoleRunnable {

    public static final String COMMA = ",";
    private Set<String> studyIds;
    private Set<String> patientIds;

    public void run() {
        ProgressMonitor.setCurrentMessage("Start removing patient(s) from study(ies).");
        parseArgs();
        ProgressMonitor.logDebug("Reading study id(s) from the database.");
        final Set<CancerStudy> cancerStudies = studyIds.stream().map(studyId -> {
            try {
                CancerStudy cancerStudy = DaoCancerStudy.getCancerStudyByStableId(studyId);
                if (cancerStudy == null) {
                    throw new NoSuchElementException("Cancer study with stable id=" + studyId + " not found.");
                }
                return cancerStudy;
            } catch (DaoException e) {
                throw new RuntimeException(e);
            }
        }).collect(Collectors.toUnmodifiableSet());
        try {
            for (CancerStudy cancerStudy : cancerStudies) {
                ProgressMonitor.setCurrentMessage("Removing patient with stable id(s) ("
                        + String.join(", ", patientIds)
                        + ") from study with stable id=" + cancerStudy.getCancerStudyStableId() + " ...");
                DaoPatient.deletePatients(cancerStudy.getInternalId(), patientIds);
            }
        } catch (DaoException e) {
            throw new RuntimeException(e);
        }
        ProgressMonitor.setCurrentMessage("Done removing patient(s) from study(ies).");
    }

    private void parseArgs() {
        OptionParser parser = new OptionParser();
        OptionSpec<String> studyIdsOpt = parser.accepts("study_ids", "Cancer Study ID(s; comma separated) to remove patients for.")
                .withRequiredArg()
                .describedAs("comma separated study ids")
                .ofType(String.class);
        OptionSpec<String> patientIdsOpt = parser.accepts("patient_ids", "Patients Stable ID(s; comma separated) to remove.")
                .withRequiredArg()
                .describedAs("comma separated patient ids")
                .ofType(String.class);
        OptionSpec<Void> help = parser.accepts("help", "print this help info");
        String progName = this.getClass().getSimpleName();
        String description = "Removes clinical patient(s) information by their stable id(s) and cancer study id(s).";

        OptionSet options;
        try {
            options = parser.parse(args);
        } catch (OptionException e) {
            throw new UsageException(progName, description, parser,
                    e.getMessage());
        }

        if (options.has(help)) {
            throw new UsageException(progName, description, parser);
        }
        if (!options.has(studyIdsOpt) || options.valueOf(studyIdsOpt) == null || "".equals(options.valueOf(studyIdsOpt).trim())) {
            throw new UsageException(progName, description, parser, "'--study_ids' argument has to specify study id(s).");
        }
        if (!options.has(patientIdsOpt) || options.valueOf(patientIdsOpt) == null || "".equals(options.valueOf(patientIdsOpt).trim())) {
            throw new UsageException(progName, description, parser, "'--patient_ids' argument has to specify patient id(s).");
        }
        this.studyIds = parseCsvAsSet(options.valueOf(studyIdsOpt));
        this.patientIds = parseCsvAsSet(options.valueOf(patientIdsOpt));
    }

    private Set<String> parseCsvAsSet(String s) {
        return Arrays.stream(s.trim().split(COMMA)).filter(val -> !"".equals(val)).collect(Collectors.toUnmodifiableSet());
    }

    /**
     * Makes an instance to run with the given command line arguments.
     *
     * @param args the command line arguments to be used
     */
    public RemovePatients(String[] args) {
        super(args);
    }

    /**
     * Runs the command as a script and exits with an appropriate exit code.
     *
     * @param args the arguments given on the command line
     */
    public static void main(String[] args) {
        ConsoleRunnable runner = new RemovePatients(args);
        runner.runInConsole();
    }
}
