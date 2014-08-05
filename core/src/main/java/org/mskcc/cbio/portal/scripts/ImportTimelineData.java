

package org.mskcc.cbio.portal.scripts;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import joptsimple.OptionException;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;
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
public class ImportTimelineData {
    
    private ImportTimelineData() {}
    
    public static void main(String[] args) throws Exception {
//        args = new String[] {"--data","/Users/jgao/projects/cbio-portal-data/impact/mixed/dmp/MSK-IMPACT/2014/data_clinical_events.txt",
//            "--meta","/Users/jgao/projects/cbio-portal-data/impact/mixed/dmp/MSK-IMPACT/2014/meta_clinical_events.txt",
//            "--loadMode", "bulkLoad"};
        if (args.length < 4) {
            System.out.println("command line usage:  importTimelineData --data <data_clinical_events.txt> --meta <meta_clinical_events.txt>");
            return;
        }
        
       OptionParser parser = new OptionParser();
       OptionSpec<String> data = parser.accepts( "data",
               "clinial events data file" ).withRequiredArg().describedAs( "data_clinical_events.txt" ).ofType( String.class );
       OptionSpec<String> meta = parser.accepts( "meta",
               "meta (description) file" ).withRequiredArg().describedAs( "meta_clinical_events.txt" ).ofType( String.class );
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
    
    private static void importData(String dataFile, int cancerStudyId) throws IOException, DaoException {
        MySQLbulkLoader.bulkLoadOn();
        FileReader reader =  new FileReader(dataFile);
        BufferedReader buff = new BufferedReader(reader);

        String line = buff.readLine();
        if (!line.startsWith("PATEINT_ID\tSTART_DATE\tSTOP_DATE\tEVENT_TYPE")) {
            System.err.println("The first line must start with 'PATEINT_ID\tSTART_DATE\tSTOP_DATE\tEVENT_TYPE'");
            return;
        }
        String[] headers = line.split("\t");

        long clinicalEventId = DaoClinicalEvent.getLargestClinicalEventId();
        
        while ((line = buff.readLine()) != null) {
            line = line.trim();

            String[] fields = line.split("\t");
            if (fields.length > headers.length) {
                System.err.println("more attributes than header: "+line);
                continue;
            }
            
            ClinicalEvent event = new ClinicalEvent();
            event.setClinicalEventId(++clinicalEventId);
            event.setCancerStudyId(cancerStudyId);
            event.setPatientId(fields[0]);
            event.setStartDate(Long.valueOf(fields[1]));
            if (!fields[2].isEmpty()) {
                event.setStopDate(Long.valueOf(fields[2]));
            }
            event.setEventType(fields[3]);
            
            Map<String, String> eventData = new HashMap<String, String>();
            for (int i = 4; i < fields.length; i++) {
                if (!fields[i].isEmpty()) {
                    eventData.put(headers[i], fields[i]);
                }
            }
            event.setEventData(eventData);
            
            DaoClinicalEvent.addClinicalEvent(event);
        }
        
        MySQLbulkLoader.flushAll();
    }
    
}
