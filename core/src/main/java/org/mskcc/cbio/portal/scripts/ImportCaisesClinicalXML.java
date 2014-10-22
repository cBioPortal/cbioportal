/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mskcc.cbio.portal.scripts;

import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import joptsimple.OptionException;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;
import org.mskcc.cbio.portal.dao.DaoCancerStudy;
import org.mskcc.cbio.portal.dao.DaoClinicalAttribute;
import org.mskcc.cbio.portal.dao.DaoClinicalData;
import org.mskcc.cbio.portal.dao.DaoClinicalEvent;
import org.mskcc.cbio.portal.dao.DaoException;
import org.mskcc.cbio.portal.dao.MySQLbulkLoader;
import org.mskcc.cbio.portal.model.CancerStudy;
import org.mskcc.cbio.portal.model.ClinicalAttribute;
import org.mskcc.cbio.portal.model.ClinicalData;
import org.mskcc.cbio.portal.model.ClinicalEvent;

/**
 *
 * @author jgao
 */
public final class ImportCaisesClinicalXML {
    
    private ImportCaisesClinicalXML() {}
    
    public static void main(String[] args) throws Exception {
//        args = new String[] {"--data","/Users/jgao/projects/cbio-portal-data/studies/prad/su2c/data_clinical_caises.xml",
//            "--meta","/Users/jgao/projects/cbio-portal-data/studies/prad/su2c/meta_clinical_caises.txt",
//            "--loadMode", "bulkLoad"};
        if (args.length < 4) {
            System.out.println("command line usage:  importCaisesXml --data <data_clinical_caises.xml> --meta <meta_clinical_caises.txt>");
            return;
        }
        
       OptionParser parser = new OptionParser();
       OptionSpec<String> data = parser.accepts( "data",
               "caises data file" ).withRequiredArg().describedAs( "data_clinical_caises.xml" ).ofType( String.class );
       OptionSpec<String> meta = parser.accepts( "meta",
               "meta (description) file" ).withRequiredArg().describedAs( "meta_clinical_caises.txt" ).ofType( String.class );
       parser.acceptsAll(Arrays.asList("dbmsAction", "loadMode"));
       OptionSet options = null;
      try {
         options = parser.parse( args );
         //exitJVM = !options.has(returnFromMain);
      } catch (OptionException e) {
          e.printStackTrace();
      }
       
       String dataFile = null;
       if( options.has( data ) ){
          dataFile = options.valueOf( data );
       }else{
           throw new Exception( "'data' argument required.");
       }

       String descriptorFile = null;
       if( options.has( meta ) ){
          descriptorFile = options.valueOf( meta );
       }else{
           throw new Exception( "'meta' argument required.");
       }
        
        Properties properties = new Properties();
        properties.load(new FileInputStream(descriptorFile));
      
        CancerStudy cancerStudy = DaoCancerStudy.getCancerStudyByStableId(properties.getProperty("cancer_study_identifier"));
        if (cancerStudy == null) {
            throw new Exception("Unknown cancer study: " + properties.getProperty("cancer_study_identifier"));
        }
        
        int cancerStudyId = cancerStudy.getInternalId();
        DaoClinicalEvent.deleteByCancerStudyId(cancerStudyId);
        
        importData(dataFile, cancerStudy.getInternalId());

        System.out.println("Done!");
    }
    
//    private static Map<String,String> readSampleIDMapping(String clinicalDataFile) throws IOException {
//        FileReader reader =  new FileReader(clinicalDataFile);
//        BufferedReader buff = new BufferedReader(reader);
//
//        Map<String,String> map = new HashMap<String,String>();
//        
//        String line = buff.readLine(); 
//        while (line.startsWith("#")) {
//            line = buff.readLine();
//        }
//        
//        Map<String,Integer> mapHeaderIndex = new HashMap<String,Integer>();
//        String[] headers = line.split("\t");
//        for (int i=0; i<headers.length; i++) {
//            String header = headers[i];
//            mapHeaderIndex.put(header, i);
//        }
//        
//        int ixSampleId = mapHeaderIndex.get("CASE_ID");
//        int ixSu2cSampleId = mapHeaderIndex.get("SU2C_SAMPLE_ID");
//        
//        while ((line = buff.readLine()) != null) {
//            String[] parts = line.split("\t");
//            if (!parts[1].isEmpty()) {
//                map.put(parts[ixSampleId], parts[ixSu2cSampleId]);
//            }
//        }
//        return map;
//    }
    
    private static void importData(String urlXml, int cancerStudyId) throws Exception {
        MySQLbulkLoader.bulkLoadOn();
        
        SAXReader reader = new SAXReader();
        Document document = reader.read(urlXml);
        
        List<Node> patientNodes = document.selectNodes("//Patients/Patient");
        
        long clinicalEventId = DaoClinicalEvent.getLargestClinicalEventId();
        
        Map<String, Set<String>> mapPatientIdSampleId = getMapPatientIdSampleId(cancerStudyId);
        Map<String, Set<String>> mapSu2cSampleIdSampleId = getMapSu2cSampleIdSampleId(cancerStudyId);
        
        if (mapPatientIdSampleId.isEmpty()) {
            throw new Exception("clinical data need to be imported first");
        }
        
        for (Node patientNode : patientNodes) {
            String patientId = patientNode.selectSingleNode("PtProtocolStudyId").getText();
            
            System.out.println("Importing "+patientId);

            // processing clinical data
            List<ClinicalData> clinicalData = filterClinicalData(
                    parsePatientClinicalData(patientNode, patientId, cancerStudyId),
                    mapPatientIdSampleId);
            clinicalData.addAll(filterClinicalData(
                    parseClinicalDataFromSpecimen(patientNode, cancerStudyId),
                    mapSu2cSampleIdSampleId));
            for (ClinicalData cd : clinicalData) {
                if (DaoClinicalData.getDatum(cancerStudyId, cd.getCaseId(), cd.getAttrId())==null) {
                    DaoClinicalData.addDatum(cd);
                }
            }
            // add unknow attriutes -- this 
            for (ClinicalAttribute ca : getClinicalAttributes()) {
                if (DaoClinicalAttribute.getDatum(ca.getAttrId())==null) {
                    DaoClinicalAttribute.addDatum(ca);
                }
            }
            
            // processing timeline data
            List<ClinicalEvent> clinicalEvents = new ArrayList<ClinicalEvent>();
            long diagnositicDate = parseStatusesAndReturnDiagnosisDate(clinicalEvents, patientNode, patientId, cancerStudyId);
            parseClinicalEventsFromSpecimen(clinicalEvents, patientNode, patientId, cancerStudyId);
            parseMedicalTherapies(clinicalEvents, patientNode, patientId, cancerStudyId);
            parseRadiationTherapies(clinicalEvents, patientNode, patientId, cancerStudyId);
            parseBrachyTherapies(clinicalEvents, patientNode, patientId, cancerStudyId);
            parseDiagnostics(clinicalEvents, patientNode, patientId, cancerStudyId);
            parseLabTests(clinicalEvents, patientNode, patientId, cancerStudyId);
            for (ClinicalEvent clinicalEvent : clinicalEvents) {
                clinicalEvent.setClinicalEventId(++clinicalEventId);
                if (clinicalEvent.getStartDate()!=null) {
                    clinicalEvent.setStartDate(clinicalEvent.getStartDate()-diagnositicDate);
                }
                if (clinicalEvent.getStopDate()!=null) {
                    clinicalEvent.setStopDate(clinicalEvent.getStopDate()-diagnositicDate);
                }
                DaoClinicalEvent.addClinicalEvent(clinicalEvent);
            }
        }
        
        MySQLbulkLoader.flushAll();
    }
    
    private static List<ClinicalData> filterClinicalData(List<ClinicalData> clinicalData,
            Map<String, Set<String>> mapPatientIdSampleId) throws DaoException {
        List<ClinicalData> filteredData = new ArrayList<ClinicalData>();
        for (ClinicalData cd : clinicalData) {
            String patientId = cd.getCaseId();
            Set<String> sampleIds = mapPatientIdSampleId.get(patientId);
            if (sampleIds!=null) {
                for (String sampleId : sampleIds) {
                    ClinicalData newCD = new ClinicalData(cd);
                    newCD.setCaseId(sampleId);
                    filteredData.add(newCD);
                }
            }
        }
        
        return filteredData;
    }
    
    private static Map<String, Set<String>> getMapPatientIdSampleId(int cancerStudyId) throws DaoException {
        List<ClinicalData> clinicalData = DaoClinicalData.getDataByAttributeIds(cancerStudyId, Arrays.asList("PATIENT_ID"));
        Map<String, Set<String>> map = new HashMap<String, Set<String>>();
        for (ClinicalData cd : clinicalData) {
            String patientId = cd.getAttrVal();
            String sampleId = cd.getCaseId();
            Set<String> sampleIds = map.get(patientId);
            if (sampleIds==null) {
                sampleIds = new HashSet<String>();
                map.put(patientId, sampleIds);
            }
            sampleIds.add(sampleId);
        }
        return map;
    }
    
    private static Map<String, Set<String>> getMapSu2cSampleIdSampleId(int cancerStudyId) throws DaoException {
        List<ClinicalData> clinicalData = DaoClinicalData.getDataByAttributeIds(cancerStudyId, Arrays.asList("SU2C_SAMPLE_ID"));
        Map<String, Set<String>> map = new HashMap<String, Set<String>>();
        for (ClinicalData cd : clinicalData) {
            String su2cSampleId = cd.getAttrVal();
            String sampleId = cd.getCaseId();
            if (null!=map.put(su2cSampleId, Collections.singleton(sampleId))) {
                System.err.println("Something is wrong: there are two samples with the same su2c ID: "+su2cSampleId);
            }
        }
        return map;
    }
    
    private static List<ClinicalAttribute> getClinicalAttributes() {
        return Arrays.asList(
                new ClinicalAttribute("PATIENT_ID", "Patient ID", "Patient ID", "STRING"),
                new ClinicalAttribute("RACE", "Race", "Race", "STRING"),
                new ClinicalAttribute("AGE", "Age", "Age", "Number"),
                new ClinicalAttribute("PATIENT_CATEGORY", "Patient category", "Patient category", "STRING"),
                new ClinicalAttribute("CLIN_T_Stage", "Clinical T stage", "Clinical T stage", "STRING"),
                new ClinicalAttribute("CLIN_N_Stage", "Clinical N stage", "Clinical N stage", "STRING"),
                new ClinicalAttribute("CLIN_M_Stage", "Clinical M stage", "Clinical M stage", "STRING"),
                new ClinicalAttribute("HISTOLOGY", "Histology", "Histology", "STRING"),
                new ClinicalAttribute("PATH_RESULT", "Pathology result", "Pathology result", "STRING"),
                new ClinicalAttribute("PATH_T_STAGE", "Pathology T stage", "Pathology T stage", "STRING"),
                new ClinicalAttribute("PATH_N_STAGE", "Pathology N stage", "Pathology N stage", "STRING"),
                new ClinicalAttribute("PATH_M_STAGE", "Pathology M stage", "Pathology M stage", "STRING"),
                new ClinicalAttribute("GLEASON_SCORE_1", "Gleason score 1", "Gleason score 1", "Number"),
                new ClinicalAttribute("GLEASON_SCORE_2", "Gleason score 2", "Gleason score 2", "Number"),
                new ClinicalAttribute("GLEASON_SCORE", "Gleason score", "Gleason score", "Number"),
                new ClinicalAttribute("TUMOR_SITE", "Tumor site", "Tumor site", "STRING"),
                new ClinicalAttribute("PROC_INSTRUMENT", "Procedure instrument", "Procedure instrument", "STRING")
        );
    }
    
    private static List<ClinicalData> parsePatientClinicalData(
            Node patientNode, String patientId, int cancerStudyId) {
        List<ClinicalData> clinicalData = new ArrayList<ClinicalData>();
        Node node = patientNode.selectSingleNode("PtProtocolStudyId");
        if (node!=null) {
            clinicalData.add(new ClinicalData(cancerStudyId, patientId, "PATIENT_ID", node.getText()));
        }
        
        node = patientNode.selectSingleNode("PtRace");
        if (node!=null) {
            clinicalData.add(new ClinicalData(cancerStudyId, patientId, "RACE", node.getText()));
        }
        
        node = patientNode.selectSingleNode("PtRegistrationAge");
        if (node!=null) {
            clinicalData.add(new ClinicalData(cancerStudyId, patientId, "AGE", node.getText()));
        }
        
        node = patientNode.selectSingleNode("Categories/Category/Category");
        if (node!=null) {
            clinicalData.add(new ClinicalData(cancerStudyId, patientId, "PATIENT_CATEGORY", node.getText()));
        }
        
        node = patientNode.selectSingleNode("ClinicalStages/ClinicalStage/ClinStageT");
        if (node!=null) {
            clinicalData.add(new ClinicalData(cancerStudyId, patientId, "CLIN_T_Stage", node.getText()));
        }
        
        node = patientNode.selectSingleNode("ClinicalStages/ClinicalStage/ClinStageN");
        if (node!=null) {
            clinicalData.add(new ClinicalData(cancerStudyId, patientId, "CLIN_N_Stage", node.getText()));
        }
        
        node = patientNode.selectSingleNode("ClinicalStages/ClinicalStage/ClinStageM");
        if (node!=null) {
            clinicalData.add(new ClinicalData(cancerStudyId, patientId, "CLIN_M_Stage", node.getText()));
        }
        
        node = patientNode.selectSingleNode("Pathologies/Pathology/PathHistology");
        if (node!=null) {
            clinicalData.add(new ClinicalData(cancerStudyId, patientId, "HISTOLOGY", node.getText()));
        }
        
        node = patientNode.selectSingleNode("Pathologies/Pathology/PathResult");
        if (node!=null) {
            clinicalData.add(new ClinicalData(cancerStudyId, patientId, "PATH_RESULT", node.getText()));
        }
        
        node = patientNode.selectSingleNode("Pathologies/Pathology/PathologyStageGrades/PathologyStageGrade/PathStageT");
        if (node!=null) {
            clinicalData.add(new ClinicalData(cancerStudyId, patientId, "PATH_T_STAGE", node.getText()));
        }
        
        node = patientNode.selectSingleNode("Pathologies/Pathology/PathologyStageGrades/PathologyStageGrade/PathStageN");
        if (node!=null) {
            clinicalData.add(new ClinicalData(cancerStudyId, patientId, "PATH_N_STAGE", node.getText()));
        }
        
        node = patientNode.selectSingleNode("Pathologies/Pathology/PathologyStageGrades/PathologyStageGrade/PathStageM");
        if (node!=null) {
            clinicalData.add(new ClinicalData(cancerStudyId, patientId, "PATH_M_STAGE", node.getText()));
        }
        
        node = patientNode.selectSingleNode("Pathologies/Pathology/ProstateBiopsyPaths/ProstateBiopsyPath/PathGG1");
        if (node!=null) {
            clinicalData.add(new ClinicalData(cancerStudyId, patientId, "GLEASON_SCORE_1", node.getText()));
        }
        
        node = patientNode.selectSingleNode("Pathologies/Pathology/ProstateBiopsyPaths/ProstateBiopsyPath/PathGG2");
        if (node!=null) {
            clinicalData.add(new ClinicalData(cancerStudyId, patientId, "GLEASON_SCORE_2", node.getText()));
        }
        
        node = patientNode.selectSingleNode("Pathologies/Pathology/ProstateBiopsyPaths/ProstateBiopsyPath/PathGGS");
        if (node!=null) {
            clinicalData.add(new ClinicalData(cancerStudyId, patientId, "GLEASON_SCORE", node.getText()));
        }

        return clinicalData;
    }
    
    private static void parseMedicalTherapies(List<ClinicalEvent> clinicalEvents,
            Node patientNode, String patientId, int cancerStudyId) {
        List<Node> treatmentNodes = patientNode.selectNodes("MedicalTherapies/MedicalTherapy");
        
        for (Node treatmentNode : treatmentNodes) {
            ClinicalEvent clinicalEvent = new ClinicalEvent();
            clinicalEvent.setCancerStudyId(cancerStudyId);
            clinicalEvent.setPatientId(patientId);
            clinicalEvent.setEventType("TREATMENT");
            clinicalEvent.addEventDatum("TREATMENT_TYPE","Medical Therapy");
            
            Node node = treatmentNode.selectSingleNode("MedTxDate");
            if (node==null) {
                System.err.println("no date");
                continue;
            }
            clinicalEvent.setStartDate(Long.parseLong(node.getText()));
            
            node = treatmentNode.selectSingleNode("MedTxStopDate");
            if (node!=null) {
                clinicalEvent.setStopDate(Long.parseLong(node.getText()));
            }
            
            node = treatmentNode.selectSingleNode("MedTxType");
            if (node!=null) {
                clinicalEvent.addEventDatum("SUBTYPE", node.getText());
            }
            
            node = treatmentNode.selectSingleNode("MedTxIndication");
            if (node!=null) {
                clinicalEvent.addEventDatum("INDICATION", node.getText());
            }
            
            node = treatmentNode.selectSingleNode("MedTxAgent");
            if (node!=null) {
                clinicalEvent.addEventDatum("AGENT", node.getText());
            }
            
            node = treatmentNode.selectSingleNode("MedTxDose");
            if (node!=null) {
                clinicalEvent.addEventDatum("DOSE", node.getText());
            }
            
            node = treatmentNode.selectSingleNode("MedTxTotalDose");
            if (node!=null) {
                clinicalEvent.addEventDatum("TOTAL_DOSE", node.getText());
            }
            
            node = treatmentNode.selectSingleNode("MedTxUnits");
            if (node!=null) {
                clinicalEvent.addEventDatum("UNIT", node.getText());
            }
            
            node = treatmentNode.selectSingleNode("MedTxSchedule");
            if (node!=null) {
                clinicalEvent.addEventDatum("SCHEDULE", node.getText());
            }
            
            node = treatmentNode.selectSingleNode("MedTxRoute");
            if (node!=null) {
                clinicalEvent.addEventDatum("ROUTE", node.getText());
            }
            
            clinicalEvents.add(clinicalEvent);
        }
    }
    
    private static void parseRadiationTherapies(List<ClinicalEvent> clinicalEvents,
            Node patientNode, String patientId, int cancerStudyId) {
        List<Node> treatmentNodes = patientNode.selectNodes("RadiationTherapies/RadiationTherapy");
        
        for (Node treatmentNode : treatmentNodes) {
            ClinicalEvent clinicalEvent = new ClinicalEvent();
            clinicalEvent.setCancerStudyId(cancerStudyId);
            clinicalEvent.setPatientId(patientId);
            clinicalEvent.setEventType("TREATMENT");
            clinicalEvent.addEventDatum("TREATMENT_TYPE", "Radiation Therapy");
            
            Node node = treatmentNode.selectSingleNode("RadTxDate");
            if (node==null) {
                System.err.println("no date");
                continue;
            }
            clinicalEvent.setStartDate(Long.parseLong(node.getText()));
            
            node = treatmentNode.selectSingleNode("RadTxStopDate");
            if (node!=null) {
                clinicalEvent.setStopDate(Long.parseLong(node.getText()));
            }
            
            node = treatmentNode.selectSingleNode("RadTxType");
            if (node!=null) {
                clinicalEvent.addEventDatum("SUBTYPE", node.getText());
            }
            
            node = treatmentNode.selectSingleNode("RadTxIndication");
            if (node!=null) {
                clinicalEvent.addEventDatum("INDICATION", node.getText());
            }
            
            node = treatmentNode.selectSingleNode("RadTxIntent");
            if (node!=null) {
                clinicalEvent.addEventDatum("INTENT", node.getText());
            }
            
            node = treatmentNode.selectSingleNode("RadTxDosePerFraction");
            if (node!=null) {
                clinicalEvent.addEventDatum("DOSE_PER_FRACTION", node.getText());
            }
            
            node = treatmentNode.selectSingleNode("RadTxTotalDose");
            if (node!=null) {
                clinicalEvent.addEventDatum("TOTAL_DOSE", node.getText());
            }
            
            node = treatmentNode.selectSingleNode("RadTxUnits");
            if (node!=null) {
                clinicalEvent.addEventDatum("UNIT", node.getText());
            }
            
            node = treatmentNode.selectSingleNode("RadTxNumFractions");
            if (node!=null) {
                clinicalEvent.addEventDatum("NUM_FRACTIONS", node.getText());
            }
            
            node = treatmentNode.selectSingleNode("RadTxTarget");
            if (node!=null) {
                clinicalEvent.addEventDatum("TARGET", node.getText());
            }
            
            clinicalEvents.add(clinicalEvent);
        }
    }
    
    private static void parseBrachyTherapies(List<ClinicalEvent> clinicalEvents,
            Node patientNode, String patientId, int cancerStudyId) {
        List<Node> treatmentNodes = patientNode.selectNodes("BrachyTherapies/BrachyTherapy");
        
        for (Node treatmentNode : treatmentNodes) {
            ClinicalEvent clinicalEvent = new ClinicalEvent();
            clinicalEvent.setCancerStudyId(cancerStudyId);
            clinicalEvent.setPatientId(patientId);
            clinicalEvent.setEventType("TREATMENT");
            clinicalEvent.addEventDatum("TREATMENT_TYPE","Brachytherapy");
            
            Node node = treatmentNode.selectSingleNode("BrachyDate");
            if (node==null) {
                System.err.println("no date");
                continue;
            }
            clinicalEvent.setStartDate(Long.parseLong(node.getText()));
            
            node = treatmentNode.selectSingleNode("BrachyIsotope");
            if (node!=null) {
                clinicalEvent.addEventDatum("BRACHY_ISOTOPE", node.getText());
            }
            
            node = treatmentNode.selectSingleNode("BrachyPrescribedDose");
            if (node!=null) {
                clinicalEvent.addEventDatum("DOSE", node.getText());
            }
            
            node = treatmentNode.selectSingleNode("BrachyDoseNotes");
            if (node!=null) {
                clinicalEvent.addEventDatum("DOSE_NOTES", node.getText());
            }
            
            clinicalEvents.add(clinicalEvent);
        }
    }
    
    private static void parseDiagnostics(List<ClinicalEvent> clinicalEvents,
            Node patientNode, String patientId, int cancerStudyId) {
        List<Node> diagnosticNodes = patientNode.selectNodes("Diagnostics/Diagnostic");
        for (Node diagnosticNode : diagnosticNodes) {
            ClinicalEvent clinicalEvent = new ClinicalEvent();
            clinicalEvent.setCancerStudyId(cancerStudyId);
            clinicalEvent.setPatientId(patientId);
            clinicalEvent.setEventType("DIAGNOSTIC");
            
            Node node = diagnosticNode.selectSingleNode("DxDate");
            if (node==null) {
                System.err.println("no date");
                continue;
            }
            clinicalEvent.setStartDate(Long.parseLong(node.getText()));
            
            node = diagnosticNode.selectSingleNode("DxType");
            if (node!=null) {
                clinicalEvent.addEventDatum("DIAGNOSTIC_TYPE", node.getText());
            }
            
            node = diagnosticNode.selectSingleNode("DxTarget");
            if (node!=null) {
                clinicalEvent.addEventDatum("TARGET", node.getText());
            }
            
            node = diagnosticNode.selectSingleNode("DxResult");
            if (node!=null) {
                clinicalEvent.addEventDatum("RESULT", node.getText());
            }
            
            node = diagnosticNode.selectSingleNode("DxNotes");
            if (node!=null) {
                clinicalEvent.addEventDatum("NOTES", node.getText());
            }
            
            node = diagnosticNode.selectSingleNode("DxSide");
            if (node!=null) {
                clinicalEvent.addEventDatum("SIDE", node.getText());
            }
            
            node = diagnosticNode.selectSingleNode("DxStatus");
            if (node!=null) {
                clinicalEvent.addEventDatum("STATUS", node.getText());
            }
            
            node = diagnosticNode.selectSingleNode("ImgBaseline");
            if (node!=null) {
                clinicalEvent.addEventDatum("BASELINE", node.getText());
            }
            
            node = diagnosticNode.selectSingleNode("DxNumNewTumors");
            if (node!=null) {
                clinicalEvent.addEventDatum("NUM_NEW_TUMORS", node.getText());
            }
            
            clinicalEvents.add(clinicalEvent);
        }
    }
    
    private static void parseLabTests(List<ClinicalEvent> clinicalEvents,
            Node patientNode, String patientId, int cancerStudyId) {
        List<Node> labTestNodes = patientNode.selectNodes("LabTests/LabTest");
        for (Node labTestNode : labTestNodes) {
            ClinicalEvent clinicalEvent = new ClinicalEvent();
            clinicalEvent.setCancerStudyId(cancerStudyId);
            clinicalEvent.setPatientId(patientId);
            clinicalEvent.setEventType("LAB_TEST");
            
            Node node  = labTestNode.selectSingleNode("LabDate");
            if (node==null) {
                System.err.println("no date");
                continue;
            }
            clinicalEvent.setStartDate(Long.parseLong(node.getText()));
            
            node  = labTestNode.selectSingleNode("LabTest");
            if (node==null) {
                System.err.println("no lab test name");
                continue;
            }
            clinicalEvent.addEventDatum("TEST", node.getText());
            
            node  = labTestNode.selectSingleNode("LabResult");
            if (node==null) {
                System.err.println("no lab result");
                continue;
            }
            clinicalEvent.addEventDatum("RESULT", node.getText());
            
            node  = labTestNode.selectSingleNode("LabUnits");
            if (node!=null) {
                clinicalEvent.addEventDatum("UNIT", node.getText());
            }
            
            node  = labTestNode.selectSingleNode("LabNormalRange");
            if (node!=null) {
                clinicalEvent.addEventDatum("NORMAL_RANGE", node.getText());
            }
            
            node  = labTestNode.selectSingleNode("LabNotes");
            if (node!=null) {
                clinicalEvent.addEventDatum("NOTES", node.getText());
            }
            
            clinicalEvents.add(clinicalEvent);
        }
    }
    
    private static void parseClinicalEventsFromSpecimen(List<ClinicalEvent> clinicalEvents,
            Node patientNode, String patientId, int cancerStudyId) {
        List<Node> specimenAccessionNodes = patientNode.selectNodes("SpecimenAccessions/SpecimenAccession");
        for (Node specimenAccessionNode : specimenAccessionNodes) {
            Node node  = specimenAccessionNode.selectSingleNode("AccessionDate");
            if (node==null) {
                System.err.println("no date");
                continue;
            }
            long date = Long.parseLong(node.getText());
            
            String site = null, type = null, instrument = null;
            node  = specimenAccessionNode.selectSingleNode("AccessionAnatomicSite");
            if (node!=null) {
                site = node.getText();
            }
            node  = specimenAccessionNode.selectSingleNode("AccessionVisitType");
            if (node!=null) {
                type = node.getText();
            }
            node  = specimenAccessionNode.selectSingleNode("AccessionProcInstrument");
            if (node!=null) {
                instrument = node.getText();
            }
            
            List<Node> specimenNodes = specimenAccessionNode.selectNodes("Specimens/Specimen");
            for (Node specimenNode : specimenNodes) {
                ClinicalEvent clinicalEvent = new ClinicalEvent();
                clinicalEvent.setCancerStudyId(cancerStudyId);
                clinicalEvent.setPatientId(patientId);
                clinicalEvent.setEventType("SPECIMEN");
                clinicalEvent.setStartDate(date);
                if (site!=null) {
                    clinicalEvent.addEventDatum("SPECIMEN_SITE", site);
                }
                if (type!=null) {
                    clinicalEvent.addEventDatum("ANATOMIC_SITE", type);
                }
                if (instrument!=null) {
                    clinicalEvent.addEventDatum("PROC_INSTRUMENT", instrument);
                }
                
                addAllDataUnderNode(clinicalEvent, Element.class.cast(specimenNode));

                clinicalEvents.add(clinicalEvent);
            }
        }
    }
    
    private static List<ClinicalData> parseClinicalDataFromSpecimen(Node patientNode, int cancerStudyId) {
        List<ClinicalData> clinicalData = new ArrayList<ClinicalData>();
        List<Node> specimenAccessionNodes = patientNode.selectNodes("SpecimenAccessions/SpecimenAccession");
        for (Node specimenAccessionNode : specimenAccessionNodes) {
            String site = null, instrument = null;
            Node node  = specimenAccessionNode.selectSingleNode("AccessionAnatomicSite");
            if (node!=null) {
                site = node.getText();
            }
            node  = specimenAccessionNode.selectSingleNode("AccessionProcInstrument");
            if (node!=null) {
                instrument = node.getText();
            }
            
            List<Node> specimenNodes = specimenAccessionNode.selectNodes("Specimens/Specimen");
            for (Node specimenNode : specimenNodes) {
                node  = specimenNode.selectSingleNode("SpecimenReferenceNumber");
                if (node==null) {
                    continue;
                }
                String su2cSampleId = node.getText();
                
                if (site!=null) {
                    ClinicalData clinicalDatum = new ClinicalData(cancerStudyId, su2cSampleId, "TUMOR_SITE", site);
                    clinicalData.add(clinicalDatum);
                }
                
                if (instrument!=null) {
                    ClinicalData clinicalDatum = new ClinicalData(cancerStudyId, su2cSampleId, "PROC_INSTRUMENT", instrument);
                    clinicalData.add(clinicalDatum);
                }
            }
        }
        return clinicalData;
    }
    
    private static long parseStatusesAndReturnDiagnosisDate(List<ClinicalEvent> clinicalEvents,
            Node patientNode, String patientId, int cancerStudyId) {
        List<Node> statusNodes = patientNode.selectNodes("Statuses/Status");
        long diagnosisDate = 0;
        for (Node statusNode : statusNodes) {
            ClinicalEvent clinicalEvent = new ClinicalEvent();
            clinicalEvent.setCancerStudyId(cancerStudyId);
            clinicalEvent.setPatientId(patientId);
            clinicalEvent.setEventType("STATUS");
            
            Node node  = statusNode.selectSingleNode("StatusDate");
            if (node==null) {
                System.err.println("no date");
                continue;
            }
            long statusDate = Long.parseLong(node.getText());
            clinicalEvent.setStartDate(statusDate);
            
            node  = statusNode.selectSingleNode("Status");
            if (node==null) {
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
    
    private static void addAllDataUnderNode(ClinicalEvent clinicalEvent, Element element) {
        for ( Iterator i = element.elementIterator(); i.hasNext(); ) {
            Element child = (Element) i.next();
            clinicalEvent.addEventDatum(child.getName(), child.getTextTrim());
        }
    }
}
