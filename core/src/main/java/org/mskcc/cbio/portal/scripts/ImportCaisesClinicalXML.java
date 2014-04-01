/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mskcc.cbio.portal.scripts;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
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
//        args = new String[] {"--data","/Users/gaoj/projects/cbio-portal-data/studies/prad/su2c/data_clinical_caises.xml",
//            "--meta","/Users/gaoj/projects/cbio-portal-data/studies/prad/su2c/meta_clinical_caises.txt",
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
            
            parseSpecimen(clinicalEvents, patientNode, patientId, cancerStudyId);
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
    
    private static void parseSpecimen(List<ClinicalEvent> clinicalEvents,
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
    
    private static void addAllDataUnderNode(ClinicalEvent clinicalEvent, Element element) {
        for ( Iterator i = element.elementIterator(); i.hasNext(); ) {
            Element child = (Element) i.next();
            clinicalEvent.addEventDatum(child.getName(), child.getTextTrim());
        }
    }
}
