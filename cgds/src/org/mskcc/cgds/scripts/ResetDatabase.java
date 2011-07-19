package org.mskcc.cgds.scripts;

import org.mskcc.cgds.dao.DaoCancerStudy;
import org.mskcc.cgds.dao.DaoCase;
import org.mskcc.cgds.dao.DaoCaseList;
import org.mskcc.cgds.dao.DaoClinicalData;
import org.mskcc.cgds.dao.DaoException;
import org.mskcc.cgds.dao.DaoGeneOptimized;
import org.mskcc.cgds.dao.DaoGeneticAlteration;
import org.mskcc.cgds.dao.DaoGeneticProfile;
import org.mskcc.cgds.dao.DaoGeneticProfileCases;
import org.mskcc.cgds.dao.DaoInteraction;
import org.mskcc.cgds.dao.DaoMicroRna;
import org.mskcc.cgds.dao.DaoMicroRnaAlteration;
import org.mskcc.cgds.dao.DaoMutation;
import org.mskcc.cgds.dao.DaoMutationFrequency;
import org.mskcc.cgds.dao.DaoSecretKey;
import org.mskcc.cgds.dao.DaoTypeOfCancer;
import org.mskcc.cgds.dao.DaoUser;
import org.mskcc.cgds.dao.DaoUserAccessRight;
import org.mskcc.cgds.model.SecretKey;
import org.mskcc.cgds.util.AccessControl;
import org.mskcc.cgds.util.DatabaseProperties;

/**
 * Empty the database.
 * 
 * @author Ethan Cerami
 * @author Arthur Goldberg goldberg@cbio.mskcc.org
 */
public class ResetDatabase {
   
   public static final int MAX_RESET_SIZE = 6;

   /**
    * Remove all records from any size database.
    * Whenever a new Dao* class is defined, must add its deleteAllRecords() method here. 
    * @throws DaoException
    */
   public static void resetAnySizeDatabase() throws DaoException {
      
      DaoSecretKey.deleteAllRecords();
      DaoUser.deleteAllRecords();
       DaoUserAccessRight.deleteAllRecords();
       DaoTypeOfCancer.deleteAllRecords();
       DaoCancerStudy.deleteAllRecords();
        DaoMicroRna daoMicroRna = new DaoMicroRna();
        daoMicroRna.deleteAllRecords();
        DaoGeneOptimized daoGene = DaoGeneOptimized.getInstance();
        daoGene.deleteAllRecords();
        DaoCase daoCase = new DaoCase();
        daoCase.deleteAllRecords();
        DaoGeneticAlteration daoGenetic = DaoGeneticAlteration.getInstance();
        daoGenetic.deleteAllRecords();
        DaoMicroRnaAlteration daoMicroRnaAlteration = DaoMicroRnaAlteration.getInstance();
        daoMicroRnaAlteration.deleteAllRecords();
        
        DaoGeneticProfile daoGeneticProfile = new DaoGeneticProfile();
        daoGeneticProfile.deleteAllRecords();
        DaoCaseList daoCaseList = new DaoCaseList();
        daoCaseList.deleteAllRecords();
        DaoClinicalData daoClinicalData = new DaoClinicalData();
        daoClinicalData.deleteAllRecords();
        DaoMutation daoMutation = DaoMutation.getInstance();
        daoMutation.deleteAllRecords();
        DaoMutationFrequency daoMutationFrequency = new DaoMutationFrequency();
        daoMutationFrequency.deleteAllRecords();
        DaoGeneticProfileCases daoGeneticProfileCases = new DaoGeneticProfileCases();
        daoGeneticProfileCases.deleteAllRecords();
        DaoInteraction daoInteraction = DaoInteraction.getInstance();
        daoInteraction.deleteAllRecords();
        
        // No production keys stored in filesystem or code: digest the key; put it in properties; load it into dbms on startup
        DatabaseProperties databaseProperties = DatabaseProperties.getInstance();
        SecretKey secretKey = new SecretKey();
        secretKey.setEncryptedKey(databaseProperties.getDbEncryptedKey());
        DaoSecretKey.addSecretKey(secretKey);

    }
    
    public static void resetDatabase() throws DaoException {
       
       // safety measure: don't reset a big database
       if( MAX_RESET_SIZE < DaoCancerStudy.getCount() ){
          throw new DaoException( "Database has " + DaoCancerStudy.getCount() + 
                   " studies, and we don't reset a database with more than " + MAX_RESET_SIZE + " records.");
       }else{
          resetAnySizeDatabase();
       }
       
    }
    
    public static void main(String[] args) throws DaoException {
        StatDatabase.statDb();
        ResetDatabase.resetDatabase();
        System.err.println("Database Cleared and Reset.");
    }
}
