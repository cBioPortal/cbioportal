package org.mskcc.cgds.util;

import org.mskcc.cgds.dao.DaoCancerStudy;
import org.mskcc.cgds.dao.DaoException;
import org.mskcc.cgds.model.CancerStudy;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * Reads and loads a cancer study file. (Before July 2011, was called a cancer type file.)
 * By default, the loaded cancers are public. 
 * 
 * @author Arthur Goldberg goldberg@cbio.mskcc.org
 */
public class CancerStudyReader {

   public static CancerStudy loadCancerStudy(File file) throws IOException, DaoException {
      Properties properties = new Properties();
      properties.load(new FileInputStream(file));

      String cancerStudyIdentifier = properties.getProperty("cancer_study_identifier");
      if (cancerStudyIdentifier == null) {
         throw new IllegalArgumentException("cancer_study_identifier is not specified.");
      }
      
      if ( DaoCancerStudy.doesCancerStudyExistByStableId(cancerStudyIdentifier) ) {
         throw new IllegalArgumentException("cancer study identified by cancer_study_identifier "
                  + cancerStudyIdentifier + " already in dbms.");
      }

      String name = properties.getProperty("name");
      if (name == null) {
         throw new IllegalArgumentException("name is not specified.");
      }

      String description = properties.getProperty("description");
      if (description == null) {
         throw new IllegalArgumentException("description is not specified.");
      }

      String typeOfCancer = properties.getProperty("type_of_cancer");
      if ( typeOfCancer == null) {
         throw new IllegalArgumentException("type of cancer is not specified.");
      }
      
      return addCancerStudy(cancerStudyIdentifier, name, description, 
               typeOfCancer, publicStudy( properties ) );
   }

   private static CancerStudy addCancerStudy(String cancerStudyIdentifier, String name, String description, 
            String typeOfCancer, boolean publicStudy )
            throws DaoException {
      CancerStudy cancerStudy = new CancerStudy( name, description, 
               cancerStudyIdentifier, typeOfCancer, publicStudy );
      DaoCancerStudy.addCancerStudy(cancerStudy);
      return cancerStudy;
   }
   
   private static boolean publicStudy( Properties properties ) {
      String studyAccess = properties.getProperty("study_access");
      if ( studyAccess != null) {
         if( studyAccess.equals("public") ){
            return true;
         }
         if( studyAccess.equals("private") ){
            return false;
         }
         throw new IllegalArgumentException("study_access must be either 'public' or 'private', but is " + 
                  studyAccess );
      }
      // studies are public by default
      return true;
   }

}