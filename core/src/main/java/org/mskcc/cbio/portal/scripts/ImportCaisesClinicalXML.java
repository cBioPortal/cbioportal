/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mskcc.cbio.portal.scripts;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;
import org.mskcc.cbio.portal.dao.DaoCancerStudy;
import org.mskcc.cbio.portal.dao.DaoClinicalEvent;
import org.mskcc.cbio.portal.dao.DaoException;
import org.mskcc.cbio.portal.dao.MySQLbulkLoader;
import org.mskcc.cbio.portal.model.CancerStudy;
import org.mskcc.cbio.portal.model.ClinicalEvent;

/**
 *
 * @author jgao
 */
public final class ImportCaisesClinicalXML {
    
    private ImportCaisesClinicalXML() {}
    
    public static void main(String[] args) throws Exception {        
        if (args.length != 2) {
            System.out.println("command line usage:  importCaisesXml <data_clinical_caises.xml> <meta_clinical_caises.txt>");
            return;
        }
        
        String urlXml = args[0];
        String meta = args[1];
        
        Properties properties = new Properties();
        properties.load(new FileInputStream(meta));
      
        CancerStudy cancerStudy = DaoCancerStudy.getCancerStudyByStableId(properties.getProperty("cancer_study_identifier"));
        if (cancerStudy == null) {
            throw new Exception("Unknown cancer study: " + properties.getProperty("cancer_study_identifier"));
        }
        
        int cancerStudyId = cancerStudy.getInternalId();
        DaoClinicalEvent.deleteByCancerStudyId(cancerStudyId);
        
        importData(urlXml, cancerStudy.getInternalId());

        System.out.println("Done!");
    }
    
    private static Map<String,String> readPatientIDMapping(String urlIDMappingFile) throws IOException {
        FileReader reader =  new FileReader(urlIDMappingFile);
        BufferedReader buff = new BufferedReader(reader);

        Map<String,String> map = new HashMap<String,String>();
        
        String line = buff.readLine(); // skip the first line
        while ((line = buff.readLine()) != null) {
            String[] parts = line.split("\t");
            if (!parts[1].isEmpty()) {
                map.put(parts[2], parts[1]);
            }
        }
        return map;
    }
    
    private static void importData(String urlXml, int cancerStudyId) throws DocumentException, DaoException {
        MySQLbulkLoader.bulkLoadOn();
        
        SAXReader reader = new SAXReader();
        Document document = reader.read(urlXml);
        
        List<Node> patientNodes = document.selectNodes("//Patients/Patient");
        
        long clinicalEventId = DaoClinicalEvent.getLargestClinicalEventId();
        
        for (Node patientNode : patientNodes) {
            String patientId = patientNode.selectSingleNode("PtProtocolStudyId").getText();
            
            System.out.println("Importing "+patientId);
            
//            List<ClinicalData> clinicalData = 
            
            List<ClinicalEvent> clinicalEvents = new ArrayList<ClinicalEvent>();
            
            parseMedicalTherapies(clinicalEvents, patientNode, patientId, cancerStudyId);
            parseRadiationTherapies(clinicalEvents, patientNode, patientId, cancerStudyId);
            parseBrachyTherapies(clinicalEvents, patientNode, patientId, cancerStudyId);
            parseDiagnostics(clinicalEvents, patientNode, patientId, cancerStudyId);
            parseLabTests(clinicalEvents, patientNode, patientId, cancerStudyId);
            for (ClinicalEvent clinicalEvent : clinicalEvents) {
                clinicalEvent.setClinicalEventId(++clinicalEventId);
                DaoClinicalEvent.addClinicalEvent(clinicalEvent);
            }
        }
        
        MySQLbulkLoader.flushAll();
    }
    
//    private static List<ClinicalData> parseClinicalData(Node patientNode, String patientId, int cancerStudyId) {
//        List<ClinicalData> clinicalData = new ArrayList<ClinicalData>();
//        Node node = patientNode.selectSingleNode("PtProtocolStudyId");
//        if (node!=null) {
//            clinicalData.add(new ClinicalData(cancerStudyId, patientId, "AGE", node.getText()));
//        }
//        
//        node = patientNode.selectSingleNode("PtRace");
//        if (node!=null) {
//            clinicalData.add(new ClinicalData(cancerStudyId, patientId, "RACE", node.getText()));
//        }
//        
//        node = patientNode.selectSingleNode("PtRegistrationAge");
//        if (node!=null) {
//            clinicalData.add(new ClinicalData(cancerStudyId, patientId, "AGE", node.getText()));
//        }
//        
//        return clinicalData;
//    }
    
    private static void parseMedicalTherapies(List<ClinicalEvent> clinicalEvents,
            Node patientNode, String patientId, int cancerStudyId) {
        List<Node> treatmentNodes = patientNode.selectNodes("MedicalTherapies/MedicalTherapy");
        
        for (Node treatmentNode : treatmentNodes) {
            ClinicalEvent clinicalEvent = new ClinicalEvent();
            clinicalEvent.setCancerStudyId(cancerStudyId);
            clinicalEvent.setPatientId(patientId);
            clinicalEvent.setEventType("treatment");
            clinicalEvent.addEventDatum("type","medical therapy");
            
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
                clinicalEvent.addEventDatum("subtype", node.getText());
            }
            
            node = treatmentNode.selectSingleNode("MedTxIndication");
            if (node!=null) {
                clinicalEvent.addEventDatum("indication", node.getText());
            }
            
            node = treatmentNode.selectSingleNode("MedTxAgent");
            if (node!=null) {
                clinicalEvent.addEventDatum("agent", node.getText());
            }
            
            node = treatmentNode.selectSingleNode("MedTxDose");
            if (node!=null) {
                clinicalEvent.addEventDatum("dose", node.getText());
            }
            
            node = treatmentNode.selectSingleNode("MedTxTotalDose");
            if (node!=null) {
                clinicalEvent.addEventDatum("totalDose", node.getText());
            }
            
            node = treatmentNode.selectSingleNode("MedTxUnits");
            if (node!=null) {
                clinicalEvent.addEventDatum("unit", node.getText());
            }
            
            node = treatmentNode.selectSingleNode("MedTxSchedule");
            if (node!=null) {
                clinicalEvent.addEventDatum("schedule", node.getText());
            }
            
            node = treatmentNode.selectSingleNode("MedTxRoute");
            if (node!=null) {
                clinicalEvent.addEventDatum("route", node.getText());
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
            clinicalEvent.setEventType("treatment");
            clinicalEvent.addEventDatum("type", "radiation therapy");
            
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
                clinicalEvent.addEventDatum("subtype", node.getText());
            }
            
            node = treatmentNode.selectSingleNode("RadTxIndication");
            if (node!=null) {
                clinicalEvent.addEventDatum("indication", node.getText());
            }
            
            node = treatmentNode.selectSingleNode("RadTxIntent");
            if (node!=null) {
                clinicalEvent.addEventDatum("intent", node.getText());
            }
            
            node = treatmentNode.selectSingleNode("RadTxDosePerFraction");
            if (node!=null) {
                clinicalEvent.addEventDatum("dosePerFraction", node.getText());
            }
            
            node = treatmentNode.selectSingleNode("RadTxTotalDose");
            if (node!=null) {
                clinicalEvent.addEventDatum("totalDose", node.getText());
            }
            
            node = treatmentNode.selectSingleNode("RadTxUnits");
            if (node!=null) {
                clinicalEvent.addEventDatum("unit", node.getText());
            }
            
            node = treatmentNode.selectSingleNode("RadTxNumFractions");
            if (node!=null) {
                clinicalEvent.addEventDatum("numFractions", node.getText());
            }
            
            node = treatmentNode.selectSingleNode("RadTxTarget");
            if (node!=null) {
                clinicalEvent.addEventDatum("target", node.getText());
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
            clinicalEvent.setEventType("treatment");
            clinicalEvent.addEventDatum("type","Brachytherapy");
            
            Node node = treatmentNode.selectSingleNode("BrachyDate");
            if (node==null) {
                System.err.println("no date");
                continue;
            }
            clinicalEvent.setStartDate(Long.parseLong(node.getText()));
            
            node = treatmentNode.selectSingleNode("BrachyIsotope");
            if (node!=null) {
                clinicalEvent.addEventDatum("brachyIsotope", node.getText());
            }
            
            node = treatmentNode.selectSingleNode("BrachyPrescribedDose");
            if (node!=null) {
                clinicalEvent.addEventDatum("dose", node.getText());
            }
            
            node = treatmentNode.selectSingleNode("BrachyDoseNotes");
            if (node!=null) {
                clinicalEvent.addEventDatum("doseNotes", node.getText());
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
            clinicalEvent.setEventType("diagnostic");
            
            Node node = diagnosticNode.selectSingleNode("DxDate");
            if (node==null) {
                System.err.println("no date");
                continue;
            }
            clinicalEvent.setStartDate(Long.parseLong(node.getText()));
            
            node = diagnosticNode.selectSingleNode("DxType");
            if (node!=null) {
                clinicalEvent.addEventDatum("type", node.getText());
            }
            
            node = diagnosticNode.selectSingleNode("DxTarget");
            if (node!=null) {
                clinicalEvent.addEventDatum("target", node.getText());
            }
            
            node = diagnosticNode.selectSingleNode("DxResult");
            if (node!=null) {
                clinicalEvent.addEventDatum("result", node.getText());
            }
            
            node = diagnosticNode.selectSingleNode("DxNotes");
            if (node!=null) {
                clinicalEvent.addEventDatum("notes", node.getText());
            }
            
            node = diagnosticNode.selectSingleNode("DxSide");
            if (node!=null) {
                clinicalEvent.addEventDatum("side", node.getText());
            }
            
            node = diagnosticNode.selectSingleNode("DxStatus");
            if (node!=null) {
                clinicalEvent.addEventDatum("status", node.getText());
            }
            
            node = diagnosticNode.selectSingleNode("ImgBaseline");
            if (node!=null) {
                clinicalEvent.addEventDatum("baseline", node.getText());
            }
            
            node = diagnosticNode.selectSingleNode("DxNumNewTumors");
            if (node!=null) {
                clinicalEvent.addEventDatum("numNewTumors", node.getText());
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
            clinicalEvent.setEventType("lab_test");
            
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
            clinicalEvent.addEventDatum("test", node.getText());
            
            node  = labTestNode.selectSingleNode("LabResult");
            if (node==null) {
                System.err.println("no lab result");
                continue;
            }
            clinicalEvent.addEventDatum("result", node.getText());
            
            node  = labTestNode.selectSingleNode("LabUnits");
            if (node!=null) {
                clinicalEvent.addEventDatum("unit", node.getText());
            }
            
            node  = labTestNode.selectSingleNode("LabNormalRange");
            if (node!=null) {
                clinicalEvent.addEventDatum("normalange", node.getText());
            }
            
            node  = labTestNode.selectSingleNode("LabNotes");
            if (node!=null) {
                clinicalEvent.addEventDatum("notes", node.getText());
            }
            
            clinicalEvents.add(clinicalEvent);
        }
    }
}
