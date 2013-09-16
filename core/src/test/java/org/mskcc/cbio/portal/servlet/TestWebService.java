/** Copyright (c) 2012 Memorial Sloan-Kettering Cancer Center.
**
** This library is free software; you can redistribute it and/or modify it
** under the terms of the GNU Lesser General Public License as published
** by the Free Software Foundation; either version 2.1 of the License, or
** any later version.
**
** This library is distributed in the hope that it will be useful, but
** WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
** MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
** documentation provided hereunder is on an "as is" basis, and
** Memorial Sloan-Kettering Cancer Center 
** has no obligations to provide maintenance, support,
** updates, enhancements or modifications.  In no event shall
** Memorial Sloan-Kettering Cancer Center
** be liable to any party for direct, indirect, special,
** incidental or consequential damages, including lost profits, arising
** out of the use of this software and its documentation, even if
** Memorial Sloan-Kettering Cancer Center 
** has been advised of the possibility of such damage.  See
** the GNU Lesser General Public License for more details.
**
** You should have received a copy of the GNU Lesser General Public License
** along with this library; if not, write to the Free Software Foundation,
** Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
**/

package org.mskcc.cbio.portal.servlet;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import junit.framework.TestCase;
import org.mskcc.cbio.portal.dao.*;
import org.mskcc.cbio.portal.model.*;
import org.mskcc.cbio.portal.scripts.ImportTypesOfCancers;
import org.mskcc.cbio.portal.scripts.ResetDatabase;
import org.mskcc.cbio.portal.util.NullHttpServletRequest;
import org.mskcc.cbio.portal.util.ProgressMonitor;
import org.mskcc.cbio.portal.util.WebserviceParserUtils;

/**
 * JUnit test for WebService class.
 */
public class TestWebService extends TestCase {

   private CancerStudy publicCancerStudy;
   private CancerStudy privateCancerStudy1;
   private CancerStudy privateCancerStudy2;
   private User user1;
   private User user2;
   private String cleartextPwd;
   private GeneticProfile privateGeneticProfile;
   private GeneticProfile publicGeneticProfile;
   
   public void testWebService() throws Exception {
      setUpDBMS();
      
      WebService webService = new WebService();

      // null request
      NullHttpServletRequest aNullHttpServletRequest = new NullHttpServletRequest();
      NullHttpServletResponse aNullHttpServletResponse = new NullHttpServletResponse();
      webService.processClient( aNullHttpServletRequest, aNullHttpServletResponse );
      assertTrue( aNullHttpServletResponse.getOutput().contains("Error: you must specify a cmd parameter.\n") );
      
      checkRequest( mkStringArray( WebService.CMD, "getTypesOfCancer" ), 
               mkStringArray( "type_of_cancer_id\tname", "BLCA\tBladder Urothelial Carcinoma" ) );

      // bad command
      checkRequest( mkStringArray( WebService.CMD, "badCommand" ), "Error: 'badCommand' not a valid command." );
      
      /* TBD: Recoded when we provide granualar access
      // public studies
      String[] publicStudies = mkStringArray( "cancer_study_id\tname\tdescription", 
               studyLine( publicCancerStudy ) );
      checkRequest( mkStringArray( WebService.CMD, "getCancerStudies" ), publicStudies );
      */
      
      // no cancer_study_id for "getGeneticProfiles" 
      checkRequest( mkStringArray( WebService.CMD, "getGeneticProfiles" ), 
            mkStringArray( "Error: " + "No cancer study (cancer_study_id), or genetic profile (genetic_profile_id) " +
                              "or case list or (case_list) case set (case_set_id) provided by request. " +
                              "Please reformulate request." ) );
      checkRequest( mkStringArray( WebService.CMD, "getGeneticProfiles",
              WebService.CANCER_STUDY_ID, CancerStudy.NO_SUCH_STUDY +"" ),
            mkStringArray( "Error: " + "Problem when identifying a cancer study for the request." ) );

      // getGeneticProfiles for public study
      String[] publicGenePro = mkStringArray(
         "genetic_profile_id\tgenetic_profile_name\tgenetic_profile_description\t" +
            "cancer_study_id\tgenetic_alteration_type\tshow_profile_in_analysis_tab",
         "stableIdpublic\tprofileName\tprofileDescription\t3\tCOPY_NUMBER_ALTERATION\ttrue");  
      checkRequest( mkStringArray( 
            WebService.CMD, "getGeneticProfiles", 
            WebService.CANCER_STUDY_ID, publicCancerStudy.getCancerStudyStableId()),
            publicGenePro );

      /* TBD: Recoded when we provide granualar access
      // with bad key
      checkRequest( mkStringArray( 
            WebService.CMD, "getGeneticProfiles", 
            WebService.SECRET_KEY, "not_good_key", 
            WebService.CANCER_STUDY_ID, publicCancerStudy.getCancerStudyStableId()),
            publicGenePro );
      
      // missing email address for private study
      String deniedError = "Error: User cannot access the cancer study called 'name'." +
      "Please provide credentials to access private data.";
      checkRequest( mkStringArray( WebService.CMD, "getGeneticProfiles", WebService.CANCER_STUDY_ID, "study1" ),
               mkStringArray( deniedError ) );
      
      // denied for "getGeneticProfiles"; no key 
      checkRequest( mkStringArray( 
            WebService.CMD, "getGeneticProfiles", 
            WebService.CANCER_STUDY_ID, "study1",
            WebService.EMAIL_ADDRESS, user1.getEmail() 
            ), 
            mkStringArray( deniedError ) );
      
      // denied for "getGeneticProfiles"; bad key 
      checkRequest( mkStringArray( 
            WebService.CMD, "getGeneticProfiles", 
            WebService.SECRET_KEY, "not_good_key", 
            WebService.CANCER_STUDY_ID, "study1",
            WebService.EMAIL_ADDRESS, user1.getEmail() 
            ), 
            mkStringArray( deniedError ) );

      // getGeneticProfiles works for private study 
      String[] privateGenePro1 = mkStringArray( "genetic_profile_id\tgenetic_profile_name\tgenetic_profile_description"
            + "\tcancer_study_id\tgenetic_alteration_type\tshow_profile_in_analysis_tab",
      "stableIdPrivate\tprofileName\tprofileDescription\t1\tCOPY_NUMBER_ALTERATION\ttrue");  
      checkRequest( mkStringArray( 
            WebService.CMD, "getGeneticProfiles", 
            WebService.SECRET_KEY, cleartextPwd, 
            WebService.CANCER_STUDY_ID, "study1", 
            WebService.EMAIL_ADDRESS, user1.getEmail() 
            ), 
            mkStringArray( privateGenePro1 ) );
      */
   }
   
   private void checkRequest( String[] requestFields, String... responseLines ) throws IOException{
      checkRequest( false, requestFields, responseLines );
   }
   
   /**
    * check a request
    * @param debug            if true, print servlet's output
    * @param requestFields    array embedded with name, value pairs for the httpRequest 
    * @param responseLines    array of expected responses
    * @throws IOException
    */
   private void checkRequest( boolean debug, String[] requestFields, String... responseLines ) throws IOException{
      WebService webService = new WebService();
      NullHttpServletRequest aNullHttpServletRequest = new NullHttpServletRequest();
      NullHttpServletResponse aNullHttpServletResponse = new NullHttpServletResponse();
      for( int i=0; i<requestFields.length; i += 2 ){
         aNullHttpServletRequest.setParameter( requestFields[i], requestFields[i+1] );
      }

      webService.processClient( aNullHttpServletRequest, aNullHttpServletResponse );
      if( debug ){
         System.out.println( "\nResponse says:\n" + aNullHttpServletResponse.getOutput() );
      }
      String[] lines = aNullHttpServletResponse.getOutput().split("\n");
      for( int i=0; i<responseLines.length; i++ ){
          assertEquals( responseLines[i], lines[i]);
      }
   }
   
   private String[] mkStringArray( String... requestFields ){
      return requestFields;
   }
   
   private String studyLine( CancerStudy cancerStudy ){
      return cancerStudy.getCancerStudyStableId() + "\t" + cancerStudy.getName()
              + "\t" + cancerStudy.getDescription();
   }

   public void testGetCancerStudyIDs() throws Exception {

      setUpDBMS();
      HashSet<String> studies;
      NullHttpServletRequest aNullHttpServletRequest = new NullHttpServletRequest();
      studies = WebserviceParserUtils.getCancerStudyIDs(aNullHttpServletRequest);
      assertEquals( 0, studies.size() );
      
      // example getGeneticProfiles request      
      aNullHttpServletRequest.setParameter(WebService.CANCER_STUDY_ID, "HI");
      studies = WebserviceParserUtils.getCancerStudyIDs(aNullHttpServletRequest);
      assertTrue(studies.isEmpty());
      aNullHttpServletRequest.setParameter(WebService.CANCER_STUDY_ID, "33");
      studies = WebserviceParserUtils.getCancerStudyIDs(aNullHttpServletRequest);
      assertTrue(studies.isEmpty());
      aNullHttpServletRequest.setParameter(WebService.CANCER_STUDY_ID, "study1");
      studies = WebserviceParserUtils.getCancerStudyIDs(aNullHttpServletRequest);
      assertEquals( 1, studies.size() );
      assertTrue( studies.contains("study1"));

      // example getProfileData, getMutationData, ... request with existing CASE_SET_ID
      aNullHttpServletRequest = new NullHttpServletRequest();
      aNullHttpServletRequest.setParameter(WebService.CASE_SET_ID, "HI");
      studies = WebserviceParserUtils.getCancerStudyIDs(aNullHttpServletRequest);
      assertTrue(studies.isEmpty());

      DaoCaseList aDaoCaseList = new DaoCaseList();
      String exampleCaseSetId = "exampleID";
      int thisIsNotACancerStudyId = 5;
      CaseList caseList = new CaseList( exampleCaseSetId, 0, thisIsNotACancerStudyId, "", CaseListCategory.OTHER);
      ArrayList<String> t = new ArrayList<String>();
      caseList.setCaseList( t );
      aDaoCaseList.addCaseList(caseList);
      aNullHttpServletRequest.setParameter(WebService.CASE_SET_ID, exampleCaseSetId );
      studies = WebserviceParserUtils.getCancerStudyIDs(aNullHttpServletRequest);
      assertTrue(studies.isEmpty());

      aDaoCaseList.deleteAllRecords();
      caseList.setCancerStudyId( 1 ); // CancerStudyId inserted by setUpDBMS()
      aDaoCaseList.addCaseList(caseList);
      studies = WebserviceParserUtils.getCancerStudyIDs(aNullHttpServletRequest);
      assertEquals( 1, studies.size() );
      assertTrue( studies.contains("study1"));
      
      // test situations when case_set_id not provided, but profile_id is, as by getProfileData
      aNullHttpServletRequest = new NullHttpServletRequest();
      aNullHttpServletRequest.setParameter(WebService.GENETIC_PROFILE_ID, 
               privateGeneticProfile.getStableId() );
      studies = WebserviceParserUtils.getCancerStudyIDs(aNullHttpServletRequest);
      assertEquals( 1, studies.size() );
      assertTrue( studies.contains(
              DaoCancerStudy.getCancerStudyByInternalId(
                      privateGeneticProfile.getCancerStudyId()).getCancerStudyStableId()));

      // test situation when multiple profile_ids provided, as by getProfileData
      aNullHttpServletRequest = new NullHttpServletRequest();
      aNullHttpServletRequest.setParameter(
               WebService.GENETIC_PROFILE_ID, privateGeneticProfile.getStableId() + ","
              + publicGeneticProfile.getStableId() );
      studies = WebserviceParserUtils.getCancerStudyIDs(aNullHttpServletRequest);
      assertTrue( studies.contains(DaoCancerStudy.getCancerStudyByInternalId
              (privateGeneticProfile.getCancerStudyId()).getCancerStudyStableId()));
      assertTrue( studies.contains(DaoCancerStudy.getCancerStudyByInternalId
              (privateGeneticProfile.getCancerStudyId()).getCancerStudyStableId()));

      // test situation when a case_list is explicitly provided, as in getClinicalData, etc.
//      String c1 = "TCGA-12345";
//      DaoCaseProfile.addCaseProfile( c1, publicGeneticProfile.getGeneticProfileId());
//      aNullHttpServletRequest = new NullHttpServletRequest();
//      aNullHttpServletRequest.setParameter( WebService.CASE_LIST, c1 ); 
//      studies = WebserviceParserUtils.getCancerStudyIDs(aNullHttpServletRequest);
//      assertTrue( studies.contains(DaoCancerStudy.getCancerStudyByInternalId
//              (publicGeneticProfile.getCancerStudyId()).getCancerStudyStableId()));
//
//      String c2 = "TCGA-54321";
//      DaoCaseProfile.addCaseProfile( c2, privateGeneticProfile.getGeneticProfileId() );
//      aNullHttpServletRequest = new NullHttpServletRequest();
//      aNullHttpServletRequest.setParameter( WebService.CASE_LIST, c1 + "," + c2 ); 
//      studies = WebserviceParserUtils.getCancerStudyIDs(aNullHttpServletRequest);
//      assertTrue( studies.contains(DaoCancerStudy.getCancerStudyByInternalId
//              (privateGeneticProfile.getCancerStudyId()).getCancerStudyStableId()));
//      assertTrue( studies.contains(DaoCancerStudy.getCancerStudyByInternalId
//              (publicGeneticProfile.getCancerStudyId()).getCancerStudyStableId()));
   }
   
   private void setUpDBMS() throws DaoException, IOException{
      ResetDatabase.resetDatabase();

      user1 = new User("artg@gmail.com", "Arthur", true);
      DaoUser.addUser(user1);
      user2 = new User("joe@gmail.com", "J", true);
      DaoUser.addUser(user2);

      // load cancers
	  // TBD: change this to use getResourceAsStream()
      ImportTypesOfCancers.load(new ProgressMonitor(), new File("target/test-classes/cancers.txt"));

      // make a couple of private studies (1 and 2)
      privateCancerStudy1 = new CancerStudy( "name", "description", "study1", "brca", false );
      DaoCancerStudy.addCancerStudy(privateCancerStudy1);  // 1
      privateCancerStudy2 = new CancerStudy( "other name", "other description", "study2", "brca", false );
      DaoCancerStudy.addCancerStudy(privateCancerStudy2);  // 2
      
      publicCancerStudy = new CancerStudy( "public name", "description", "study3", "brca", true );
      DaoCancerStudy.addCancerStudy(publicCancerStudy);  // 3
      
      UserAuthorities authorities = new UserAuthorities(user1.getEmail(), java.util.Arrays.asList("ROLE_USER"));
      DaoUserAuthorities.addUserAuthorities(authorities);
      
      String publicSid = "stableIdpublic";
      publicGeneticProfile = new GeneticProfile( publicSid, publicCancerStudy.getInternalId(),
												 GeneticAlterationType.COPY_NUMBER_ALTERATION, "DISCRETE",
												 "profileName", "profileDescription", true);
      DaoGeneticProfile.addGeneticProfile( publicGeneticProfile );
      // have to refetch from the dbms to get the profile_id; sigh!
      publicGeneticProfile = DaoGeneticProfile.getGeneticProfileByStableId( publicSid ); 
      String privateSid = "stableIdPrivate";
      privateGeneticProfile = new GeneticProfile( privateSid, privateCancerStudy1.getInternalId(),
												  GeneticAlterationType.COPY_NUMBER_ALTERATION, "DISCRETE",
												  "profileName", "profileDescription", true);
      DaoGeneticProfile.addGeneticProfile( privateGeneticProfile );
      privateGeneticProfile = DaoGeneticProfile.getGeneticProfileByStableId(privateSid);
   }
   
}
