/*
 * SPDX-License-Identifier: Apache-2.0
 */

package org.mskcc.cbio.portal.scripts;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;
import joptsimple.OptionException;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;
import org.mskcc.cbio.portal.dao.DaoCancerStudy;
import org.mskcc.cbio.portal.dao.DaoException;
import org.mskcc.cbio.portal.dao.DaoSampleList;
import org.mskcc.cbio.portal.model.CancerStudy;
import org.mskcc.cbio.portal.model.SampleList;
import org.mskcc.cbio.portal.util.CaseList;
import org.mskcc.cbio.portal.util.CaseListReader;
import org.mskcc.cbio.portal.util.ProgressMonitor;
import org.mskcc.cbio.portal.validate.CaseListValidator;

public class UpdateCaseListsSampleIds extends ConsoleRunnable {

    private File metaFile;
    private File dataFile;
    private List<File> caseListFiles = List.of();
    private String cancerStudyStableId;
    private final Map<String, Set<String>> caseListSampleIdToSampleIds = new LinkedHashMap<>();
    private final DaoSampleList daoSampleList = new DaoSampleList();
    private LinkedHashSet<String> allSampleIds;

    public UpdateCaseListsSampleIds(String[] args) {
        super(args);
    }

    /**
     * Updates case list sample ids from clinical sample and case list files
     */
    public void run() {
        parseArguments();
        readStudyIdAndDataFileFromMetaFile();
        this.allSampleIds = readSampleIdsFromDataFile(this.dataFile);
        this.caseListSampleIdToSampleIds.put(cancerStudyStableId + "_all", this.allSampleIds);
        Map<String, Set<String>> readCaseListSampleIds = readCaseListFiles();
        this.caseListSampleIdToSampleIds.putAll(readCaseListSampleIds);
        updateCaseListsForTheStudy(this.caseListSampleIdToSampleIds);
    }

    private Map<String, Set<String>> readCaseListFiles() {
        LinkedHashMap<String, Set<String>> result = new LinkedHashMap<>();
        for (File caseListFile : this.caseListFiles) {
            CaseList caseList = CaseListReader.readFile(caseListFile);
            CaseListValidator.validateIdFields(caseList);
            String cancerStudyIdentifier = caseList.getCancerStudyIdentifier();
            if (!cancerStudyIdentifier.equals(this.cancerStudyStableId)) {
                ProgressMonitor.logWarning(
                        String.format(
                                "Skipping %s case list file as it belongs to %s study and we uploading %s study.",
                                caseListFile, cancerStudyIdentifier, this.cancerStudyStableId));
                continue;
            }
            LinkedHashSet<String> extraSampleIds = new LinkedHashSet<>(caseList.getSampleIds());
            extraSampleIds.removeAll(this.allSampleIds);
            if (!extraSampleIds.isEmpty()) {
                throw new RuntimeException(caseListFile.getAbsolutePath() + ": The following sample ids present in the case list file, but not specified in the clinical sample file: " + String.join(", ", extraSampleIds));
            }
            result.put(caseList.getStableId(), new LinkedHashSet<>(caseList.getSampleIds()));
        }
        return result;
    }

    /**
     * Updates the sample lists according to the steps below:
     *
     * 1. New sample IDs provided in the `caseListSampleIdToSampleIds` map are added to their corresponding case lists.
     * 2. These sample IDs are removed from any other case lists within the same study.
     *
     * @param caseListSampleIdToSampleIds A map where the key is the case list stable ID and the value is a set of sample IDs
     *                                    to be added to the corresponding case list.
     *                                    Note: This map only includes the case lists that need to be updated with new sample IDs.
     *                                    Existing case lists in the study that are not in the map will not be dropped,
     *                                    but the provided sample IDs will be removed from these lists if present.
     * @throws RuntimeException if any DAO operations fail or if a case list with a specified stable ID is not found.
     */
    private void updateCaseListsForTheStudy(Map<String, Set<String>> caseListSampleIdToSampleIds) {
        DaoCancerStudy.reCacheAll();
        try {
            for (Map.Entry<String, Set<String>> caseListStableIdToSampleIds : caseListSampleIdToSampleIds.entrySet()) {
                String caseListStableId = caseListStableIdToSampleIds.getKey();
                Set<String> uploadedSampleIds = caseListStableIdToSampleIds.getValue();
                SampleList sampleList = daoSampleList.getSampleListByStableId(caseListStableId);
                if (sampleList == null) {
                    throw new RuntimeException("No case list with " + caseListStableId + " stable id is found");
                }
                LinkedHashSet<String> newCaseListSampleIds = new LinkedHashSet<>(sampleList.getSampleList());
                if (newCaseListSampleIds.addAll(uploadedSampleIds)) {
                    sampleList.setSampleList(new ArrayList<>(newCaseListSampleIds));
                    daoSampleList.updateSampleListList(sampleList);
                }
            }
            CancerStudy cancerStudy = DaoCancerStudy.getCancerStudyByStableId(this.cancerStudyStableId);
            List<SampleList> sampleLists = daoSampleList.getAllSampleLists(cancerStudy.getInternalId());
            List<SampleList> remainingLists = sampleLists.stream().filter(sl ->
                    !caseListSampleIdToSampleIds.containsKey(sl.getStableId()) && sl.getSampleList().stream().anyMatch(this.allSampleIds::contains)
            ).toList();
            for (SampleList remainingList : remainingLists) {
                ArrayList<String> newSampleList = new ArrayList<>(remainingList.getSampleList());
                if (newSampleList.removeAll(this.allSampleIds)) {
                    remainingList.setSampleList(newSampleList);
                    daoSampleList.updateSampleListList(remainingList);
                }
            }
        } catch (DaoException e) {
            throw new RuntimeException(e);
        }
    }

    private LinkedHashSet<String> readSampleIdsFromDataFile(File dataFile) {
        LinkedHashSet<String> allSampleIds = new LinkedHashSet<>();
        try (FileReader reader = new FileReader(dataFile);
             BufferedReader buff = new BufferedReader(reader)) {
            String line;
            int sampleIdPosition = -1;
            while ((line = buff.readLine()) != null) {
                String trimmedLine = line.trim();
                if (trimmedLine.isEmpty() || trimmedLine.startsWith("#")) {
                    continue;
                }

                String[] fieldValues = line.split("\t");
                if (sampleIdPosition == -1) {
                    sampleIdPosition = List.of(fieldValues).indexOf("SAMPLE_ID");
                    if (sampleIdPosition == -1) {
                        throw new RuntimeException("No SAMPLE_ID header is found");
                    }
                } else {
                    allSampleIds.add(fieldValues[sampleIdPosition].trim());
                }
            }
            return allSampleIds;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void readStudyIdAndDataFileFromMetaFile() {
        TrimmedProperties properties = new TrimmedProperties();
        try {
            FileInputStream inStream = new FileInputStream(this.metaFile);
            properties.load(inStream);
            this.cancerStudyStableId = properties.getProperty("cancer_study_identifier");
            String dataFilename = properties.getProperty("data_filename");
            this.dataFile = new File(metaFile.getParent(), dataFilename);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void parseArguments() {
        String progName = getClass().getName();
        String description = "Updates (adds/removes) sample ids in specified case lists.";

        OptionParser parser = new OptionParser();
        OptionSpec<String> metaOpt = parser.accepts("meta",
                "clinical sample (genetic_alteration_type=CLINICAL and datatype=SAMPLE_ATTRIBUTES or datatype=MIXED_ATTRIBUTES) meta data file. All sample ids found in the file will be added to the _all case list.").withRequiredArg().required().describedAs("meta_clinical_sample.txt").ofType(String.class);
        OptionSpec<String> caseListDirOrFileOpt = parser.accepts("case-lists",
                "case list file or a directory with case list files").withRequiredArg().describedAs("case_lists/").ofType(String.class);

        try {
            OptionSet options = parser.parse(args);
            this.metaFile = new File(options.valueOf(metaOpt));
            if (options.has(caseListDirOrFileOpt)) {
                File caseListDirOrFile = new File(options.valueOf(caseListDirOrFileOpt));
                if (caseListDirOrFile.isDirectory()) {
                    this.caseListFiles = Arrays.stream(Objects.requireNonNull(caseListDirOrFile.listFiles()))
                            .filter(file -> !file.getName().startsWith(".") && !file.getName().endsWith("~")).collect(Collectors.toList());
                } else if (caseListDirOrFile.isFile()) {
                    this.caseListFiles = List.of(caseListDirOrFile);
                } else {
                    throw new RuntimeException("No file " + caseListDirOrFile.getAbsolutePath() + " exists");
                }
            }
        } catch (OptionException e) {
            throw new UsageException(
                    progName, description, parser,
                    e.getMessage());
        }
    }

    /**
     * Runs the command as a script and exits with an appropriate exit code.
     *
     * @param args the arguments given on the command line
     */
    public static void main(String[] args) {
        ConsoleRunnable runner = new UpdateCaseListsSampleIds(args);
        runner.runInConsole();
    }
}
