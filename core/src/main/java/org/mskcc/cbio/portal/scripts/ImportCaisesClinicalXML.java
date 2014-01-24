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
import org.mskcc.cbio.portal.dao.DaoDiagnostic;
import org.mskcc.cbio.portal.dao.DaoException;
import org.mskcc.cbio.portal.dao.DaoLabTest;
import org.mskcc.cbio.portal.dao.DaoTreatment;
import org.mskcc.cbio.portal.dao.MySQLbulkLoader;
import org.mskcc.cbio.portal.model.CancerStudy;
import org.mskcc.cbio.portal.model.ClinicalAttribute;
import org.mskcc.cbio.portal.model.ClinicalData;
import org.mskcc.cbio.portal.model.Diagnostic;
import org.mskcc.cbio.portal.model.LabTest;
import org.mskcc.cbio.portal.model.Treatment;

/**
 *
 * @author jgao
 */
public final class ImportCaisesClinicalXML {
    
    private ImportCaisesClinicalXML() {}
    
    public static void main(String[] args) throws Exception {
        //args = new String[]{"/Users/jj/projects/cbio-portal-data/studies/prad/su2c/data_clinical_caises.xml","/Users/jj/projects/cbio-portal-data/studies/prad/su2c/patient_id_mapping.txt","/Users/jj/projects/cbio-portal-data/studies/prad/su2c/meta_clinical_caises.txt"};
        
        if (args.length != 3) {
            System.out.println("command line usage:  importCaisesXml <data_clinical_caises.xml> <patient_id_mapping.txt> <meta_clinical_caises.txt>");
            return;
        }
        
        String urlXml = args[0];
        String urlIDMappingFile = args[1];
        String meta = args[2];
        
        Properties properties = new Properties();
        properties.load(new FileInputStream(meta));
      
        CancerStudy cancerStudy = DaoCancerStudy.getCancerStudyByStableId(properties.getProperty("cancer_study_identifier"));
        if (cancerStudy == null) {
            System.err.println("Unknown cancer study: " + args[1]);
            return;
        }
        
        int cancerStudyId = cancerStudy.getInternalId();
        DaoTreatment.deleteByCancerStudyId(cancerStudyId);
        DaoLabTest.deleteByCancerStudyId(cancerStudyId);
        DaoDiagnostic.deleteByCancerStudyId(cancerStudyId);
        
        Map<String,String> patientIDMapping = readPatientIDMapping(urlIDMappingFile);
        importData(urlXml, cancerStudy.getInternalId(), patientIDMapping);

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
    
    private static void importData(String urlXml, int cancerStudyId, Map<String,String> patientIDMapping) throws DocumentException, DaoException {
        MySQLbulkLoader.bulkLoadOn();
        
        SAXReader reader = new SAXReader();
        Document document = reader.read(urlXml);
        
        List<Node> patientNodes = document.selectNodes("//Patients/Patient");
        
        long labTestId = DaoLabTest.getLargestLabTestId();
        long diagnosticId = DaoDiagnostic.getLargestDiagnosticId();
        long treatmentId = DaoTreatment.getLargestTreatmentId();
        
        for (Node patientNode : patientNodes) {
            String patientInternalId = patientNode.selectSingleNode("PtProtocolStudyId").getText();
            String patientId = patientIDMapping.get(patientInternalId);
            if (patientId==null) {
                System.err.println(patientInternalId+" is not found in the mapping file. Skip...");
                continue;
            }
            
            System.out.println("Importing "+patientId+" ("+patientInternalId+")");
            
//            List<ClinicalData> clinicalData = 
            
            List<Treatment> treatments = new ArrayList<Treatment>();
            parseMedicalTherapies(treatments, patientNode, patientId, cancerStudyId);
            parseRadiationTherapies(treatments, patientNode, patientId, cancerStudyId);
            parseBrachyTherapies(treatments, patientNode, patientId, cancerStudyId);
            for (Treatment treatment : treatments) {
                if (validateTreatment(treatment)) {
                    treatment.setTreatmentId(++treatmentId);
                    DaoTreatment.addDatum(treatment);
                }
            }
            
            List<Diagnostic> diagnostics = parseDiagnostics(patientNode, patientId, cancerStudyId);
            for (Diagnostic diagnostic : diagnostics) {
                if (validateDiagnostic(diagnostic)) {
                    diagnostic.setDiagosticId(++diagnosticId);
                    DaoDiagnostic.addDatum(diagnostic);
                }
            }
            
            List<LabTest> labTests = parseLabTests(patientNode, patientId, cancerStudyId);
            for (LabTest labTest : labTests) {
                if (validateLabTest(labTest)) {
                    labTest.setLabTestId(++labTestId);
                    DaoLabTest.addDatum(labTest);
                }
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
    
    private static void parseMedicalTherapies(List<Treatment> treatments, Node patientNode, String patientId, int cancerStudyId) {
        List<Node> treatmentNodes = patientNode.selectNodes("MedicalTherapies/MedicalTherapy");
        
        for (Node treatmentNode : treatmentNodes) {
            Treatment treatment = new Treatment();
            treatment.setCancerStudyId(cancerStudyId);
            treatment.setPatientId(patientId);
            treatment.setType("Medical therapy");
            
            Node node = treatmentNode.selectSingleNode("MedTxDate");
            if (node!=null) {
                treatment.setStartDate(parseInt(node.getText(), false));
            }
            
            node = treatmentNode.selectSingleNode("MedTxStopDate");
            if (node!=null) {
                treatment.setStopDate(parseInt(node.getText(), false));
            }
            
            node = treatmentNode.selectSingleNode("MedTxType");
            if (node!=null) {
                treatment.setSubtype(node.getText());
            }
            
            node = treatmentNode.selectSingleNode("MedTxIndication");
            if (node!=null) {
                treatment.setIndication(node.getText());
            }
            
            node = treatmentNode.selectSingleNode("MedTxAgent");
            if (node!=null) {
                treatment.setAgent(node.getText());
            }
            
            node = treatmentNode.selectSingleNode("MedTxDose");
            if (node!=null) {
                treatment.setDose(parseDouble(node.getText(),true));
            }
            
            node = treatmentNode.selectSingleNode("MedTxTotalDose");
            if (node!=null) {
                treatment.setTotalDose(parseDouble(node.getText(),true));
            }
            
            node = treatmentNode.selectSingleNode("MedTxUnits");
            if (node!=null) {
                treatment.setUnit(node.getText());
            }
            
            node = treatmentNode.selectSingleNode("MedTxSchedule");
            if (node!=null) {
                treatment.setSchedule(node.getText());
            }
            
            node = treatmentNode.selectSingleNode("MedTxRoute");
            if (node!=null) {
                treatment.setRoute(node.getText());
            }
            
            treatments.add(treatment);
        }
    }
    
    private static void parseRadiationTherapies(List<Treatment> treatments, Node patientNode, String patientId, int cancerStudyId) {
        List<Node> treatmentNodes = patientNode.selectNodes("RadiationTherapies/RadiationTherapy");
        
        for (Node treatmentNode : treatmentNodes) {
            Treatment treatment = new Treatment();
            treatment.setCancerStudyId(cancerStudyId);
            treatment.setPatientId(patientId);
            treatment.setType("Radiation therapy");
            
            Node node = treatmentNode.selectSingleNode("RadTxDate");
            if (node!=null) {
                treatment.setStartDate(parseInt(node.getText(), false));
            }
            
            node = treatmentNode.selectSingleNode("RadTxStopDate");
            if (node!=null) {
                treatment.setStopDate(parseInt(node.getText(), false));
            }
            
            node = treatmentNode.selectSingleNode("RadTxType");
            if (node!=null) {
                treatment.setSubtype(node.getText());
            }
            
            node = treatmentNode.selectSingleNode("RadTxIndication");
            if (node!=null) {
                treatment.setIndication(node.getText());
            }
            
            node = treatmentNode.selectSingleNode("RadTxIntent");
            if (node!=null) {
                treatment.setIntent(node.getText());
            }
            
            node = treatmentNode.selectSingleNode("RadTxDosePerFraction");
            if (node!=null) {
                treatment.setDose(parseDouble(node.getText(),true));
            }
            
            node = treatmentNode.selectSingleNode("RadTxTotalDose");
            if (node!=null) {
                treatment.setTotalDose(parseDouble(node.getText(),true));
            }
            
            node = treatmentNode.selectSingleNode("RadTxUnits");
            if (node!=null) {
                treatment.setUnit(node.getText());
            }
            
            node = treatmentNode.selectSingleNode("RadTxNumFractions");
            if (node!=null) {
                treatment.setSchedule(node.getText() + " fractions");
            }
            
            node = treatmentNode.selectSingleNode("RadTxTarget");
            if (node!=null) {
                treatment.setTarget(node.getText());
            }
            
            treatments.add(treatment);
        }
    }
    
    private static void parseBrachyTherapies(List<Treatment> treatments, Node patientNode, String patientId, int cancerStudyId) {
        List<Node> treatmentNodes = patientNode.selectNodes("BrachyTherapies/BrachyTherapy");
        
        for (Node treatmentNode : treatmentNodes) {
            Treatment treatment = new Treatment();
            treatment.setCancerStudyId(cancerStudyId);
            treatment.setPatientId(patientId);
            treatment.setType("Brachytherapy");
            
            Node node = treatmentNode.selectSingleNode("BrachyDate");
            if (node!=null) {
                treatment.setStartDate(parseInt(node.getText(), false));
            }
            
            node = treatmentNode.selectSingleNode("BrachyIsotope");
            if (node!=null) {
                treatment.setIsotope(node.getText());
            }
            
            node = treatmentNode.selectSingleNode("BrachyPrescribedDose");
            if (node!=null) {
                treatment.setTotalDose(parseDouble(node.getText(),true));
            }
            
//            node = treatmentNode.selectSingleNode("BrachyDoseNotes");
//            if (node!=null) {
//                treatment.(node.getText());
//            }
            
            treatments.add(treatment);
        }
    }
    
    private static boolean validateTreatment(Treatment treatment) {
        if (treatment.getStartDate()==null) {
            return false;
        }
        
        return true;
    }
    
    private static List<Diagnostic> parseDiagnostics(Node patientNode, String patientId, int cancerStudyId) {
        List<Node> diagnosticNodes = patientNode.selectNodes("Diagnostics/Diagnostic");
        List<Diagnostic> diagnostics = new ArrayList<Diagnostic>(diagnosticNodes.size());
        for (Node diagnosticNode : diagnosticNodes) {
            Diagnostic diagnostic = new Diagnostic();
            diagnostic.setCancerStudyId(cancerStudyId);
            diagnostic.setPatientId(patientId);
            
            Node node = diagnosticNode.selectSingleNode("DxDate");
            if (node!=null) {
                diagnostic.setDate(parseInt(node.getText(),false));
            }
            
            node = diagnosticNode.selectSingleNode("DxType");
            if (node!=null) {
                diagnostic.setType(node.getText());
            }
            
            node = diagnosticNode.selectSingleNode("DxTarget");
            if (node!=null) {
                diagnostic.setTarget(node.getText());
            }
            
            node = diagnosticNode.selectSingleNode("DxResult");
            if (node!=null) {
                diagnostic.setResult(node.getText());
            }
            
            node = diagnosticNode.selectSingleNode("DxNotes");
            if (node!=null) {
                diagnostic.setNotes(node.getText());
            }
            
            node = diagnosticNode.selectSingleNode("DxSide");
            if (node!=null) {
                diagnostic.setSide(node.getText());
            }
            
            node = diagnosticNode.selectSingleNode("DxStatus");
            if (node!=null) {
                diagnostic.setStatus(node.getText());
            }
            
            node = diagnosticNode.selectSingleNode("ImgBaseline");
            if (node!=null) {
                diagnostic.setImageBaseLine(node.getText());
            }
            
            node = diagnosticNode.selectSingleNode("DxNumNewTumors");
            if (node!=null) {
                diagnostic.setNumNewTumors(parseInt(node.getText(),false));
            }
            
            diagnostics.add(diagnostic);
        }
        
        return diagnostics;
    }
    
    private static boolean validateDiagnostic(Diagnostic diagnostic) {
        if (diagnostic.getDate()==null) {
            return false;
        }
        
        return true;
    }
    
//     `DIAGOSTIC_ID` int(255) NOT NULL auto_increment,
//  `CANCER_STUDY_ID` int(11) NOT NULL,
//  `PATIENT_ID` varchar(255) NOT NULL,
//  `DATE` int,
//  `TYPE` varchar(30), # Bone scan, CT scan (for diagnostics), PCA, ACP (for lab tests)
//  `SIDE` varchar(50), 
//  `TARGET` varchar(255),
//  `RESULT` varchar(255),
//  `STATUS` varchar(255),
//  `IMAGE_BASELINE` varchar(20),
//  `NUM_NEW_TUMORS` int,
    
    private static List<LabTest> parseLabTests(Node patientNode, String patientId, int cancerStudyId) {
        List<Node> labTestNodes = patientNode.selectNodes("LabTests/LabTest");
        List<LabTest> labTests = new ArrayList<LabTest>(labTestNodes.size());
        for (Node labTestNode : labTestNodes) {
            LabTest labTest = new LabTest();
            labTest.setCancerStudyId(cancerStudyId);
            labTest.setPatientId(patientId);
            
            Node node  = labTestNode.selectSingleNode("LabDate");
            if (node!=null) {
                labTest.setDate(parseInt(node.getText(),false));
            }
            
            node  = labTestNode.selectSingleNode("LabTest");
            if (node!=null) {
                labTest.setTest(node.getText());
            }
            
            node  = labTestNode.selectSingleNode("LabResult");
            if (node!=null) {
                labTest.setResult(node.getText());
            }
            
            node  = labTestNode.selectSingleNode("LabUnits");
            if (node!=null) {
                labTest.setUnit(node.getText());
            }
            
            node  = labTestNode.selectSingleNode("LabNormalRange");
            if (node!=null) {
                labTest.setNormalRange(node.getText());
            }
            
            node  = labTestNode.selectSingleNode("LabNotes");
            if (node!=null) {
                labTest.setNotes(node.getText());
            }
            
            labTests.add(labTest);
        }
        
        
        return labTests;
    }
    
    private static boolean validateLabTest(LabTest labTest) {
        if (labTest.getDate()==null) {
            return false;
        }
        if (labTest.getTest()==null) {
            return false;
        }
        if (labTest.getResult()==null) {
            return false;
        }
        return true;
    }
    
    private static Double parseDouble(String str, boolean takeFirstPart) {
        if (str==null) {
            return null;
        }
        
        if (takeFirstPart) {
            int ix = str.indexOf(" ");
            if (ix>0) {
                str = str.substring(0, ix);
            }
        }
        
        try {
            return Double.valueOf(str);
        } catch (NumberFormatException e) {
            e.printStackTrace();
            return null;
        }
    }
    
    private static Integer parseInt(String str, boolean takeFirstPart) {
        if (str==null) {
            return null;
        }
        
        if (takeFirstPart) {
            int ix = str.indexOf(" ");
            if (ix>0) {
                str = str.substring(0, ix);
            }
        }
        
        try {
            return Integer.valueOf(str);
        } catch (NumberFormatException e) {
            e.printStackTrace();
            return null;
        }
    }
}
