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
import java.util.*;
import joptsimple.*;
import org.dom4j.*;
import org.dom4j.io.SAXReader;
import org.mskcc.cbio.portal.dao.*;
import org.mskcc.cbio.portal.model.*;

/**
 *
 * @author jgao
 */
public class ImportCaisesClinicalXML extends ConsoleRunnable {
    private File xmlFile;
    private int cancerStudyId;

    public ImportCaisesClinicalXML(String[] args) {
        super(args);
    }

    /**
     * Runs the command as a script and exits with an appropriate exit code.
     *
     * @param args  the arguments given on the command line
     */
    public static void main(String[] args) {
        ConsoleRunnable runner = new ImportCaisesClinicalXML(args);
        runner.runInConsole();
    }

    public void run() {
        //        args = new String[] {"--data","/Users/jgao/projects/cbio-portal-data/studies/prad/su2c/data_clinical_caises.xml",
        //            "--meta","/Users/jgao/projects/cbio-portal-data/studies/prad/su2c/meta_clinical_caises.txt",
        //            "--loadMode", "bulkLoad"};
        try {
            String progName = "ImportCaisesClinicalXML";
            String description = "Import clinical Caises XML files";

            OptionParser parser = new OptionParser();
            parser.accepts("noprogress");
            OptionSpec<String> data = parser
                .accepts("data", "caises data file")
                .withRequiredArg()
                .describedAs("data_clinical_caises.xml")
                .ofType(String.class);
            OptionSpec<String> study = parser
                .accepts("study", "cancer study identifier")
                .withRequiredArg()
                .describedAs("study")
                .ofType(String.class);
            parser.acceptsAll(Arrays.asList("dbmsAction", "loadMode"));
            OptionSet options = null;
            try {
                options = parser.parse(args);
                //exitJVM = !options.has(returnFromMain);
            } catch (OptionException e) {
                throw new UsageException(
                    progName,
                    description,
                    parser,
                    e.getMessage()
                );
            }

            String dataFile = null;
            if (options.has(data)) {
                dataFile = options.valueOf(data);
            } else {
                throw new UsageException(
                    progName,
                    description,
                    parser,
                    "'data' argument required"
                );
            }

            String cancerStudyIdentifier = null;
            if (options.has(study)) {
                cancerStudyIdentifier = options.valueOf(study);
            } else {
                throw new UsageException(
                    progName,
                    description,
                    parser,
                    "'study' argument required"
                );
            }

            CancerStudy cancerStudy = DaoCancerStudy.getCancerStudyByStableId(
                cancerStudyIdentifier
            );
            if (cancerStudy == null) {
                throw new RuntimeException(
                    "Unknown cancer study: " + cancerStudyIdentifier
                );
            }

            this.cancerStudyId = cancerStudy.getInternalId();
            DaoClinicalEvent.deleteByCancerStudyId(cancerStudyId);
            this.xmlFile = new File(dataFile);

            importData();

            System.out.println("Done!");
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void setFile(File xmlFile, CancerStudy cancerStudy) {
        this.xmlFile = xmlFile;
        this.cancerStudyId = cancerStudy.getInternalId();
    }

    public void importData() throws Exception {
        MySQLbulkLoader.bulkLoadOn();

        // add unknow attriutes -- this
        for (ClinicalAttribute ca : getClinicalAttributes(cancerStudyId)) {
            if (
                DaoClinicalAttributeMeta.getDatum(
                    ca.getAttrId(),
                    cancerStudyId
                ) ==
                null
            ) {
                DaoClinicalAttributeMeta.addDatum(ca);
            }
        }

        SAXReader reader = new SAXReader();
        Document document = reader.read(xmlFile);

        List<Node> patientNodes = document.selectNodes("//Patients/Patient");

        long clinicalEventId = DaoClinicalEvent.getLargestClinicalEventId();
        CancerStudy cancerStudy = DaoCancerStudy.getCancerStudyByInternalId(
            cancerStudyId
        );

        Map<String, String> mapSu2cSampleIdSampleId = getMapSu2cSampleIdSampleId(
            cancerStudyId
        );

        for (Node patientNode : patientNodes) {
            String patientId = patientNode
                .selectSingleNode("PtProtocolStudyId")
                .getText();
            Patient patient = DaoPatient.getPatientByCancerStudyAndPatientId(
                cancerStudyId,
                patientId
            );
            if (patient == null) {
                continue;
            }

            System.out.println("Importing " + patientId);

            // processing clinical data
            // patient clinical data
            List<ClinicalData> patientClinicalData = parsePatientClinicalData(
                patientNode,
                patientId,
                cancerStudyId
            );
            for (ClinicalData cd : patientClinicalData) {
                if (
                    DaoClinicalData.getDatum(
                        cancerStudy.getCancerStudyStableId(),
                        cd.getStableId(),
                        cd.getAttrId()
                    ) ==
                    null
                ) {
                    DaoClinicalData.addPatientDatum(
                        patient.getInternalId(),
                        cd.getAttrId(),
                        cd.getAttrVal()
                    );
                }
            }
            // sample clinical data
            List<ClinicalData> sampleClinicalData = parseClinicalDataFromSpecimen(
                patientNode,
                cancerStudyId,
                mapSu2cSampleIdSampleId
            );
            for (ClinicalData cd : sampleClinicalData) {
                if (
                    DaoClinicalData.getDatum(
                        cancerStudy.getCancerStudyStableId(),
                        cd.getStableId(),
                        cd.getAttrId()
                    ) ==
                    null
                ) {
                    Sample sample = DaoSample.getSampleByCancerStudyAndSampleId(
                        cancerStudyId,
                        cd.getStableId()
                    );
                    DaoClinicalData.addSampleDatum(
                        sample.getInternalId(),
                        cd.getAttrId(),
                        cd.getAttrVal()
                    );
                }
            }

            // processing timeline data
            List<ClinicalEvent> clinicalEvents = new ArrayList<ClinicalEvent>();
            long diagnositicDate = parseStatusesAndReturnDiagnosisDate(
                clinicalEvents,
                patientNode,
                patientId,
                cancerStudyId
            );
            parseClinicalEventsFromSpecimen(
                clinicalEvents,
                patientNode,
                patientId,
                cancerStudyId
            );
            parseMedicalTherapies(
                clinicalEvents,
                patientNode,
                patientId,
                cancerStudyId
            );
            parseRadiationTherapies(
                clinicalEvents,
                patientNode,
                patientId,
                cancerStudyId
            );
            parseBrachyTherapies(
                clinicalEvents,
                patientNode,
                patientId,
                cancerStudyId
            );
            parseDiagnostics(
                clinicalEvents,
                patientNode,
                patientId,
                cancerStudyId
            );
            parseLabTests(
                clinicalEvents,
                patientNode,
                patientId,
                cancerStudyId
            );
            for (ClinicalEvent clinicalEvent : clinicalEvents) {
                clinicalEvent.setClinicalEventId(++clinicalEventId);
                if (clinicalEvent.getStartDate() != null) {
                    clinicalEvent.setStartDate(
                        clinicalEvent.getStartDate() - diagnositicDate
                    );
                }
                if (clinicalEvent.getStopDate() != null) {
                    clinicalEvent.setStopDate(
                        clinicalEvent.getStopDate() - diagnositicDate
                    );
                }
                DaoClinicalEvent.addClinicalEvent(clinicalEvent);
            }
        }

        MySQLbulkLoader.flushAll();
    }

    private static Map<String, String> getMapSu2cSampleIdSampleId(
        int cancerStudyId
    )
        throws DaoException {
        List<ClinicalData> clinicalData = DaoClinicalData.getDataByAttributeIds(
            cancerStudyId,
            Arrays.asList("SU2C_SAMPLE_ID")
        );
        Map<String, String> map = new HashMap<String, String>();
        for (ClinicalData cd : clinicalData) {
            String su2cSampleId = cd.getAttrVal();
            String sampleId = cd.getStableId();
            if (null != map.put(su2cSampleId, sampleId)) {
                System.err.println(
                    "Something is wrong: there are two samples with the same su2c ID: " +
                    su2cSampleId
                );
            }
        }
        return map;
    }

    private static List<ClinicalAttribute> getClinicalAttributes(
        int cancerStudyId
    ) {
        return Arrays.asList(
            //                new ClinicalAttribute("PATIENT_ID", "Patient ID", "Patient ID", "STRING", true, "1"),
            new ClinicalAttribute(
                "RACE",
                "Race",
                "Race",
                "STRING",
                true,
                "1",
                cancerStudyId
            ),
            new ClinicalAttribute(
                "AGE",
                "Age",
                "Age",
                "Number",
                true,
                "1",
                cancerStudyId
            ),
            new ClinicalAttribute(
                "PATIENT_CATEGORY",
                "Patient category",
                "Patient category",
                "STRING",
                true,
                "1",
                cancerStudyId
            ),
            new ClinicalAttribute(
                "CLIN_T_STAGE",
                "Clinical T stage",
                "Clinical T stage",
                "STRING",
                true,
                "1",
                cancerStudyId
            ),
            new ClinicalAttribute(
                "CLIN_N_STAGE",
                "Clinical N stage",
                "Clinical N stage",
                "STRING",
                true,
                "1",
                cancerStudyId
            ),
            new ClinicalAttribute(
                "CLIN_M_STAGE",
                "Clinical M stage",
                "Clinical M stage",
                "STRING",
                true,
                "1",
                cancerStudyId
            ),
            new ClinicalAttribute(
                "HISTOLOGY",
                "Histology",
                "Histology",
                "STRING",
                true,
                "1",
                cancerStudyId
            ),
            new ClinicalAttribute(
                "PATH_T_STAGE",
                "Pathology T stage",
                "Pathology T stage",
                "STRING",
                true,
                "1",
                cancerStudyId
            ),
            new ClinicalAttribute(
                "PATH_N_STAGE",
                "Pathology N stage",
                "Pathology N stage",
                "STRING",
                true,
                "1",
                cancerStudyId
            ),
            new ClinicalAttribute(
                "PATH_M_STAGE",
                "Pathology M stage",
                "Pathology M stage",
                "STRING",
                true,
                "1",
                cancerStudyId
            ),
            new ClinicalAttribute(
                "GLEASON_SCORE_1",
                "Gleason score 1",
                "Gleason score 1",
                "Number",
                true,
                "1",
                cancerStudyId
            ),
            new ClinicalAttribute(
                "GLEASON_SCORE_2",
                "Gleason score 2",
                "Gleason score 2",
                "Number",
                true,
                "1",
                cancerStudyId
            ),
            new ClinicalAttribute(
                "GLEASON_SCORE",
                "Gleason score",
                "Gleason score",
                "Number",
                true,
                "1",
                cancerStudyId
            ),
            new ClinicalAttribute(
                "TUMOR_SITE",
                "Tumor site",
                "Tumor site",
                "STRING",
                false,
                "1",
                cancerStudyId
            )
        );
    }

    private static List<ClinicalData> parsePatientClinicalData(
        Node patientNode,
        String patientId,
        int cancerStudyId
    ) {
        List<ClinicalData> clinicalData = new ArrayList<ClinicalData>();

        Node node = patientNode.selectSingleNode("PtRace");
        if (node != null) {
            clinicalData.add(
                new ClinicalData(
                    cancerStudyId,
                    patientId,
                    "RACE",
                    node.getText()
                )
            );
        }

        node = patientNode.selectSingleNode("PtRegistrationAge");
        if (node != null) {
            clinicalData.add(
                new ClinicalData(
                    cancerStudyId,
                    patientId,
                    "AGE",
                    node.getText()
                )
            );
        }

        node = patientNode.selectSingleNode("Categories/Category/Category");
        if (node != null) {
            clinicalData.add(
                new ClinicalData(
                    cancerStudyId,
                    patientId,
                    "PATIENT_CATEGORY",
                    node.getText()
                )
            );
        }

        node =
            patientNode.selectSingleNode(
                "ClinicalStages/ClinicalStage/ClinStageT"
            );
        if (node != null) {
            clinicalData.add(
                new ClinicalData(
                    cancerStudyId,
                    patientId,
                    "CLIN_T_STAGE",
                    node.getText()
                )
            );
        }

        node =
            patientNode.selectSingleNode(
                "ClinicalStages/ClinicalStage/ClinStageN"
            );
        if (node != null) {
            clinicalData.add(
                new ClinicalData(
                    cancerStudyId,
                    patientId,
                    "CLIN_N_STAGE",
                    node.getText()
                )
            );
        }

        node =
            patientNode.selectSingleNode(
                "ClinicalStages/ClinicalStage/ClinStageM"
            );
        if (node != null) {
            clinicalData.add(
                new ClinicalData(
                    cancerStudyId,
                    patientId,
                    "CLIN_M_STAGE",
                    node.getText()
                )
            );
        }

        node =
            patientNode.selectSingleNode("Pathologies/Pathology/PathHistology");
        if (node != null) {
            clinicalData.add(
                new ClinicalData(
                    cancerStudyId,
                    patientId,
                    "HISTOLOGY",
                    node.getText()
                )
            );
        }

        node =
            patientNode.selectSingleNode(
                "Pathologies/Pathology/PathologyStageGrades/PathologyStageGrade/PathStageT"
            );
        if (node != null) {
            clinicalData.add(
                new ClinicalData(
                    cancerStudyId,
                    patientId,
                    "PATH_T_STAGE",
                    node.getText()
                )
            );
        }

        node =
            patientNode.selectSingleNode(
                "Pathologies/Pathology/PathologyStageGrades/PathologyStageGrade/PathStageN"
            );
        if (node != null) {
            clinicalData.add(
                new ClinicalData(
                    cancerStudyId,
                    patientId,
                    "PATH_N_STAGE",
                    node.getText()
                )
            );
        }

        node =
            patientNode.selectSingleNode(
                "Pathologies/Pathology/PathologyStageGrades/PathologyStageGrade/PathStageM"
            );
        if (node != null) {
            clinicalData.add(
                new ClinicalData(
                    cancerStudyId,
                    patientId,
                    "PATH_M_STAGE",
                    node.getText()
                )
            );
        }

        node =
            patientNode.selectSingleNode(
                "Pathologies/Pathology/ProstateBiopsyPaths/ProstateBiopsyPath/PathGG1"
            );
        if (node != null) {
            clinicalData.add(
                new ClinicalData(
                    cancerStudyId,
                    patientId,
                    "GLEASON_SCORE_1",
                    node.getText()
                )
            );
        }

        node =
            patientNode.selectSingleNode(
                "Pathologies/Pathology/ProstateBiopsyPaths/ProstateBiopsyPath/PathGG2"
            );
        if (node != null) {
            clinicalData.add(
                new ClinicalData(
                    cancerStudyId,
                    patientId,
                    "GLEASON_SCORE_2",
                    node.getText()
                )
            );
        }

        node =
            patientNode.selectSingleNode(
                "Pathologies/Pathology/ProstateBiopsyPaths/ProstateBiopsyPath/PathGGS"
            );
        if (node != null) {
            clinicalData.add(
                new ClinicalData(
                    cancerStudyId,
                    patientId,
                    "GLEASON_SCORE",
                    node.getText()
                )
            );
        }

        return clinicalData;
    }

    private static void parseMedicalTherapies(
        List<ClinicalEvent> clinicalEvents,
        Node patientNode,
        String patientId,
        int cancerStudyId
    ) {
        List<Node> treatmentNodes = patientNode.selectNodes(
            "MedicalTherapies/MedicalTherapy"
        );

        for (Node treatmentNode : treatmentNodes) {
            Patient patient = DaoPatient.getPatientByCancerStudyAndPatientId(
                cancerStudyId,
                patientId
            );
            ClinicalEvent clinicalEvent = new ClinicalEvent();
            clinicalEvent.setPatientId(patient.getInternalId());
            clinicalEvent.setEventType("TREATMENT");
            clinicalEvent.addEventDatum("TREATMENT_TYPE", "Medical Therapy");

            Node node = treatmentNode.selectSingleNode("MedTxDate");
            if (node == null) {
                System.err.println("no date");
                continue;
            }
            clinicalEvent.setStartDate(Long.parseLong(node.getText()));

            node = treatmentNode.selectSingleNode("MedTxStopDate");
            if (node != null) {
                clinicalEvent.setStopDate(Long.parseLong(node.getText()));
            }

            node = treatmentNode.selectSingleNode("MedTxType");
            if (node != null) {
                clinicalEvent.addEventDatum("SUBTYPE", node.getText());
            }

            node = treatmentNode.selectSingleNode("MedTxIndication");
            if (node != null) {
                clinicalEvent.addEventDatum("INDICATION", node.getText());
            }

            node = treatmentNode.selectSingleNode("MedTxAgent");
            if (node != null) {
                clinicalEvent.addEventDatum("AGENT", node.getText());
            }

            node = treatmentNode.selectSingleNode("MedTxDose");
            if (node != null) {
                clinicalEvent.addEventDatum("DOSE", node.getText());
            }

            node = treatmentNode.selectSingleNode("MedTxTotalDose");
            if (node != null) {
                clinicalEvent.addEventDatum("TOTAL_DOSE", node.getText());
            }

            node = treatmentNode.selectSingleNode("MedTxUnits");
            if (node != null) {
                clinicalEvent.addEventDatum("UNIT", node.getText());
            }

            node = treatmentNode.selectSingleNode("MedTxSchedule");
            if (node != null) {
                clinicalEvent.addEventDatum("SCHEDULE", node.getText());
            }

            node = treatmentNode.selectSingleNode("MedTxRoute");
            if (node != null) {
                clinicalEvent.addEventDatum("ROUTE", node.getText());
            }

            clinicalEvents.add(clinicalEvent);
        }
    }

    private static void parseRadiationTherapies(
        List<ClinicalEvent> clinicalEvents,
        Node patientNode,
        String patientId,
        int cancerStudyId
    ) {
        List<Node> treatmentNodes = patientNode.selectNodes(
            "RadiationTherapies/RadiationTherapy"
        );

        for (Node treatmentNode : treatmentNodes) {
            Patient patient = DaoPatient.getPatientByCancerStudyAndPatientId(
                cancerStudyId,
                patientId
            );
            ClinicalEvent clinicalEvent = new ClinicalEvent();
            clinicalEvent.setPatientId(patient.getInternalId());
            clinicalEvent.setEventType("TREATMENT");
            clinicalEvent.addEventDatum("TREATMENT_TYPE", "Radiation Therapy");

            Node node = treatmentNode.selectSingleNode("RadTxDate");
            if (node == null) {
                System.err.println("no date");
                continue;
            }
            clinicalEvent.setStartDate(Long.parseLong(node.getText()));

            node = treatmentNode.selectSingleNode("RadTxStopDate");
            if (node != null) {
                clinicalEvent.setStopDate(Long.parseLong(node.getText()));
            }

            node = treatmentNode.selectSingleNode("RadTxType");
            if (node != null) {
                clinicalEvent.addEventDatum("SUBTYPE", node.getText());
            }

            node = treatmentNode.selectSingleNode("RadTxIndication");
            if (node != null) {
                clinicalEvent.addEventDatum("INDICATION", node.getText());
            }

            node = treatmentNode.selectSingleNode("RadTxIntent");
            if (node != null) {
                clinicalEvent.addEventDatum("INTENT", node.getText());
            }

            node = treatmentNode.selectSingleNode("RadTxDosePerFraction");
            if (node != null) {
                clinicalEvent.addEventDatum(
                    "DOSE_PER_FRACTION",
                    node.getText()
                );
            }

            node = treatmentNode.selectSingleNode("RadTxTotalDose");
            if (node != null) {
                clinicalEvent.addEventDatum("TOTAL_DOSE", node.getText());
            }

            node = treatmentNode.selectSingleNode("RadTxUnits");
            if (node != null) {
                clinicalEvent.addEventDatum("UNIT", node.getText());
            }

            node = treatmentNode.selectSingleNode("RadTxNumFractions");
            if (node != null) {
                clinicalEvent.addEventDatum("NUM_FRACTIONS", node.getText());
            }

            node = treatmentNode.selectSingleNode("RadTxTarget");
            if (node != null) {
                clinicalEvent.addEventDatum("TARGET", node.getText());
            }

            clinicalEvents.add(clinicalEvent);
        }
    }

    private static void parseBrachyTherapies(
        List<ClinicalEvent> clinicalEvents,
        Node patientNode,
        String patientId,
        int cancerStudyId
    ) {
        List<Node> treatmentNodes = patientNode.selectNodes(
            "BrachyTherapies/BrachyTherapy"
        );

        for (Node treatmentNode : treatmentNodes) {
            Patient patient = DaoPatient.getPatientByCancerStudyAndPatientId(
                cancerStudyId,
                patientId
            );
            ClinicalEvent clinicalEvent = new ClinicalEvent();
            clinicalEvent.setPatientId(patient.getInternalId());
            clinicalEvent.setEventType("TREATMENT");
            clinicalEvent.addEventDatum("TREATMENT_TYPE", "Brachytherapy");

            Node node = treatmentNode.selectSingleNode("BrachyDate");
            if (node == null) {
                System.err.println("no date");
                continue;
            }
            clinicalEvent.setStartDate(Long.parseLong(node.getText()));

            node = treatmentNode.selectSingleNode("BrachyIsotope");
            if (node != null) {
                clinicalEvent.addEventDatum("BRACHY_ISOTOPE", node.getText());
            }

            node = treatmentNode.selectSingleNode("BrachyPrescribedDose");
            if (node != null) {
                clinicalEvent.addEventDatum("DOSE", node.getText());
            }

            node = treatmentNode.selectSingleNode("BrachyDoseNotes");
            if (node != null) {
                clinicalEvent.addEventDatum("DOSE_NOTES", node.getText());
            }

            clinicalEvents.add(clinicalEvent);
        }
    }

    private static void parseDiagnostics(
        List<ClinicalEvent> clinicalEvents,
        Node patientNode,
        String patientId,
        int cancerStudyId
    ) {
        List<Node> diagnosticNodes = patientNode.selectNodes(
            "Diagnostics/Diagnostic"
        );
        for (Node diagnosticNode : diagnosticNodes) {
            Patient patient = DaoPatient.getPatientByCancerStudyAndPatientId(
                cancerStudyId,
                patientId
            );
            ClinicalEvent clinicalEvent = new ClinicalEvent();
            clinicalEvent.setPatientId(patient.getInternalId());
            clinicalEvent.setEventType("DIAGNOSTIC");

            Node node = diagnosticNode.selectSingleNode("DxDate");
            if (node == null) {
                System.err.println("no date");
                continue;
            }
            clinicalEvent.setStartDate(Long.parseLong(node.getText()));

            node = diagnosticNode.selectSingleNode("DxType");
            if (node != null) {
                clinicalEvent.addEventDatum("DIAGNOSTIC_TYPE", node.getText());
            }

            node = diagnosticNode.selectSingleNode("DxTarget");
            if (node != null) {
                clinicalEvent.addEventDatum("TARGET", node.getText());
            }

            node = diagnosticNode.selectSingleNode("DxResult");
            if (node != null) {
                clinicalEvent.addEventDatum("RESULT", node.getText());
            }

            node = diagnosticNode.selectSingleNode("DxNotes");
            if (node != null) {
                clinicalEvent.addEventDatum("NOTES", node.getText());
            }

            node = diagnosticNode.selectSingleNode("DxSide");
            if (node != null) {
                clinicalEvent.addEventDatum("SIDE", node.getText());
            }

            node = diagnosticNode.selectSingleNode("DxStatus");
            if (node != null) {
                clinicalEvent.addEventDatum("STATUS", node.getText());
            }

            node = diagnosticNode.selectSingleNode("ImgBaseline");
            if (node != null) {
                clinicalEvent.addEventDatum("BASELINE", node.getText());
            }

            node = diagnosticNode.selectSingleNode("DxNumNewTumors");
            if (node != null) {
                clinicalEvent.addEventDatum("NUM_NEW_TUMORS", node.getText());
            }

            clinicalEvents.add(clinicalEvent);
        }
    }

    private static void parseLabTests(
        List<ClinicalEvent> clinicalEvents,
        Node patientNode,
        String patientId,
        int cancerStudyId
    ) {
        List<Node> labTestNodes = patientNode.selectNodes("LabTests/LabTest");
        for (Node labTestNode : labTestNodes) {
            Patient patient = DaoPatient.getPatientByCancerStudyAndPatientId(
                cancerStudyId,
                patientId
            );
            ClinicalEvent clinicalEvent = new ClinicalEvent();
            clinicalEvent.setPatientId(patient.getInternalId());
            clinicalEvent.setEventType("LAB_TEST");

            Node node = labTestNode.selectSingleNode("LabDate");
            if (node == null) {
                System.err.println("no date");
                continue;
            }
            clinicalEvent.setStartDate(Long.parseLong(node.getText()));

            node = labTestNode.selectSingleNode("LabTest");
            if (node == null) {
                System.err.println("no lab test name");
                continue;
            }
            clinicalEvent.addEventDatum("TEST", node.getText());

            node = labTestNode.selectSingleNode("LabResult");
            if (node == null) {
                System.err.println("no lab result");
                continue;
            }
            clinicalEvent.addEventDatum("RESULT", node.getText());

            node = labTestNode.selectSingleNode("LabUnits");
            if (node != null) {
                clinicalEvent.addEventDatum("UNIT", node.getText());
            }

            node = labTestNode.selectSingleNode("LabNormalRange");
            if (node != null) {
                clinicalEvent.addEventDatum("NORMAL_RANGE", node.getText());
            }

            node = labTestNode.selectSingleNode("LabNotes");
            if (node != null) {
                clinicalEvent.addEventDatum("NOTES", node.getText());
            }

            clinicalEvents.add(clinicalEvent);
        }
    }

    private static void parseClinicalEventsFromSpecimen(
        List<ClinicalEvent> clinicalEvents,
        Node patientNode,
        String patientId,
        int cancerStudyId
    ) {
        List<Node> specimenAccessionNodes = patientNode.selectNodes(
            "SpecimenAccessions/SpecimenAccession"
        );
        for (Node specimenAccessionNode : specimenAccessionNodes) {
            Node node = specimenAccessionNode.selectSingleNode("AccessionDate");
            if (node == null) {
                System.err.println("no date");
                continue;
            }
            long date = Long.parseLong(node.getText());

            String site = null, type = null, instrument = null;
            node =
                specimenAccessionNode.selectSingleNode("AccessionAnatomicSite");
            if (node != null) {
                site = node.getText();
            }
            node = specimenAccessionNode.selectSingleNode("AccessionVisitType");
            if (node != null) {
                type = node.getText();
            }
            node =
                specimenAccessionNode.selectSingleNode(
                    "AccessionProcInstrument"
                );
            if (node != null) {
                instrument = node.getText();
            }

            List<Node> specimenNodes = specimenAccessionNode.selectNodes(
                "Specimens/Specimen"
            );
            for (Node specimenNode : specimenNodes) {
                Patient patient = DaoPatient.getPatientByCancerStudyAndPatientId(
                    cancerStudyId,
                    patientId
                );

                ClinicalEvent clinicalEvent = new ClinicalEvent();
                clinicalEvent.setPatientId(patient.getInternalId());
                clinicalEvent.setEventType("SPECIMEN");
                clinicalEvent.setStartDate(date);
                if (site != null) {
                    clinicalEvent.addEventDatum("SPECIMEN_SITE", site);
                }
                if (type != null) {
                    clinicalEvent.addEventDatum("ANATOMIC_SITE", type);
                }
                if (instrument != null) {
                    clinicalEvent.addEventDatum("PROC_INSTRUMENT", instrument);
                }

                addAllDataUnderNode(
                    clinicalEvent,
                    Element.class.cast(specimenNode)
                );

                clinicalEvents.add(clinicalEvent);
            }
        }
    }

    private static List<ClinicalData> parseClinicalDataFromSpecimen(
        Node patientNode,
        int cancerStudyId,
        Map<String, String> mapSu2cSampleIdSampleId
    ) {
        List<ClinicalData> clinicalData = new ArrayList<ClinicalData>();
        List<Node> specimenAccessionNodes = patientNode.selectNodes(
            "SpecimenAccessions/SpecimenAccession"
        );
        for (Node specimenAccessionNode : specimenAccessionNodes) {
            String site = null, instrument = null;
            Node node = specimenAccessionNode.selectSingleNode(
                "AccessionAnatomicSite"
            );
            if (node != null) {
                site = node.getText();
            }
            node =
                specimenAccessionNode.selectSingleNode(
                    "AccessionProcInstrument"
                );
            if (node != null) {
                instrument = node.getText();
            }

            List<Node> specimenNodes = specimenAccessionNode.selectNodes(
                "Specimens/Specimen"
            );
            for (Node specimenNode : specimenNodes) {
                node = specimenNode.selectSingleNode("SpecimenReferenceNumber");
                if (node == null) {
                    continue;
                }
                String su2cSampleId = node.getText();
                String sampleId = mapSu2cSampleIdSampleId.get(su2cSampleId);
                if (sampleId == null) {
                    continue;
                }

                if (site != null) {
                    ClinicalData clinicalDatum = new ClinicalData(
                        cancerStudyId,
                        sampleId,
                        "TUMOR_SITE",
                        site
                    );
                    clinicalData.add(clinicalDatum);
                }

                if (instrument != null) {
                    ClinicalData clinicalDatum = new ClinicalData(
                        cancerStudyId,
                        sampleId,
                        "PROC_INSTRUMENT",
                        instrument
                    );
                    clinicalData.add(clinicalDatum);
                }
            }
        }
        return clinicalData;
    }

    private static long parseStatusesAndReturnDiagnosisDate(
        List<ClinicalEvent> clinicalEvents,
        Node patientNode,
        String patientId,
        int cancerStudyId
    ) {
        List<Node> statusNodes = patientNode.selectNodes("Statuses/Status");
        long diagnosisDate = 0;
        for (Node statusNode : statusNodes) {
            Patient patient = DaoPatient.getPatientByCancerStudyAndPatientId(
                cancerStudyId,
                patientId
            );
            ClinicalEvent clinicalEvent = new ClinicalEvent();
            clinicalEvent.setPatientId(patient.getInternalId());
            clinicalEvent.setEventType("STATUS");

            Node node = statusNode.selectSingleNode("StatusDate");
            if (node == null) {
                System.err.println("no date");
                continue;
            }
            long statusDate = Long.parseLong(node.getText());
            clinicalEvent.setStartDate(statusDate);

            node = statusNode.selectSingleNode("Status");
            if (node == null) {
                System.err.println("no status");
                continue;
            }
            clinicalEvent.addEventDatum("STATUS", node.getText());
            if (node.getText().equalsIgnoreCase("Diagnosis Date")) {
                diagnosisDate = statusDate;
            }

            clinicalEvents.add(clinicalEvent);
        }
        return diagnosisDate;
    }

    private static void addAllDataUnderNode(
        ClinicalEvent clinicalEvent,
        Element element
    ) {
        for (Iterator i = element.elementIterator(); i.hasNext();) {
            Element child = (Element) i.next();
            clinicalEvent.addEventDatum(child.getName(), child.getTextTrim());
        }
    }
}
