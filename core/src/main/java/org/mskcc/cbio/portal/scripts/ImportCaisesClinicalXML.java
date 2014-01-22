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
import org.mskcc.cbio.portal.model.LabTest;

/**
 *
 * @author jgao
 */
public final class ImportCaisesClinicalXML {
    
    private ImportCaisesClinicalXML() {}
    
    public static void main(String[] args) throws Exception {
        args = new String[]{"/Users/jj/projects/cbio-portal-data/studies/prad/su2c/data_clinical_caises.xml","/Users/jj/projects/cbio-portal-data/studies/prad/su2c/patient_id_mapping.txt","/Users/jj/projects/cbio-portal-data/studies/prad/su2c/meta_clinical_caises.txt"};
        
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
        
        long labTestId = 0;
        
        for (Node patientNode : patientNodes) {
            String patientInternalId = patientNode.selectSingleNode("PtProtocolStudyId").getText();
            String patientId = patientIDMapping.get(patientInternalId);
            if (patientId==null) {
                System.err.println(patientInternalId+" is not found in the mapping file. Skip...");
                continue;
            }
            
            System.out.println("Importing "+patientId+" ("+patientInternalId+")");
            
            List<LabTest> labTests = parseLabTests(patientNode, patientId, cancerStudyId);
            for (LabTest labTest : labTests) {
                labTest.setLabTestId(++labTestId);
                DaoLabTest.addDatum(labTest);
            }
        }
        
        MySQLbulkLoader.flushAll();
    }
    
    private static List<LabTest> parseLabTests(Node patientNode, String patientId, int cancerStudyId) {
        List<Node> labTestNodes = patientNode.selectNodes("LabTests/LabTest");
        List<LabTest> labTests = new ArrayList<LabTest>(labTestNodes.size());
        for (Node labTestNode : labTestNodes) {
            LabTest labTest = new LabTest();
            labTest.setCancerStudyId(cancerStudyId);
            labTest.setCaseId(patientId);
            
            Node node  = labTestNode.selectSingleNode("LabDate");
            if (node==null) {
                continue;
            }
            try {
                labTest.setDate(Integer.parseInt(node.getText()));
            } catch (NumberFormatException e) {
                continue;
            }
            
            node  = labTestNode.selectSingleNode("LabTest");
            if (node==null) {
                continue;
            }
            labTest.setTest(node.getText());
            
            node  = labTestNode.selectSingleNode("LabResult");
            if (node==null) {
                continue;
            }
            try {
                labTest.setResult(Double.parseDouble(node.getText()));
            } catch (NumberFormatException e) {
                continue;
            }
            
            node  = labTestNode.selectSingleNode("LabUnites");
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
}
